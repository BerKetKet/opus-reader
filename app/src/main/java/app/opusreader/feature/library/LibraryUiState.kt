package app.opusreader.feature.library

enum class LibraryViewMode {
    GRID,
    LIST,
    COLLECTIONS
}

enum class LibrarySortMode {
    RECENT,
    TITLE,
    AUTHOR_AZ,
    AUTHOR_ZA;

    companion object {
        val AUTHOR = AUTHOR_AZ
    }
}

data class LibraryFilterState(
    val showRead: Boolean = false,
    val showUnread: Boolean = false,
    val showBooks: Boolean = false,
    val showSamples: Boolean = false,
    val showDocs: Boolean = false
)

enum class LibraryFilter {
    ALL,
    DOWNLOADED,
    UNREAD,
    BOOKS,
    SAMPLES,
    DOCS
}

enum class ActiveSheet {
    FILTER,
    VIEW_SORT,
    BOOK_MENU,
    CREATE_COLLECTION
}

data class BookUiModel(
    val id: String,
    val title: String,
    val author: String,
    val coverPath: String?,
    val progressPercent: Float,
    val isDownloaded: Boolean,
    val isSample: Boolean,
    val isNew: Boolean,
    val collectionName: String?,
    val lastReadTimestamp: Long,
    val isRead: Boolean = progressPercent >= 1.0f,
    val isDoc: Boolean = false,
    val fileSize: String = "15 MB"
)

data class LibraryUiState(
    val allBooks: List<BookUiModel> = emptyList(),
    val filteredBooks: List<BookUiModel> = emptyList(),
    val collections: List<String> = emptyList(),
    val activeCollection: String? = null,
    val searchQuery: String = "",
    val activeFilter: LibraryFilter = LibraryFilter.ALL,
    val filterState: LibraryFilterState = LibraryFilterState(),
    val viewMode: LibraryViewMode = LibraryViewMode.GRID,
    val sortMode: LibrarySortMode = LibrarySortMode.RECENT,
    val groupBySeries: Boolean = false,
    val lastReadBook: BookUiModel? = null,
    val activeSheet: ActiveSheet? = null,
    val selectedBookForMenu: BookUiModel? = null,
    val newCollectionNameInput: String = ""
)
