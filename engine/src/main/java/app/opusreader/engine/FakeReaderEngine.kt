package app.opusreader.engine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import app.opusreader.engine.internal.EpubExtractor
import app.opusreader.engine.internal.ExtractedBook
import app.opusreader.engine.internal.ExtractedSpine
import app.opusreader.engine.model.BookInfo
import app.opusreader.engine.model.BookSource
import app.opusreader.engine.model.LinkBox
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.LocatorRange
import app.opusreader.engine.model.Page
import app.opusreader.engine.model.PageTheme
import app.opusreader.engine.model.Progress
import app.opusreader.engine.model.ReaderSettings
import app.opusreader.engine.model.SearchHit
import app.opusreader.engine.model.TocEntry
import app.opusreader.engine.model.WordBox
import java.io.File
import kotlin.math.hypot

class FakeReaderEngine(
    private var viewportWidth: Int = 1080,
    private var viewportHeight: Int = 2202,
    private var marginHorizontal: Float = 75f,
    private var marginVertical: Float = 90f,
    private val defaultTypeface: Typeface? = null
) : ReaderEngine {

    val totalPages: Int get() = allPages.size

    private var currentBook: ExtractedBook? = null
    private var currentSettings: ReaderSettings = ReaderSettings()
    private val allPages = mutableListOf<Page>()
    private var totalCharacterCount: Int = 0
    private val spineCharOffsets = mutableListOf<Int>() // Cumulative char offset per spine

    private val isAndroidRuntime: Boolean by lazy {
        try {
            Class.forName("android.os.Build")
            android.os.Build.VERSION.SDK_INT > 0
        } catch (_: Throwable) {
            false
        }
    }

    fun setViewport(width: Int, height: Int, marginH: Float, marginV: Float) {
        this.viewportWidth = width
        this.viewportHeight = height
        this.marginHorizontal = marginH
        this.marginVertical = marginV
        if (currentBook != null) {
            paginate()
        }
    }

    override suspend fun open(book: BookSource, settings: ReaderSettings): BookInfo {
        this.currentSettings = settings
        val file = File(book.filePath)
        val extracted = EpubExtractor.extract(file)
        this.currentBook = extracted
        paginate()
        return extracted.info
    }

    override val toc: List<TocEntry>
        get() = currentBook?.toc ?: emptyList()

    private fun paginate() {
        allPages.clear()
        spineCharOffsets.clear()
        val book = currentBook ?: return

        var cumulative = 0
        book.spines.forEach { spine ->
            spineCharOffsets.add(cumulative)
            cumulative += spine.text.length
        }
        totalCharacterCount = cumulative

        book.spines.forEach { spine ->
            paginateSpine(spine)
        }

        if (allPages.isEmpty()) {
            allPages.add(
                Page(
                    start = Locator(0, 0),
                    end = Locator(0, 0),
                    words = emptyList(),
                    links = emptyList()
                )
            )
        }
    }

    private fun paginateSpine(spine: ExtractedSpine) {
        val text = spine.text
        if (text.isBlank()) {
            allPages.add(
                Page(
                    start = Locator(spine.index, 0),
                    end = Locator(spine.index, 0),
                    words = emptyList(),
                    links = emptyList()
                )
            )
            return
        }

        val activeTypeface = currentSettings.typeface ?: defaultTypeface ?: Typeface.SERIF
        val fontSizePx = currentSettings.fontSizeSp * 2.625f
        val lineHeight = fontSizePx * currentSettings.lineSpacingMultiplier
        val contentWidth = (viewportWidth - (2 * marginHorizontal)).coerceAtLeast(100f)
        val contentHeight = (viewportHeight - (2 * marginVertical)).coerceAtLeast(100f)

        // Try measuring with Paint if available
        val paint = try {
            Paint().apply {
                textSize = fontSizePx
                typeface = activeTypeface
            }
        } catch (_: Throwable) {
            null
        }

        val testMeasure = try { paint?.measureText("M") ?: 0f } catch (_: Throwable) { 0f }
        val usePaintMetrics = testMeasure > 0.1f

        val tfFactor = when {
            activeTypeface != null && activeTypeface == Typeface.SANS_SERIF -> 0.48f
            activeTypeface != null && activeTypeface == Typeface.MONOSPACE -> 0.62f
            currentSettings.fontName.contains("Ember", ignoreCase = true) || currentSettings.fontName.contains("Sans", ignoreCase = true) -> 0.48f
            currentSettings.fontName.contains("Mono", ignoreCase = true) -> 0.62f
            activeTypeface != null && activeTypeface != Typeface.SERIF -> 0.56f
            else -> 0.52f
        }
        val charWidth = if (usePaintMetrics) 0f else (fontSizePx * tfFactor)
        val spaceWidth = if (usePaintMetrics) paint!!.measureText(" ") else (fontSizePx * 0.28f)
        val ascent = if (usePaintMetrics) kotlin.math.abs(paint!!.fontMetrics.ascent) else (fontSizePx * 0.8f)
        val descent = if (usePaintMetrics) paint!!.fontMetrics.descent else (fontSizePx * 0.2f)

        var currentX = marginHorizontal
        var currentY = marginVertical + lineHeight
        var pageStartOffset = 0
        val pageWords = mutableListOf<WordBox>()

        // Tokenize words with offsets
        val regex = Regex("\\S+")
        val matches = regex.findAll(text).toList()

        for (match in matches) {
            val wordText = match.value
            val wordStart = match.range.first
            val wordEnd = match.range.last + 1

            val wordWidth = if (usePaintMetrics) paint!!.measureText(wordText) else (wordText.length * charWidth)

            // Check line wrap
            if (currentX + wordWidth > marginHorizontal + contentWidth && pageWords.isNotEmpty() && currentX > marginHorizontal) {
                currentX = marginHorizontal
                currentY += lineHeight
            }

            // Check page overflow
            if (currentY + descent > marginVertical + contentHeight && pageWords.isNotEmpty()) {
                val pageEndOffset = pageWords.last().end.charOffset
                allPages.add(
                    Page(
                        start = Locator(spine.index, pageStartOffset),
                        end = Locator(spine.index, pageEndOffset),
                        words = ArrayList(pageWords),
                        links = emptyList()
                    )
                )
                pageWords.clear()
                pageStartOffset = wordStart
                currentX = marginHorizontal
                currentY = marginVertical + lineHeight
            }

            val rect = RectF().apply {
                left = currentX
                top = currentY - ascent
                right = currentX + wordWidth
                bottom = currentY + descent
            }

            pageWords.add(
                WordBox(
                    text = wordText,
                    rect = rect,
                    start = Locator(spine.index, wordStart),
                    end = Locator(spine.index, wordEnd)
                )
            )

            currentX += wordWidth + spaceWidth
        }

        if (pageWords.isNotEmpty()) {
            allPages.add(
                Page(
                    start = Locator(spine.index, pageStartOffset),
                    end = Locator(spine.index, text.length),
                    words = ArrayList(pageWords),
                    links = emptyList()
                )
            )
        }
    }

    override fun pageAt(locator: Locator): Page {
        if (allPages.isEmpty()) {
            return Page(locator, locator, emptyList(), emptyList())
        }

        val spinePages = allPages.filter { it.start.spineIndex == locator.spineIndex }
        if (spinePages.isEmpty()) {
            // Clamp to closest spine
            return if (locator.spineIndex < allPages.first().start.spineIndex) {
                allPages.first()
            } else {
                allPages.last()
            }
        }

        for (page in spinePages) {
            if (locator.charOffset >= page.start.charOffset && locator.charOffset < page.end.charOffset) {
                return page
            }
        }

        return if (locator.charOffset >= spinePages.last().end.charOffset) {
            spinePages.last()
        } else {
            spinePages.first()
        }
    }

    override fun next(page: Page): Page? {
        val index = allPages.indexOfFirst {
            it.start == page.start && it.end == page.end
        }
        return if (index != -1 && index + 1 < allPages.size) {
            allPages[index + 1]
        } else {
            null
        }
    }

    override fun previous(page: Page): Page? {
        val index = allPages.indexOfFirst {
            it.start == page.start && it.end == page.end
        }
        return if (index > 0) {
            allPages[index - 1]
        } else {
            null
        }
    }

    override fun progress(page: Page): Progress {
        val spineIdx = page.start.spineIndex
        val baseOffset = spineCharOffsets.getOrElse(spineIdx) { 0 }
        val currentOffset = (baseOffset + page.start.charOffset).coerceAtMost(totalCharacterCount)
        val percent = if (totalCharacterCount > 0) {
            (currentOffset.toFloat() / totalCharacterCount.toFloat()) * 100f
        } else 0f

        return Progress(
            current = currentOffset,
            total = totalCharacterCount,
            percent = percent
        )
    }

    override fun hitTest(page: Page, x: Float, y: Float): Locator? {
        if (page.words.isEmpty()) return page.start

        // 1. Direct bounding box hit
        for (word in page.words) {
            if (containsPoint(word.rect, x, y)) {
                val width = (word.rect.right - word.rect.left).coerceAtLeast(1f)
                val fraction = ((x - word.rect.left) / width).coerceIn(0f, 1f)
                val charIndex = (fraction * word.text.length).toInt().coerceIn(0, word.text.length)
                return Locator(word.start.spineIndex, word.start.charOffset + charIndex)
            }
        }

        // 2. Proximity hit (find nearest word within 100px)
        var closestWord: WordBox? = null
        var minDistance = Float.MAX_VALUE

        for (word in page.words) {
            val cx = (word.rect.left + word.rect.right) / 2f
            val cy = (word.rect.top + word.rect.bottom) / 2f
            val dist = hypot(x - cx, y - cy)
            if (dist < minDistance) {
                minDistance = dist
                closestWord = word
            }
        }

        return if (closestWord != null && minDistance < 100f) {
            closestWord.start
        } else {
            null
        }
    }

    override fun rectsFor(page: Page, range: LocatorRange): List<RectF> {
        if (range.start.spineIndex != page.start.spineIndex) return emptyList()

        return page.words.filter { word ->
            word.start.charOffset < range.end.charOffset && word.end.charOffset > range.start.charOffset
        }.map { it.rect }
    }

    override fun textFor(page: Page, range: LocatorRange): String {
        if (range.start.spineIndex != page.start.spineIndex) return ""
        val matchingWords = page.words.filter { word ->
            word.start.charOffset < range.end.charOffset && word.end.charOffset > range.start.charOffset
        }
        return matchingWords.joinToString(" ") { it.text }
    }

    override fun draw(page: Page, canvas: Canvas, theme: PageTheme) {
        canvas.drawColor(theme.backgroundColor)

        val activeTypeface = currentSettings.typeface ?: defaultTypeface ?: Typeface.SERIF
        val paint = Paint().apply {
            color = theme.textColor
            textSize = currentSettings.fontSizeSp * 2.625f
            isAntiAlias = true
            try {
                typeface = activeTypeface
            } catch (_: Throwable) { }
        }

        val descent = paint.fontMetrics?.descent ?: (paint.textSize * 0.2f)

        for (word in page.words) {
            canvas.drawText(
                word.text,
                word.rect.left,
                word.rect.bottom - descent,
                paint
            )
        }
    }

    override suspend fun search(query: String): List<SearchHit> {
        if (query.isBlank()) return emptyList()
        val book = currentBook ?: return emptyList()
        val hits = mutableListOf<SearchHit>()

        book.spines.forEach { spine ->
            val text = spine.text
            var startIndex = 0
            while (startIndex < text.length) {
                val found = text.indexOf(query, startIndex, ignoreCase = true)
                if (found == -1) break

                val snippetStart = (found - 30).coerceAtLeast(0)
                val snippetEnd = (found + query.length + 30).coerceAtMost(text.length)
                val snippet = text.substring(snippetStart, snippetEnd)
                    .replace('\n', ' ')
                    .trim()

                hits.add(
                    SearchHit(
                        locator = Locator(spine.index, found),
                        snippet = "...$snippet..."
                    )
                )
                startIndex = found + query.length
            }
        }
        return hits
    }

    private fun containsPoint(rect: RectF, x: Float, y: Float): Boolean {
        return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom
    }
}
