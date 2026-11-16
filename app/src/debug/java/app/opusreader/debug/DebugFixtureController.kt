package app.opusreader.debug

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import app.opusreader.OpusApplication
import app.opusreader.data.db.OpusDatabase
import app.opusreader.data.entity.BookEntity
import app.opusreader.data.repository.SettingsRepository
import app.opusreader.feature.library.ActiveSheet
import app.opusreader.feature.library.LibrarySortMode
import app.opusreader.feature.library.LibraryViewModel
import app.opusreader.feature.library.LibraryViewMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DebugFixtureController {

    fun handleFixtureDeepLink(context: Context, fixtureUri: String) {
        android.util.Log.d("OpusDebug", "handleFixtureDeepLink received: $fixtureUri")
        val uri = Uri.parse(fixtureUri)
        val fixtureId = uri.lastPathSegment?.uppercase() ?: return
        val isLiveness = uri.getQueryParameter("liveness") == "1"

        val app = context.applicationContext as OpusApplication
        val db = OpusDatabase.getInstance(app)
        val bookDao = db.bookDao()
        val settingsRepo = app.container.settingsRepository

        CoroutineScope(Dispatchers.IO).launch {
            when (fixtureId) {
                "D01", "D07" -> {
                    // Seed standard 12 reference books
                    val books = createD01Books(isLiveness)
                    seedBooks(bookDao, books)
                    val mode = if (fixtureId == "D07") "LIST" else "GRID"
                    settingsRepo.updateLibraryViewMode(mode)
                    settingsRepo.updateLibrarySortMode("RECENT")
                    settingsRepo.updateGroupBySeries(false)
                }
                "D05" -> {
                    // D05 Filter sheet: 16 Read, 59 Unread, 0 Books, 5 Samples, 70 Docs
                    val books = createD05Books(isLiveness)
                    seedBooks(bookDao, books)
                    settingsRepo.updateLibraryViewMode("GRID")
                    settingsRepo.updateLibrarySortMode("RECENT")
                    settingsRepo.updateGroupBySeries(false)
                }
                "D06" -> {
                    // D06 View & Sort sheet
                    val books = createD01Books(false)
                    seedBooks(bookDao, books)
                    settingsRepo.updateLibraryViewMode("GRID")
                    settingsRepo.updateLibrarySortMode(if (isLiveness) "TITLE" else "RECENT")
                    settingsRepo.updateGroupBySeries(isLiveness)
                }
                "D08" -> {
                    // D08 Collections (empty): no collections unless liveness
                    val books = createD08Books(isLiveness)
                    seedBooks(bookDao, books)
                    settingsRepo.updateLibraryViewMode("COLLECTIONS")
                    settingsRepo.updateLibrarySortMode("RECENT")
                    settingsRepo.updateGroupBySeries(false)
                }
            }

            withContext(Dispatchers.Main) {
                val activity = context as? ComponentActivity ?: return@withContext
                try {
                    val viewModel = ViewModelProvider(activity)[LibraryViewModel::class.java]
                    when (fixtureId) {
                        "D01" -> {
                            viewModel.onViewModeChange(LibraryViewMode.GRID)
                            viewModel.onSortModeChange(LibrarySortMode.RECENT)
                            viewModel.setGroupBySeries(false)
                            viewModel.onDismissSheet()
                        }
                        "D05" -> {
                            viewModel.onViewModeChange(LibraryViewMode.GRID)
                            viewModel.onOpenSheet(ActiveSheet.FILTER, null)
                        }
                        "D06" -> {
                            viewModel.onViewModeChange(LibraryViewMode.GRID)
                            viewModel.onSortModeChange(if (isLiveness) LibrarySortMode.TITLE else LibrarySortMode.RECENT)
                            viewModel.setGroupBySeries(isLiveness)
                            viewModel.onOpenSheet(ActiveSheet.VIEW_SORT, null)
                        }
                        "D07" -> {
                            viewModel.onViewModeChange(LibraryViewMode.LIST)
                            viewModel.onSortModeChange(LibrarySortMode.RECENT)
                            viewModel.onDismissSheet()
                        }
                        "D08" -> {
                            viewModel.onViewModeChange(LibraryViewMode.COLLECTIONS)
                            viewModel.onDismissSheet()
                        }
                    }
                } catch (_: Exception) {
                    // Activity might not be ready yet
                }
            }
        }
    }

    private suspend fun seedBooks(bookDao: app.opusreader.data.dao.BookDao, books: List<BookEntity>) {
        bookDao.deleteAllBooks()
        bookDao.insertBooks(books)
    }

    private fun createD01Books(isLiveness: Boolean): List<BookEntity> {
        val title0 = if (isLiveness) "Gardens of the Moon (Liveness)" else "Gardens of the Moon: Book One of The Malazan Book of the Fallen"
        return listOf(
            BookEntity(
                id = "gardens_of_the_moon",
                title = title0,
                author = "Erikson, Steven",
                filePath = "/books/gardens.epub",
                coverPath = "android.resource://app.opusreader/drawable/cover_0",
                progressPercent = if (isLiveness) 50f else 0f,
                isDownloaded = isLiveness,
                isSample = false,
                isNew = false,
                collectionName = "Favorites",
                lastReadTimestamp = 1700000012000L
            ),
            BookEntity(
                id = "complete_works_kafka",
                title = "Complete Works (Kafka, Franz)",
                author = "Kafka, Franz",
                filePath = "/books/kafka.epub",
                coverPath = "android.resource://app.opusreader/drawable/cover_1",
                progressPercent = 0f,
                isDownloaded = false,
                isSample = false,
                isNew = false,
                collectionName = null,
                lastReadTimestamp = 1700000011000L
            ),
            BookEntity(
                id = "odyssey_homer",
                title = "The Odyssey",
                author = "Homer",
                filePath = "/books/odyssey.epub",
                coverPath = "android.resource://app.opusreader/drawable/cover_2",
                progressPercent = 0f,
                isDownloaded = false,
                isSample = false,
                isNew = false,
                collectionName = "Classics",
                lastReadTimestamp = 1700000010000L
            ),
            BookEntity(
                id = "hyperion_simmons",
                title = "Hyperion",
                author = "Simmons, Dan",
                filePath = "/books/hyperion.epub",
                coverPath = "android.resource://app.opusreader/drawable/cover_3",
                progressPercent = 0f,
                isDownloaded = false,
                isSample = false,
                isNew = false,
                collectionName = "Favorites",
                lastReadTimestamp = 1700000009000L
            ),
            BookEntity(
                id = "the_road_mccarthy",
                title = "The Road",
                author = "McCarthy, Cormac",
                filePath = "/books/road.epub",
                coverPath = "android.resource://app.opusreader/drawable/cover_4",
                progressPercent = 0f,
                isDownloaded = false,
                isSample = false,
                isNew = false,
                collectionName = null,
                lastReadTimestamp = 1700000008000L
            ),
            BookEntity(
                id = "notes_underground_dostoevsky",
                title = "Notes from Underground",
                author = "Dostoevsky, Fyodor",
                filePath = "/books/notes.epub",
                coverPath = "android.resource://app.opusreader/drawable/cover_5",
                progressPercent = 0f,
                isDownloaded = false,
                isSample = false,
                isNew = false,
                collectionName = "Classics",
                lastReadTimestamp = 1700000007000L
            ),
            BookEntity(
                id = "frankenstein_shelley",
                title = "Frankenstein",
                author = "Shelley, Mary",
                filePath = "/books/frankenstein.epub",
                coverPath = "android.resource://app.opusreader/drawable/cover_6",
                progressPercent = 0f,
                isDownloaded = false,
                isSample = false,
                isNew = false,
                collectionName = "Classics",
                lastReadTimestamp = 1700000006000L
            ),
            BookEntity(
                id = "pride_and_prejudice_austen",
                title = "Pride and Prejudice",
                author = "Austen, Jane",
                filePath = "/books/pride.epub",
                coverPath = "android.resource://app.opusreader/drawable/cover_7",
                progressPercent = 0f,
                isDownloaded = false,
                isSample = false,
                isNew = false,
                collectionName = "Classics",
                lastReadTimestamp = 1700000005000L
            ),
            BookEntity(
                id = "moby_dick_melville",
                title = "Moby-Dick",
                author = "Melville, Herman",
                filePath = "/books/moby.epub",
                coverPath = "android.resource://app.opusreader/drawable/cover_8",
                progressPercent = 0f,
                isDownloaded = false,
                isSample = false,
                isNew = false,
                collectionName = null,
                lastReadTimestamp = 1700000004000L
            ),
            BookEntity(
                id = "the_metamorphosis_kafka",
                title = "The Metamorphosis",
                author = "Kafka, Franz",
                filePath = "/books/metamorphosis.epub",
                coverPath = "android.resource://app.opusreader/drawable/cover_9",
                progressPercent = 0f,
                isDownloaded = false,
                isSample = false,
                isNew = false,
                collectionName = "Favorites",
                lastReadTimestamp = 1700000003000L
            ),
            BookEntity(
                id = "crime_and_punishment",
                title = "Crime and Punishment",
                author = "Dostoevsky, Fyodor",
                filePath = "/books/crime.epub",
                coverPath = "android.resource://app.opusreader/drawable/cover_10",
                progressPercent = 0f,
                isDownloaded = false,
                isSample = false,
                isNew = false,
                collectionName = "Classics",
                lastReadTimestamp = 1700000002000L
            ),
            BookEntity(
                id = "dracula_stoker",
                title = "Dracula",
                author = "Stoker, Bram",
                filePath = "/books/dracula.epub",
                coverPath = "android.resource://app.opusreader/drawable/cover_11",
                progressPercent = 0f,
                isDownloaded = false,
                isSample = false,
                isNew = false,
                collectionName = "Classics",
                lastReadTimestamp = 1700000001000L
            )
        )
    }

    private fun createD05Books(isLiveness: Boolean): List<BookEntity> {
        val list = mutableListOf<BookEntity>()
        // 5 Samples (unread)
        for (i in 1..5) {
            list.add(
                BookEntity(
                    id = "sample_$i",
                    title = "Sample Book $i",
                    author = "Sample Author $i",
                    filePath = "/samples/$i.epub",
                    progressPercent = 0f,
                    isDownloaded = false,
                    isSample = true,
                    collectionName = null
                )
            )
        }
        // 70 Docs: 16 Read, 54 Unread (or 17 Read, 53 Unread if isLiveness)
        val readDocsCount = if (isLiveness) 17 else 16
        for (i in 1..70) {
            val isRead = i <= readDocsCount
            list.add(
                BookEntity(
                    id = "doc_$i",
                    title = "Document $i",
                    author = "Author $i",
                    filePath = "/docs/$i.epub",
                    progressPercent = if (isRead) 100f else 0f,
                    isDownloaded = true,
                    isSample = false,
                    collectionName = "Docs"
                )
            )
        }
        return list
    }

    private fun createD08Books(isLiveness: Boolean): List<BookEntity> {
        val baseBooks = createD01Books(false)
        return if (isLiveness) {
            // Put one book in a collection
            baseBooks.mapIndexed { idx, book ->
                if (idx == 0) book.copy(collectionName = "Favorites") else book.copy(collectionName = null)
            }
        } else {
            // No collections
            baseBooks.map { it.copy(collectionName = null) }
        }
    }
}
