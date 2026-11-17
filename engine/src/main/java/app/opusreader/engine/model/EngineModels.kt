package app.opusreader.engine.model

import android.graphics.Canvas
import android.graphics.RectF

data class Locator(
    val spineIndex: Int,
    val charOffset: Int
)

data class LocatorRange(
    val start: Locator,
    val end: Locator
)

data class WordBox(
    val text: String,
    val rect: RectF,
    val start: Locator,
    val end: Locator
)

data class LinkBox(
    val target: String,
    val rect: RectF
)

data class Page(
    val start: Locator,
    val end: Locator,
    val words: List<WordBox>,
    val links: List<LinkBox>
)

data class BookSource(
    val id: String,
    val filePath: String
)

data class ReaderSettings(
    val fontSizeSp: Float = 16f,
    val lineSpacingMultiplier: Float = 1.3f,
    val fontName: String = "Bookerly",
    val typeface: android.graphics.Typeface? = null
)

data class BookInfo(
    val id: String,
    val title: String,
    val author: String,
    val totalSpines: Int
)

data class Progress(
    val current: Int,
    val total: Int,
    val percent: Float
)

data class PageTheme(
    val textColor: Int,
    val backgroundColor: Int
)

data class SearchHit(
    val locator: Locator,
    val snippet: String
)

data class TocEntry(
    val title: String,
    val locator: Locator,
    val level: Int = 0
)
