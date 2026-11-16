package app.opusreader.data

import app.opusreader.data.entity.AnnotationEntity
import app.opusreader.data.entity.AnnotationType
import app.opusreader.data.entity.BookEntity
import app.opusreader.data.importer.EpubParser
import app.opusreader.data.importer.MalformedEpubException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.InputStream

class BookEntityTest {

    private fun loadResource(name: String): InputStream {
        return javaClass.getResourceAsStream("/epubs/$name")
            ?: error("Resource not found: /epubs/$name")
    }

    @Test
    fun validEpub_parsesMetadataSpineAndTocCorrectly() {
        val stream = loadResource("valid_book.epub")
        val parsed = EpubParser.parse(stream, "/data/test/valid_book.epub")

        assertEquals("Pride and Prejudice", parsed.book.title)
        assertEquals("Jane Austen", parsed.book.author)
        assertEquals(2, parsed.spineItems.size)
        assertEquals(2, parsed.tocEntries.size)

        assertEquals("Chapter 1", parsed.tocEntries[0].title)
        assertEquals("Chapter 2", parsed.tocEntries[1].title)

        assertNotNull(parsed.coverEntryPath)
        assertNotNull(parsed.coverBytes)
        assertTrue(parsed.coverBytes!!.isNotEmpty())
    }

    @Test
    fun minimalEpub_parsesCleanlyWithoutCoverOrNcx() {
        val stream = loadResource("minimal_book.epub")
        val parsed = EpubParser.parse(stream, "/data/test/minimal_book.epub")

        assertEquals("Minimal Book", parsed.book.title)
        assertEquals("Anonymous", parsed.book.author)
        assertEquals(1, parsed.spineItems.size)
        assertEquals(1, parsed.tocEntries.size)
        assertNull(parsed.coverEntryPath)
        assertNull(parsed.coverBytes)
    }

    @Test
    fun malformedEpub_corruptZip_throwsGracefully() {
        val stream = loadResource("malformed_corrupt.epub")
        try {
            EpubParser.parse(stream, "/data/test/malformed_corrupt.epub")
            fail("Expected MalformedEpubException for corrupt zip")
        } catch (e: MalformedEpubException) {
            assertTrue(e.message!!.contains("Corrupt") || e.message!!.contains("invalid"))
        }
    }

    @Test
    fun malformedEpub_missingContainer_throwsGracefully() {
        val stream = loadResource("malformed_missing_container.epub")
        try {
            EpubParser.parse(stream, "/data/test/malformed_missing_container.epub")
            fail("Expected MalformedEpubException for missing container")
        } catch (e: MalformedEpubException) {
            assertTrue(e.message!!.contains("Missing META-INF/container.xml"))
        }
    }

    @Test
    fun malformedEpub_missingOpf_throwsGracefully() {
        val stream = loadResource("malformed_missing_opf.epub")
        try {
            EpubParser.parse(stream, "/data/test/malformed_missing_opf.epub")
            fail("Expected MalformedEpubException for missing OPF")
        } catch (e: MalformedEpubException) {
            assertTrue(e.message!!.contains("OPF package document not found"))
        }
    }

    @Test
    fun annotationEntity_creationAndDefaults() {
        val annotation = AnnotationEntity(
            id = "ann-1",
            bookId = "book-1",
            spineIndex = 0,
            startOffset = 10,
            endOffset = 50,
            selectedText = "Sample text",
            type = AnnotationType.HIGHLIGHT
        )
        assertEquals("ann-1", annotation.id)
        assertEquals("#FFEB3B", annotation.colorHex)
        assertNull(annotation.noteText)
    }
}
