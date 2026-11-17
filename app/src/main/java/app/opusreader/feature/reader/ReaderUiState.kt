package app.opusreader.feature.reader

import app.opusreader.engine.model.LocatorRange
import app.opusreader.engine.model.Page
import app.opusreader.engine.model.PageTheme
import app.opusreader.engine.model.Progress
import app.opusreader.engine.model.SearchHit
import app.opusreader.engine.model.TocEntry

enum class ActiveReaderSheet {
    CONTENTS,
    SEARCH,
    SETTINGS,
    OVERFLOW,
    ANNOTATIONS,
    ANNOTATIONS_FILTER,
    ANNOTATIONS_SORT,
    ANNOTATIONS_SHARE
}

enum class AnnotationFilter {
    ALL,
    HIGHLIGHTS,
    NOTES,
    BOOKMARKS,
    STARRED
}

enum class AnnotationSort {
    LOCATION_ASC,
    LOCATION_DESC,
    DATE_MODIFIED
}

enum class AnnotationUiType {
    HIGHLIGHT,
    NOTE,
    BOOKMARK
}

data class AnnotationUiModel(
    val id: String,
    val bookId: String,
    val spineIndex: Int,
    val startOffset: Int,
    val endOffset: Int,
    val selectedText: String,
    val noteText: String? = null,
    val colorHex: String = "#FFEB3B",
    val isFavorite: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val type: AnnotationUiType = AnnotationUiType.HIGHLIGHT
)

data class ReaderBookUiModel(
    val id: String,
    val title: String,
    val author: String,
    val filePath: String,
    val coverPath: String? = null,
    val spineIndex: Int = 0,
    val charOffset: Int = 0,
    val progressPercent: Float = 0f
)

data class ReaderUiState(
    val book: ReaderBookUiModel? = null,
    val currentPage: Page? = null,
    val progress: Progress = Progress(current = 1, total = 1, percent = 0f),
    val isChromeVisible: Boolean = false,
    val isThumbnailsVisible: Boolean = false,
    val isBookmarked: Boolean = false,
    val theme: PageTheme = PageTheme(textColor = 0xFFE0E0E0.toInt(), backgroundColor = 0xFF000000.toInt()),
    val activeSheet: ActiveReaderSheet? = null,
    val tocEntries: List<TocEntry> = emptyList(),
    val scrubberLocation: Int = 1,
    val totalLocations: Int = 1,
    val isLoading: Boolean = true,
    // S06 Selection, Annotations, Search
    val annotations: List<AnnotationUiModel> = emptyList(),
    val selectedRange: LocatorRange? = null,
    val selectedText: String? = null,
    val activeAnnotation: AnnotationUiModel? = null,
    val isNoteEditorVisible: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<SearchHit> = emptyList(),
    val isSearching: Boolean = false,
    val activeSearchHitIndex: Int = -1,
    val annotationsFilter: AnnotationFilter = AnnotationFilter.ALL,
    val annotationsSort: AnnotationSort = AnnotationSort.LOCATION_ASC
)
