package app.opusreader.feature.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.opusreader.data.entity.AnnotationEntity
import app.opusreader.data.entity.AnnotationType
import app.opusreader.data.repository.LibraryRepository
import app.opusreader.data.repository.SettingsRepository
import app.opusreader.engine.ReaderEngine
import app.opusreader.engine.model.BookSource
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.LocatorRange
import app.opusreader.engine.model.Page
import app.opusreader.engine.model.Progress
import app.opusreader.engine.model.ReaderSettings
import app.opusreader.engine.model.TocEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ReaderViewModel(
    private val libraryRepository: LibraryRepository,
    private val settingsRepository: SettingsRepository,
    private val engine: ReaderEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var activeBookId: String? = null

    fun loadBook(bookId: String) {
        activeBookId = bookId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val book = libraryRepository.getBook(bookId)
            if (book != null) {
                val bookUi = ReaderBookUiModel(
                    id = book.id,
                    title = book.title,
                    author = book.author,
                    filePath = book.filePath,
                    coverPath = book.coverPath,
                    spineIndex = book.spineIndex,
                    charOffset = book.charOffset,
                    progressPercent = book.progressPercent
                )

                try {
                    engine.open(BookSource(book.id, book.filePath), ReaderSettings())
                } catch (_: Throwable) {
                    // Fall back to engine's current state if open fails or file is simulated
                }

                val initialLocator = Locator(book.spineIndex, book.charOffset)
                val initialPage = engine.pageAt(initialLocator)
                val initialProgress = engine.progress(initialPage)
                val toc = engine.toc

                // Check if current page is bookmarked
                val annotations = libraryRepository.getAnnotations(bookId).firstOrNull() ?: emptyList()
                val isBookmarked = annotations.any {
                    it.type == AnnotationType.BOOKMARK &&
                        it.spineIndex == initialPage.start.spineIndex &&
                        it.startOffset <= initialPage.end.charOffset &&
                        it.endOffset >= initialPage.start.charOffset
                }

                val total = initialProgress.total.coerceAtLeast(1)
                val current = initialProgress.current.coerceIn(1, total)

                _uiState.update {
                    it.copy(
                        book = bookUi,
                        currentPage = initialPage,
                        progress = initialProgress,
                        tocEntries = toc,
                        isBookmarked = isBookmarked,
                        scrubberLocation = current,
                        totalLocations = total,
                        isLoading = false
                    )
                }

                // Observe annotations
                launch {
                    libraryRepository.getAnnotations(bookId).collect { annotList ->
                        val uiAnnots = annotList.map { it.toUiModel() }
                        _uiState.update { current ->
                            val bookmarked = uiAnnots.any { a ->
                                a.type == AnnotationUiType.BOOKMARK &&
                                    current.currentPage != null &&
                                    a.spineIndex == current.currentPage.start.spineIndex
                            }
                            current.copy(
                                annotations = uiAnnots,
                                isBookmarked = bookmarked
                            )
                        }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onPreviousPage() {
        val current = _uiState.value.currentPage ?: return
        val prev = engine.previous(current) ?: return
        val prog = engine.progress(prev)
        updatePageState(prev, prog)
    }

    fun onNextPage() {
        val current = _uiState.value.currentPage ?: return
        val next = engine.next(current) ?: return
        val prog = engine.progress(next)
        updatePageState(next, prog)
    }

    fun onToggleChrome() {
        _uiState.update { it.copy(isChromeVisible = !it.isChromeVisible) }
    }

    fun setChromeVisible(visible: Boolean) {
        _uiState.update { it.copy(isChromeVisible = visible) }
    }

    fun onToggleBookmark() {
        val bookId = activeBookId ?: return
        val currentPage = _uiState.value.currentPage ?: return
        val currentlyBookmarked = _uiState.value.isBookmarked

        viewModelScope.launch {
            if (currentlyBookmarked) {
                val annotations = libraryRepository.getAnnotations(bookId).firstOrNull() ?: emptyList()
                val bookmark = annotations.firstOrNull {
                    it.type == AnnotationType.BOOKMARK && it.spineIndex == currentPage.start.spineIndex
                }
                if (bookmark != null) {
                    libraryRepository.deleteAnnotation(bookmark.id)
                }
                _uiState.update { it.copy(isBookmarked = false) }
            } else {
                val newBookmark = AnnotationEntity(
                    id = UUID.randomUUID().toString(),
                    bookId = bookId,
                    spineIndex = currentPage.start.spineIndex,
                    startOffset = currentPage.start.charOffset,
                    endOffset = currentPage.end.charOffset,
                    selectedText = "Bookmark at ${currentPage.start.charOffset}",
                    type = AnnotationType.BOOKMARK
                )
                libraryRepository.addAnnotation(newBookmark)
                _uiState.update { it.copy(isBookmarked = true) }
            }
        }
    }

    fun onScrubberChange(location: Int) {
        val total = _uiState.value.totalLocations.coerceAtLeast(1)
        val clamped = location.coerceIn(1, total)
        val pct = (clamped.toFloat() / total.toFloat()) * 100f
        _uiState.update {
            it.copy(
                scrubberLocation = clamped,
                progress = Progress(current = clamped, total = total, percent = pct)
            )
        }
    }

    fun onScrubberCommit(location: Int) {
        val total = _uiState.value.totalLocations.coerceAtLeast(1)
        val clamped = location.coerceIn(1, total)
        val targetPage = engine.pageAt(Locator(0, clamped))
        val prog = engine.progress(targetPage)
        updatePageState(targetPage, prog)
    }

    fun onOpenSheet(sheet: ActiveReaderSheet) {
        _uiState.update { it.copy(activeSheet = sheet) }
    }

    fun onDismissSheet() {
        _uiState.update { it.copy(activeSheet = null) }
    }

    fun onToggleThumbnails() {
        _uiState.update { it.copy(isThumbnailsVisible = !it.isThumbnailsVisible) }
    }

    fun onWordLongPress(locator: Locator) {
        val page = _uiState.value.currentPage ?: return
        val word = page.words.firstOrNull {
            it.start.spineIndex == locator.spineIndex &&
                locator.charOffset in it.start.charOffset..it.end.charOffset
        }
        if (word != null) {
            val range = LocatorRange(start = word.start, end = word.end)
            _uiState.update {
                it.copy(
                    selectedRange = range,
                    selectedText = word.text,
                    activeAnnotation = null
                )
            }
        }
    }

    fun onUpdateSelectionRange(range: LocatorRange) {
        val page = _uiState.value.currentPage ?: return
        val text = engine.textFor(page, range)
        _uiState.update {
            it.copy(
                selectedRange = range,
                selectedText = text
            )
        }
    }

    fun onClearSelection() {
        _uiState.update {
            it.copy(
                selectedRange = null,
                selectedText = null,
                activeAnnotation = null
            )
        }
    }

    fun onAnnotationClick(annotation: AnnotationUiModel) {
        _uiState.update {
            it.copy(
                activeAnnotation = annotation,
                selectedRange = LocatorRange(
                    start = Locator(annotation.spineIndex, annotation.startOffset),
                    end = Locator(annotation.spineIndex, annotation.endOffset)
                ),
                selectedText = annotation.selectedText
            )
        }
    }

    fun onCreateHighlight(colorHex: String) {
        val bookId = activeBookId ?: return
        val range = _uiState.value.selectedRange ?: return
        val text = _uiState.value.selectedText ?: ""
        viewModelScope.launch {
            val highlight = AnnotationEntity(
                id = UUID.randomUUID().toString(),
                bookId = bookId,
                spineIndex = range.start.spineIndex,
                startOffset = range.start.charOffset,
                endOffset = range.end.charOffset,
                selectedText = text,
                colorHex = colorHex,
                type = AnnotationType.HIGHLIGHT
            )
            libraryRepository.addAnnotation(highlight)
            onClearSelection()
        }
    }

    fun onChangeHighlightColor(annotationId: String, newColorHex: String) {
        val annotation = _uiState.value.annotations.firstOrNull { it.id == annotationId } ?: return
        viewModelScope.launch {
            val updated = annotation.copy(colorHex = newColorHex)
            libraryRepository.addAnnotation(updated.toEntity())
            onClearSelection()
        }
    }

    fun onDeleteAnnotation(annotationId: String) {
        viewModelScope.launch {
            libraryRepository.deleteAnnotation(annotationId)
            onClearSelection()
        }
    }

    fun onOpenNoteEditor() {
        _uiState.update { it.copy(isNoteEditorVisible = true) }
    }

    fun onDismissNoteEditor() {
        _uiState.update { it.copy(isNoteEditorVisible = false) }
    }

    fun onSaveNote(annotationId: String?, noteText: String) {
        val bookId = activeBookId ?: return
        val existing = _uiState.value.annotations.firstOrNull { it.id == annotationId }
        val range = _uiState.value.selectedRange
        val text = _uiState.value.selectedText ?: ""

        viewModelScope.launch {
            if (existing != null) {
                val updated = existing.copy(
                    noteText = noteText,
                    type = AnnotationUiType.NOTE
                )
                libraryRepository.addAnnotation(updated.toEntity())
            } else if (range != null) {
                val newNote = AnnotationEntity(
                    id = UUID.randomUUID().toString(),
                    bookId = bookId,
                    spineIndex = range.start.spineIndex,
                    startOffset = range.start.charOffset,
                    endOffset = range.end.charOffset,
                    selectedText = text,
                    noteText = noteText,
                    type = AnnotationType.NOTE
                )
                libraryRepository.addAnnotation(newNote)
            }
            _uiState.update { it.copy(isNoteEditorVisible = false) }
            onClearSelection()
        }
    }

    fun onToggleFavorite(annotationId: String) {
        val annotation = _uiState.value.annotations.firstOrNull { it.id == annotationId } ?: return
        viewModelScope.launch {
            libraryRepository.toggleAnnotationFavorite(annotationId, !annotation.isFavorite)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearching = query.isNotBlank()) }
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false, activeSearchHitIndex = -1) }
            return
        }
        viewModelScope.launch {
            val hits = engine.search(query)
            _uiState.update {
                it.copy(
                    searchResults = hits,
                    isSearching = false,
                    activeSearchHitIndex = if (hits.isNotEmpty()) 0 else -1
                )
            }
        }
    }

    fun onSelectSearchHit(hitIndex: Int) {
        val hits = _uiState.value.searchResults
        if (hitIndex in hits.indices) {
            val hit = hits[hitIndex]
            val page = engine.pageAt(hit.locator)
            val prog = engine.progress(page)
            updatePageState(page, prog)
            _uiState.update {
                it.copy(
                    activeSearchHitIndex = hitIndex,
                    activeSheet = null
                )
            }
        }
    }

    fun onNextSearchHit() {
        val hits = _uiState.value.searchResults
        val current = _uiState.value.activeSearchHitIndex
        if (hits.isNotEmpty() && current + 1 < hits.size) {
            onSelectSearchHit(current + 1)
        }
    }

    fun onPreviousSearchHit() {
        val hits = _uiState.value.searchResults
        val current = _uiState.value.activeSearchHitIndex
        if (hits.isNotEmpty() && current > 0) {
            onSelectSearchHit(current - 1)
        }
    }

    fun onSetAnnotationFilter(filter: AnnotationFilter) {
        _uiState.update { it.copy(annotationsFilter = filter) }
    }

    fun onSetAnnotationSort(sort: AnnotationSort) {
        _uiState.update { it.copy(annotationsSort = sort) }
    }

    fun onJumpToToc(locator: Locator) {
        val targetPage = engine.pageAt(locator)
        val prog = engine.progress(targetPage)
        updatePageState(targetPage, prog)
        onDismissSheet()
    }

    fun setFixtureState(
        book: ReaderBookUiModel,
        page: Page,
        progress: Progress,
        toc: List<TocEntry>,
        chromeVisible: Boolean,
        activeSheet: ActiveReaderSheet? = null,
        annotations: List<AnnotationUiModel> = emptyList(),
        searchResults: List<app.opusreader.engine.model.SearchHit> = emptyList(),
        searchQuery: String = ""
    ) {
        activeBookId = book.id
        _uiState.update {
            it.copy(
                book = book,
                currentPage = page,
                progress = progress,
                tocEntries = toc,
                isChromeVisible = chromeVisible,
                activeSheet = activeSheet,
                annotations = annotations,
                searchResults = searchResults,
                searchQuery = searchQuery,
                scrubberLocation = progress.current,
                totalLocations = progress.total,
                isLoading = false
            )
        }
    }

    private fun updatePageState(page: Page, progress: Progress) {
        val bookId = activeBookId
        val total = progress.total.coerceAtLeast(1)
        val current = progress.current.coerceIn(1, total)

        _uiState.update {
            it.copy(
                currentPage = page,
                progress = progress,
                scrubberLocation = current,
                totalLocations = total
            )
        }

        if (bookId != null) {
            viewModelScope.launch {
                libraryRepository.updateProgress(
                    id = bookId,
                    spineIndex = page.start.spineIndex,
                    charOffset = page.start.charOffset,
                    progressPercent = progress.percent / 100f
                )
            }
        }
    }

    companion object {
        fun provideFactory(
            libraryRepository: LibraryRepository,
            settingsRepository: SettingsRepository,
            readerEngine: ReaderEngine
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReaderViewModel(
                    libraryRepository = libraryRepository,
                    settingsRepository = settingsRepository,
                    engine = readerEngine
                ) as T
            }
        }
    }
}

private fun AnnotationEntity.toUiModel(): AnnotationUiModel = AnnotationUiModel(
    id = id,
    bookId = bookId,
    spineIndex = spineIndex,
    startOffset = startOffset,
    endOffset = endOffset,
    selectedText = selectedText,
    noteText = noteText,
    colorHex = colorHex,
    isFavorite = isFavorite,
    createdTimestamp = createdTimestamp,
    type = when (type) {
        AnnotationType.HIGHLIGHT -> AnnotationUiType.HIGHLIGHT
        AnnotationType.NOTE -> AnnotationUiType.NOTE
        AnnotationType.BOOKMARK -> AnnotationUiType.BOOKMARK
    }
)

private fun AnnotationUiModel.toEntity(): AnnotationEntity = AnnotationEntity(
    id = id,
    bookId = bookId,
    spineIndex = spineIndex,
    startOffset = startOffset,
    endOffset = endOffset,
    selectedText = selectedText,
    noteText = noteText,
    colorHex = colorHex,
    isFavorite = isFavorite,
    createdTimestamp = createdTimestamp,
    type = when (type) {
        AnnotationUiType.HIGHLIGHT -> AnnotationType.HIGHLIGHT
        AnnotationUiType.NOTE -> AnnotationType.NOTE
        AnnotationUiType.BOOKMARK -> AnnotationType.BOOKMARK
    }
)
