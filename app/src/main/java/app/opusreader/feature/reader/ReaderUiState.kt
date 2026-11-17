package app.opusreader.feature.reader

import app.opusreader.engine.model.Page
import app.opusreader.engine.model.PageTheme
import app.opusreader.engine.model.Progress
import app.opusreader.engine.model.TocEntry

enum class ActiveReaderSheet {
    CONTENTS,
    SEARCH,
    SETTINGS,
    OVERFLOW
}

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
    val isBookmarked: Boolean = false,
    val theme: PageTheme = PageTheme(textColor = 0xFFE0E0E0.toInt(), backgroundColor = 0xFF000000.toInt()),
    val activeSheet: ActiveReaderSheet? = null,
    val tocEntries: List<TocEntry> = emptyList(),
    val scrubberLocation: Int = 1,
    val totalLocations: Int = 1,
    val isLoading: Boolean = true
)
