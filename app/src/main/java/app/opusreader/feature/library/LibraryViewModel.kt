package app.opusreader.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.opusreader.data.entity.BookEntity
import app.opusreader.data.repository.LibraryRepository
import app.opusreader.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                libraryRepository.getBooks(),
                libraryRepository.getDistinctCollections(),
                settingsRepository.settingsFlow
            ) { books, collections, settings ->
                Triple(books, collections, settings)
            }.collect { (books, collections, settings) ->
                val uiBooks = books.map { entity ->
                    BookUiModel(
                        id = entity.id,
                        title = entity.title,
                        author = entity.author,
                        coverPath = entity.coverPath,
                        progressPercent = entity.progressPercent,
                        isDownloaded = entity.isDownloaded,
                        isSample = entity.isSample,
                        isNew = entity.isNew,
                        collectionName = entity.collectionName,
                        lastReadTimestamp = entity.lastReadTimestamp,
                        isDoc = entity.collectionName == "Docs" || entity.filePath.contains("/docs/")
                    )
                }

                val savedViewMode = when (settings.libraryViewMode) {
                    "LIST" -> LibraryViewMode.LIST
                    "COLLECTIONS" -> LibraryViewMode.COLLECTIONS
                    else -> LibraryViewMode.GRID
                }

                val savedSortMode = when (settings.librarySortMode) {
                    "TITLE" -> LibrarySortMode.TITLE
                    "AUTHOR", "AUTHOR_AZ" -> LibrarySortMode.AUTHOR_AZ
                    "AUTHOR_ZA" -> LibrarySortMode.AUTHOR_ZA
                    else -> LibrarySortMode.RECENT
                }

                val savedFilter = when (settings.libraryFilter) {
                    "DOWNLOADED" -> LibraryFilter.DOWNLOADED
                    "UNREAD" -> LibraryFilter.UNREAD
                    "BOOKS" -> LibraryFilter.BOOKS
                    "SAMPLES" -> LibraryFilter.SAMPLES
                    "DOCS" -> LibraryFilter.DOCS
                    else -> LibraryFilter.ALL
                }

                _uiState.update { current ->
                    val updated = current.copy(
                        allBooks = uiBooks,
                        collections = collections,
                        viewMode = savedViewMode,
                        sortMode = savedSortMode,
                        activeFilter = savedFilter,
                        groupBySeries = settings.groupBySeries,
                        lastReadBook = uiBooks.filter { it.lastReadTimestamp > 0 }
                            .maxByOrNull { it.lastReadTimestamp }
                    )
                    updated.copy(filteredBooks = computeFilteredBooks(updated))
                }
            }
        }
    }

    private fun computeFilteredBooks(state: LibraryUiState): List<BookUiModel> {
        var result = state.allBooks

        // 1. Filter by collection if active
        if (state.activeCollection != null) {
            result = result.filter { it.collectionName == state.activeCollection }
        }

        // 2. Filter by status / filterState
        if (state.filterState.showRead || state.filterState.showUnread || state.filterState.showBooks || state.filterState.showSamples || state.filterState.showDocs) {
            if (state.filterState.showRead && !state.filterState.showUnread) {
                result = result.filter { it.isRead }
            } else if (!state.filterState.showRead && state.filterState.showUnread) {
                result = result.filter { !it.isRead }
            }
            val hasTypeFilter = state.filterState.showBooks || state.filterState.showSamples || state.filterState.showDocs
            if (hasTypeFilter) {
                result = result.filter {
                    (state.filterState.showBooks && !it.isSample && !it.isDoc) ||
                    (state.filterState.showSamples && it.isSample) ||
                    (state.filterState.showDocs && it.isDoc)
                }
            }
        } else {
            result = when (state.activeFilter) {
                LibraryFilter.ALL -> result
                LibraryFilter.DOWNLOADED -> result.filter { it.isDownloaded }
                LibraryFilter.UNREAD -> result.filter { !it.isRead }
                LibraryFilter.BOOKS -> result.filter { !it.isSample && !it.isDoc }
                LibraryFilter.SAMPLES -> result.filter { it.isSample }
                LibraryFilter.DOCS -> result.filter { it.isDoc }
            }
        }

        // 3. Filter by search query
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) || it.author.lowercase().contains(q)
            }
        }

        // 4. Sort
        return when (state.sortMode) {
            LibrarySortMode.RECENT -> result.sortedByDescending { it.lastReadTimestamp }
            LibrarySortMode.TITLE -> result.sortedBy { it.title.lowercase() }
            LibrarySortMode.AUTHOR_AZ -> result.sortedBy { it.author.lowercase() }
            LibrarySortMode.AUTHOR_ZA -> result.sortedByDescending { it.author.lowercase() }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current ->
            val updated = current.copy(searchQuery = query)
            updated.copy(filteredBooks = computeFilteredBooks(updated))
        }
    }

    fun onFilterChange(filter: LibraryFilter) {
        _uiState.update { current ->
            val updated = current.copy(activeFilter = filter)
            updated.copy(filteredBooks = computeFilteredBooks(updated))
        }
        viewModelScope.launch {
            settingsRepository.updateLibraryFilter(filter.name)
        }
    }

    fun onViewModeChange(mode: LibraryViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
        viewModelScope.launch {
            settingsRepository.updateLibraryViewMode(mode.name)
        }
    }

    fun onSortModeChange(mode: LibrarySortMode) {
        _uiState.update { current ->
            val updated = current.copy(sortMode = mode)
            updated.copy(filteredBooks = computeFilteredBooks(updated))
        }
        viewModelScope.launch {
            settingsRepository.updateLibrarySortMode(mode.name)
        }
    }

    fun setGroupBySeries(enabled: Boolean) {
        _uiState.update { it.copy(groupBySeries = enabled) }
        viewModelScope.launch {
            settingsRepository.updateGroupBySeries(enabled)
        }
    }

    fun onApplyFilterState(filterState: LibraryFilterState) {
        _uiState.update { current ->
            val updated = current.copy(filterState = filterState, activeSheet = null)
            updated.copy(filteredBooks = computeFilteredBooks(updated))
        }
    }

    fun onSelectCollection(collection: String?) {
        _uiState.update { current ->
            val updated = current.copy(activeCollection = collection)
            updated.copy(filteredBooks = computeFilteredBooks(updated))
        }
    }

    fun onOpenSheet(sheet: ActiveSheet, book: BookUiModel? = null) {
        _uiState.update { it.copy(activeSheet = sheet, selectedBookForMenu = book) }
    }

    fun onDismissSheet() {
        _uiState.update { it.copy(activeSheet = null, selectedBookForMenu = null) }
    }

    fun onToggleReadStatus(bookId: String) {
        viewModelScope.launch {
            val book = libraryRepository.getBook(bookId) ?: return@launch
            val newProgress = if (book.progressPercent >= 100f) 0f else 100f
            libraryRepository.updateProgress(
                id = bookId,
                spineIndex = if (newProgress == 0f) 0 else book.spineIndex,
                charOffset = if (newProgress == 0f) 0 else book.charOffset,
                progressPercent = newProgress
            )
            onDismissSheet()
        }
    }

    fun onToggleDownload(bookId: String) {
        viewModelScope.launch {
            val book = libraryRepository.getBook(bookId) ?: return@launch
            libraryRepository.saveBook(book.copy(isDownloaded = !book.isDownloaded))
            onDismissSheet()
        }
    }

    fun onDeleteBook(bookId: String) {
        viewModelScope.launch {
            libraryRepository.deleteBook(bookId)
            onDismissSheet()
        }
    }

    fun onAssignCollection(bookId: String, collectionName: String?) {
        viewModelScope.launch {
            libraryRepository.updateCollection(bookId, collectionName)
            onDismissSheet()
        }
    }

    fun onNewCollectionNameChanged(name: String) {
        _uiState.update { it.copy(newCollectionNameInput = name) }
    }

    fun onCreateCollection(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val selectedBook = _uiState.value.selectedBookForMenu
            if (selectedBook != null) {
                libraryRepository.updateCollection(selectedBook.id, trimmed)
            }
            _uiState.update { current ->
                val newCollections = if (trimmed in current.collections) current.collections else current.collections + trimmed
                current.copy(
                    collections = newCollections.sorted(),
                    newCollectionNameInput = "",
                    activeSheet = null,
                    selectedBookForMenu = null
                )
            }
        }
    }

    companion object {
        fun provideFactory(
            libraryRepository: LibraryRepository,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LibraryViewModel(libraryRepository, settingsRepository) as T
            }
        }
    }
}
