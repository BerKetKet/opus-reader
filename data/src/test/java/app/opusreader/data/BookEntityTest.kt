package app.opusreader.data

import app.opusreader.data.entity.BookEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class BookEntityTest {
    @Test
    fun defaultValuesAreExpected() {
        val book = BookEntity(
            id = "test-1",
            title = "Test Book",
            author = "Test Author",
            filePath = "/path/test.epub"
        )
        assertEquals("test-1", book.id)
        assertEquals(0, book.spineIndex)
        assertEquals(0, book.charOffset)
        assertEquals(true, book.isDownloaded)
    }
}
