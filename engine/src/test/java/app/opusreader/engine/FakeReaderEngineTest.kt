package app.opusreader.engine

import app.opusreader.engine.model.BookSource
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.LocatorRange
import app.opusreader.engine.model.ReaderSettings
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class FakeReaderEngineTest {

    private lateinit var validEpubFile: File
    private lateinit var minimalEpubFile: File

    @Before
    fun setUp() {
        validEpubFile = copyResourceToTempFile("valid_book.epub")
        minimalEpubFile = copyResourceToTempFile("minimal_book.epub")
    }

    private fun copyResourceToTempFile(name: String): File {
        val stream = javaClass.getResourceAsStream("/epubs/$name")
            ?: error("Resource not found: /epubs/$name")
        val temp = File.createTempFile("test_engine_", "_$name")
        temp.deleteOnExit()
        stream.use { input ->
            temp.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return temp
    }

    @Test
    fun open_validEpub_returnsAccurateBookInfoAndToc() = runTest {
        val engine = FakeReaderEngine()
        val bookInfo = engine.open(
            BookSource(id = "book_1", filePath = validEpubFile.absolutePath),
            ReaderSettings()
        )

        assertEquals("Pride and Prejudice", bookInfo.title)
        assertEquals("Jane Austen", bookInfo.author)
        assertEquals(2, bookInfo.totalSpines)

        val toc = engine.toc
        assertTrue(toc.isNotEmpty())
        assertEquals("Chapter 1", toc[0].title)
        assertEquals(0, toc[0].locator.spineIndex)
    }

    @Test
    fun pageNavigation_nextAndPrevious_traversesBookSeamlessly() = runTest {
        val engine = FakeReaderEngine()
        engine.open(
            BookSource(id = "book_1", filePath = validEpubFile.absolutePath),
            ReaderSettings()
        )

        val page1 = engine.pageAt(Locator(spineIndex = 0, charOffset = 0))
        assertNotNull(page1)
        assertEquals(0, page1.start.spineIndex)
        assertTrue(page1.words.isNotEmpty())

        val page2 = engine.next(page1)
        if (page2 != null) {
            val backToPage1 = engine.previous(page2)
            assertEquals(page1.start, backToPage1?.start)
        }

        // Previous on first page returns null
        val beforeFirst = engine.previous(page1)
        assertNull(beforeFirst)
    }

    @Test
    fun locatorPersistenceAndRestoration_survivesEngineRecreation() = runTest {
        val engine1 = FakeReaderEngine()
        engine1.open(
            BookSource(id = "book_1", filePath = validEpubFile.absolutePath),
            ReaderSettings()
        )

        val page1 = engine1.pageAt(Locator(0, 0))
        val nextPage = engine1.next(page1) ?: page1
        val savedLocator = nextPage.start

        // Simulate app kill / engine recreation
        val engine2 = FakeReaderEngine()
        engine2.open(
            BookSource(id = "book_1", filePath = validEpubFile.absolutePath),
            ReaderSettings()
        )

        val restoredPage = engine2.pageAt(savedLocator)
        assertEquals(nextPage.start, restoredPage.start)
        assertEquals(nextPage.end, restoredPage.end)
    }

    @Test
    fun progress_calculatesCumulativePosition() = runTest {
        val engine = FakeReaderEngine()
        engine.open(
            BookSource(id = "book_1", filePath = validEpubFile.absolutePath),
            ReaderSettings()
        )

        val firstPage = engine.pageAt(Locator(0, 0))
        val initialProgress = engine.progress(firstPage)
        assertEquals(0, initialProgress.current)
        assertEquals(0.0f, initialProgress.percent, 0.01f)
        assertTrue(initialProgress.total > 0)

        val secondSpinePage = engine.pageAt(Locator(1, 0))
        val spine2Progress = engine.progress(secondSpinePage)
        assertTrue(spine2Progress.current > initialProgress.current)
        assertTrue(spine2Progress.percent > initialProgress.percent)
    }

    @Test
    fun search_findsOccurrencesAcrossSpines() = runTest {
        val engine = FakeReaderEngine()
        engine.open(
            BookSource(id = "book_1", filePath = validEpubFile.absolutePath),
            ReaderSettings()
        )

        // "truth" is in Pride and Prejudice opening sentence
        val hits = engine.search("truth")
        assertTrue(hits.isNotEmpty())
        val firstHit = hits[0]
        assertEquals(0, firstHit.locator.spineIndex)
        assertTrue(firstHit.snippet.contains("truth", ignoreCase = true))
    }

    @Test
    fun rectsFor_returnsBoundingBoxesForLocatorRange() = runTest {
        val engine = FakeReaderEngine()
        engine.open(
            BookSource(id = "book_1", filePath = validEpubFile.absolutePath),
            ReaderSettings()
        )

        val page = engine.pageAt(Locator(0, 0))
        assertTrue(page.words.size >= 3)

        val word0 = page.words[0]
        val word2 = page.words[2]

        val range = LocatorRange(
            start = word0.start,
            end = word2.end
        )

        val rects = engine.rectsFor(page, range)
        assertEquals(3, rects.size)
    }

    @Test
    fun fallbackHandling_gracefullyPaginatesMissingOrNonExistentFile() = runTest {
        val engine = FakeReaderEngine()
        val info = engine.open(
            BookSource(id = "missing_book", filePath = "/non/existent/path.epub"),
            ReaderSettings()
        )

        assertNotNull(info.title)
        val page = engine.pageAt(Locator(0, 0))
        assertNotNull(page)
        assertTrue(page.words.isNotEmpty())
    }
}
