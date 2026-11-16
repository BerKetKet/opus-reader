package app.opusreader.engine

import android.graphics.Canvas
import android.graphics.RectF
import app.opusreader.engine.model.BookInfo
import app.opusreader.engine.model.BookSource
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.LocatorRange
import app.opusreader.engine.model.Page
import app.opusreader.engine.model.PageTheme
import app.opusreader.engine.model.Progress
import app.opusreader.engine.model.ReaderSettings
import app.opusreader.engine.model.SearchHit
import app.opusreader.engine.model.TocEntry

interface ReaderEngine {
    suspend fun open(book: BookSource, settings: ReaderSettings): BookInfo
    fun pageAt(locator: Locator): Page
    fun next(page: Page): Page?
    fun previous(page: Page): Page?
    fun progress(page: Page): Progress
    fun hitTest(page: Page, x: Float, y: Float): Locator?
    fun rectsFor(page: Page, range: LocatorRange): List<RectF>
    fun draw(page: Page, canvas: Canvas, theme: PageTheme)
    suspend fun search(query: String): List<SearchHit>
    val toc: List<TocEntry>
}
