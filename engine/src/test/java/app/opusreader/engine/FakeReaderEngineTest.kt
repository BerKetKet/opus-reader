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

    @Test
    fun fontSizeAndTypefaceChanges_alterPageCountsAndWordBoxes() = runTest {
        val synthFile = createSynthetic500PageEpub()
        val engine = FakeReaderEngine()

        // 1. Test Font Size changes (14sp vs 24sp) on multi-page book
        engine.open(
            BookSource(id = "synth_font", filePath = synthFile.absolutePath),
            ReaderSettings(fontSizeSp = 14f)
        )
        val pages14 = engine.totalPages
        val wordBox14 = engine.pageAt(Locator(0, 0)).words.first().rect

        engine.open(
            BookSource(id = "synth_font", filePath = synthFile.absolutePath),
            ReaderSettings(fontSizeSp = 24f)
        )
        val pages24 = engine.totalPages
        val wordBox24 = engine.pageAt(Locator(0, 0)).words.first().rect

        val h14 = wordBox14.bottom - wordBox14.top
        val w14 = wordBox14.right - wordBox14.left
        val h24 = wordBox24.bottom - wordBox24.top
        val w24 = wordBox24.right - wordBox24.left

        assertTrue("Larger font size must yield more pages (pages24: $pages24, pages14: $pages14)", pages24 > pages14)
        assertTrue("Larger font size must yield taller word box ($h24 vs $h14)", h24 > h14)
        assertTrue("Larger font size must yield wider word box ($w24 vs $w14)", w24 > w14)

        // 2. Test Typeface changes (SERIF/Bookerly vs SANS_SERIF/Amazon Ember)
        engine.open(
            BookSource(id = "synth_font", filePath = synthFile.absolutePath),
            ReaderSettings(fontSizeSp = 16f, fontName = "Bookerly", typeface = android.graphics.Typeface.SERIF)
        )
        val wordBoxSerif = engine.pageAt(Locator(0, 0)).words.first().rect
        val wSerif = wordBoxSerif.right - wordBoxSerif.left

        engine.open(
            BookSource(id = "synth_font", filePath = synthFile.absolutePath),
            ReaderSettings(fontSizeSp = 16f, fontName = "Amazon Ember", typeface = android.graphics.Typeface.SANS_SERIF)
        )
        val wordBoxSans = engine.pageAt(Locator(0, 0)).words.first().rect
        val wSans = wordBoxSans.right - wordBoxSans.left

        assertTrue("Different typeface must yield different word box dimensions ($wSerif vs $wSans)", wSerif != wSans)
    }

    @Test
    fun timing_synthetic500PageEpubPagination() = runTest {
        val synthFile = createSynthetic500PageEpub()
        val engine = FakeReaderEngine()

        val start = System.currentTimeMillis()
        val info = engine.open(
            BookSource(id = "synth_500", filePath = synthFile.absolutePath),
            ReaderSettings(fontSizeSp = 16f)
        )
        val elapsedMs = System.currentTimeMillis() - start

        println("TIMING: Synthetic 500-page EPUB (${engine.totalPages} pages) paginated in ${elapsedMs}ms")
        assertTrue("Expected book info title", info.title.isNotBlank())
        assertTrue("Expected approximately 500 pages, got ${engine.totalPages}", engine.totalPages in 400..600)
        assertTrue("Pagination of 500 pages should take under 5000ms, took ${elapsedMs}ms", elapsedMs < 5000)
    }

    private fun createSynthetic500PageEpub(): File {
        val temp = File.createTempFile("synthetic_500_", ".epub")
        temp.deleteOnExit()
        val numChapters = 20
        val paragraphText = (1..100).joinToString(" ") {
            "The quick brown fox jumps over the lazy dog and runs across the wide open green meadow under the warm autumn sun."
        }

        java.util.zip.ZipOutputStream(temp.outputStream()).use { zos ->
            val entryMime = java.util.zip.ZipEntry("mimetype").apply {
                method = java.util.zip.ZipEntry.STORED
                size = "application/epub+zip".toByteArray().size.toLong()
                crc = java.util.zip.CRC32().apply { update("application/epub+zip".toByteArray()) }.value
            }
            zos.putNextEntry(entryMime)
            zos.write("application/epub+zip".toByteArray())
            zos.closeEntry()

            zos.putNextEntry(java.util.zip.ZipEntry("META-INF/container.xml"))
            zos.write("""<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
    <rootfiles>
        <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
    </rootfiles>
</container>""".toByteArray())
            zos.closeEntry()

            val manifestItems = (1..numChapters).joinToString("\n") {
                """<item id="ch$it" href="ch$it.xhtml" media-type="application/xhtml+xml"/>"""
            }
            val spineItems = (1..numChapters).joinToString("\n") {
                """<itemref idref="ch$it"/>"""
            }
            val opfContent = """<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="http://www.idpf.org/2007/opf" unique-identifier="BookID" version="2.0">
    <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
        <dc:title>Synthetic 500 Page Book</dc:title>
        <dc:creator>Test Generator</dc:creator>
        <dc:identifier id="BookID">urn:uuid:synthetic-500-pages</dc:identifier>
        <dc:language>en</dc:language>
    </metadata>
    <manifest>
        $manifestItems
    </manifest>
    <spine>
        $spineItems
    </spine>
</package>"""
            zos.putNextEntry(java.util.zip.ZipEntry("OEBPS/content.opf"))
            zos.write(opfContent.toByteArray())
            zos.closeEntry()

            for (ch in 1..numChapters) {
                zos.putNextEntry(java.util.zip.ZipEntry("OEBPS/ch$ch.xhtml"))
                val body = "<h1>Chapter $ch</h1><p>$paragraphText</p><p>$paragraphText</p><p>$paragraphText</p>"
                val xhtml = """<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head><title>Chapter $ch</title></head>
<body>$body</body>
</html>"""
                zos.write(xhtml.toByteArray())
                zos.closeEntry()
            }
        }
        return temp
    }
}
