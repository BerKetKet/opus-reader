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
                // Delete bookmark
                val annotations = libraryRepository.getAnnotations(bookId).firstOrNull() ?: emptyList()
                val bookmark = annotations.firstOrNull {
                    it.type == AnnotationType.BOOKMARK && it.spineIndex == currentPage.start.spineIndex
                }
                if (bookmark != null) {
                    libraryRepository.deleteAnnotation(bookmark.id)
                }
                _uiState.update { it.copy(isBookmarked = false) }
            } else {
                // Create bookmark
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
        activeSheet: ActiveReaderSheet? = null
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
