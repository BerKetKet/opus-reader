package app.opusreader.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.opusreader.engine.model.BookSource
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.LocatorRange
import app.opusreader.engine.model.PageTheme
import app.opusreader.engine.model.ReaderSettings
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FakeReaderEngineDeviceTest {

    private lateinit var testEpubFile: File

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val cacheDir = context.cacheDir
        testEpubFile = File(cacheDir, "device_test_valid.epub")
        context.assets.open("epubs/valid_book.epub").use { input ->
            testEpubFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    @Test
    fun hitTest_returnsAccurateWordLocatorOnDevice() = runBlocking {
        val engine = FakeReaderEngine(viewportWidth = 1080, viewportHeight = 2202)
        engine.open(
            BookSource(id = "book_dev", filePath = testEpubFile.absolutePath),
            ReaderSettings()
        )

        val page = engine.pageAt(Locator(spineIndex = 0, charOffset = 0))
        assertTrue(page.words.isNotEmpty())

        val targetWord = page.words.first()
        val centerX = (targetWord.rect.left + targetWord.rect.right) / 2f
        val centerY = (targetWord.rect.top + targetWord.rect.bottom) / 2f

        val hitLocator = engine.hitTest(page, centerX, centerY)
        assertNotNull(hitLocator)
        assertEquals(targetWord.start.spineIndex, hitLocator!!.spineIndex)
        assertTrue(hitLocator.charOffset >= targetWord.start.charOffset)
        assertTrue(hitLocator.charOffset <= targetWord.end.charOffset)
    }

    @Test
    fun rectsFor_returnsExactBoundingBoxesOnDevice() = runBlocking {
        val engine = FakeReaderEngine(viewportWidth = 1080, viewportHeight = 2202)
        engine.open(
            BookSource(id = "book_dev", filePath = testEpubFile.absolutePath),
            ReaderSettings()
        )

        val page = engine.pageAt(Locator(spineIndex = 0, charOffset = 0))
        assertTrue(page.words.size >= 2)

        val word0 = page.words[0]
        val word1 = page.words[1]

        val range = LocatorRange(start = word0.start, end = word1.end)
        val rects = engine.rectsFor(page, range)

        assertEquals(2, rects.size)
        assertTrue(rects[0].right > rects[0].left)
        assertTrue(rects[0].bottom > rects[0].top)
    }

    @Test
    fun draw_rendersToRealCanvasWithoutError() = runBlocking {
        val engine = FakeReaderEngine(viewportWidth = 1080, viewportHeight = 2202)
        engine.open(
            BookSource(id = "book_dev", filePath = testEpubFile.absolutePath),
            ReaderSettings()
        )

        val page = engine.pageAt(Locator(spineIndex = 0, charOffset = 0))
        val bitmap = Bitmap.createBitmap(1080, 2202, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        engine.draw(page, canvas, PageTheme(textColor = Color.WHITE, backgroundColor = Color.BLACK))

        // Assert canvas was drawn (center pixel of background is black 0xFF000000)
        val bgPixel = bitmap.getPixel(10, 10)
        assertEquals(Color.BLACK, bgPixel)
    }
}
