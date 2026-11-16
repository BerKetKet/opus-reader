package app.opusreader.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.opusreader.data.db.OpusDatabase
import app.opusreader.data.entity.AnnotationEntity
import app.opusreader.data.entity.AnnotationType
import app.opusreader.data.entity.BookEntity
import app.opusreader.data.entity.SpineItemEntity
import app.opusreader.data.entity.TocEntryEntity
import app.opusreader.data.importer.EpubImporter
import app.opusreader.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DatabaseRestartTest {

    @Test
    fun dataSurvivesProcessRestart() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dbFile = context.getDatabasePath("test_restart.db")
        if (dbFile.exists()) {
            dbFile.delete()
        }

        // 1. Initial process: open database, insert records, update state
        val db1 = Room.databaseBuilder(context, OpusDatabase::class.java, "test_restart.db").build()

        val book = BookEntity(
            id = "persisted-book-1",
            title = "Pride and Prejudice",
            author = "Jane Austen",
            filePath = "/fake/path/book.epub",
            coverPath = "/fake/path/cover.png",
            collectionName = "Classics"
        )
        val spine = listOf(
            SpineItemEntity("persisted-book-1#0", "persisted-book-1", "OEBPS/ch1.xhtml", "application/xhtml+xml", 0),
            SpineItemEntity("persisted-book-1#1", "persisted-book-1", "OEBPS/ch2.xhtml", "application/xhtml+xml", 1)
        )
        val toc = listOf(
            TocEntryEntity("persisted-book-1#toc#0", "persisted-book-1", "Chapter 1", "OEBPS/ch1.xhtml", 0, 0),
            TocEntryEntity("persisted-book-1#toc#1", "persisted-book-1", "Chapter 2", "OEBPS/ch2.xhtml", 1, 0)
        )
        val annotation = AnnotationEntity(
            id = "ann-1",
            bookId = "persisted-book-1",
            spineIndex = 0,
            startOffset = 20,
            endOffset = 80,
            selectedText = "It is a truth universally acknowledged",
            noteText = "Famous opening line",
            colorHex = "#FFEB3B",
            type = AnnotationType.NOTE
        )

        db1.bookDao().insertBook(book)
        db1.spineDao().insertSpineItems(spine)
        db1.tocDao().insertTocEntries(toc)
        db1.annotationDao().insertAnnotation(annotation)

        // Update reading position / progress
        db1.bookDao().updateProgress(
            id = "persisted-book-1",
            spineIndex = 1,
            charOffset = 350,
            progressPercent = 65.0f,
            lastReadTimestamp = 123456789L
        )

        // Close db1 to simulate process termination
        db1.close()

        // 2. Simulated process restart: open brand new database connection on the persisted file
        val db2 = Room.databaseBuilder(context, OpusDatabase::class.java, "test_restart.db").build()

        val loadedBook = db2.bookDao().getBookById("persisted-book-1")
        assertNotNull(loadedBook)
        assertEquals("Pride and Prejudice", loadedBook!!.title)
        assertEquals("Jane Austen", loadedBook.author)
        assertEquals("Classics", loadedBook.collectionName)
        assertEquals(1, loadedBook.spineIndex)
        assertEquals(350, loadedBook.charOffset)
        assertEquals(65.0f, loadedBook.progressPercent, 0.01f)
        assertEquals(123456789L, loadedBook.lastReadTimestamp)

        val loadedSpine = db2.spineDao().getSpineItemsForBook("persisted-book-1")
        assertEquals(2, loadedSpine.size)
        assertEquals("OEBPS/ch1.xhtml", loadedSpine[0].href)
        assertEquals("OEBPS/ch2.xhtml", loadedSpine[1].href)

        val loadedToc = db2.tocDao().getTocEntriesForBook("persisted-book-1")
        assertEquals(2, loadedToc.size)
        assertEquals("Chapter 1", loadedToc[0].title)

        val loadedAnnotations = db2.annotationDao().getAnnotationsForBook("persisted-book-1").first()
        assertEquals(1, loadedAnnotations.size)
        assertEquals("Famous opening line", loadedAnnotations[0].noteText)

        db2.close()
    }

    @Test
    fun epubImport_successAndErrorHandling() = runBlocking {
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

        val db = OpusDatabase.createInMemory(targetContext)
        val importer = EpubImporter(targetContext, db)

        // 1. Valid EPUB import
        testContext.assets.open("epubs/valid_book.epub").use { stream ->
            val result = importer.importFromStream(stream, "valid_book.epub")
            assertTrue(result.isSuccess)
            val book = result.getOrThrow()
            assertEquals("Pride and Prejudice", book.title)
            assertEquals("Jane Austen", book.author)

            // Confirm database records exist
            val dbBook = db.bookDao().getBookById(book.id)
            assertNotNull(dbBook)
            assertEquals("Pride and Prejudice", dbBook!!.title)

            val spine = db.spineDao().getSpineItemsForBook(book.id)
            assertEquals(2, spine.size)

            val toc = db.tocDao().getTocEntriesForBook(book.id)
            assertEquals(2, toc.size)

            // Confirm cover file exists on disk
            assertNotNull(book.coverPath)
            assertTrue(File(book.coverPath!!).exists())
        }

        // 2. Corrupt EPUB import gracefully fails
        testContext.assets.open("epubs/malformed_corrupt.epub").use { stream ->
            val result = importer.importFromStream(stream, "malformed_corrupt.epub")
            assertTrue(result.isFailure)
        }

        db.close()
    }

    @Test
    fun settingsSurviveProcessRestart() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val settingsRepo1 = SettingsRepository.create(context)
        settingsRepo1.updateLibraryViewMode("LIST")
        settingsRepo1.updateLibrarySortMode("AUTHOR_ZA")
        settingsRepo1.updateGroupBySeries(true)

        // Simulate process recreation: create a new repository instance pointing to the same DataStore
        val settingsRepo2 = SettingsRepository.create(context)
        val loaded = settingsRepo2.settingsFlow.first()
        assertEquals("LIST", loaded.libraryViewMode)
        assertEquals("AUTHOR_ZA", loaded.librarySortMode)
        assertTrue(loaded.groupBySeries)
    }
}
