package app.opusreader.debug

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import app.opusreader.OpusApplication
import app.opusreader.data.db.OpusDatabase
import app.opusreader.data.entity.AnnotationEntity
import app.opusreader.data.entity.AnnotationType
import app.opusreader.data.entity.BookEntity
import app.opusreader.data.repository.SettingsRepository
import app.opusreader.feature.library.ActiveSheet
import app.opusreader.feature.library.LibrarySortMode
import app.opusreader.feature.library.LibraryViewModel
import app.opusreader.feature.library.LibraryViewMode
import app.opusreader.feature.reader.ActiveReaderSheet
import app.opusreader.feature.reader.AnnotationUiModel
import app.opusreader.feature.reader.AnnotationUiType
import app.opusreader.feature.reader.ReaderBookUiModel
import app.opusreader.feature.reader.ReaderViewModel
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.Page
import app.opusreader.engine.model.Progress
import app.opusreader.engine.model.TocEntry
import app.opusreader.engine.model.WordBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DebugFixtureController {

    fun ensureDefaultData(context: Context) {
        val app = context.applicationContext as OpusApplication
        val db = OpusDatabase.getInstance(app)
        val bookDao = db.bookDao()
        CoroutineScope(Dispatchers.IO).launch {
            val existing = bookDao.getAllBooksOnce()
            if (existing.isEmpty()) {
                val books = createD01Books(false).toMutableList()
                val malazan = BookEntity(
                    id = "fixture_malazan",
                    title = "The Complete Malazan",
                    author = "Steven Erikson",
                    filePath = "/books/malazan.epub",
                    coverPath = "android.resource://app.opusreader/drawable/cover_0",
                    spineIndex = 0,
                    charOffset = 91,
                    progressPercent = 0.10f,
                    lastReadTimestamp = System.currentTimeMillis()
                )
                books.add(0, malazan)
                bookDao.insertBooks(books)
                val annotDao = db.annotationDao()
                annotDao.deleteAnnotationsForBook("fixture_malazan")
                for (annot in createMalazanAnnotations(false)) {
                    annotDao.insertAnnotation(annot)
                }
            }
        }
    }

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
                "D10", "D11", "D12", "D13", "D14", "D15", "D16" -> {
                    val malazan = BookEntity(
                        id = "fixture_malazan",
                        title = if (isLiveness) "The Complete Malazan (Live)" else "The Complete Malazan",
                        author = "Steven Erikson",
                        filePath = "/books/malazan.epub",
                        coverPath = "android.resource://app.opusreader.debug/drawable/cover_d01_0",
                        spineIndex = if (isLiveness) 3 else 0,
                        charOffset = if (isLiveness) 245 else 91,
                        progressPercent = if (isLiveness) 0.28f else 0.10f
                    )
                    bookDao.insertBook(malazan)
                    val annots = createMalazanAnnotations(isLiveness)
                    val annotDao = db.annotationDao()
                    annotDao.deleteAnnotationsForBook("fixture_malazan")
                    for (annot in annots) {
                        annotDao.insertAnnotation(annot)
                    }
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

                    if (fixtureId in listOf("D10", "D11", "D12", "D13", "D14", "D15", "D16")) {
                        val factory = ReaderViewModel.provideFactory(
                            app.container.libraryRepository,
                            app.container.settingsRepository,
                            app.container.readerEngine
                        )
                        val readerVm = ViewModelProvider(activity, factory)[ReaderViewModel::class.java]
                        val tocList = createMalazanToc()
                        val words = createMalazanWords()
                        val page = Page(
                            start = Locator(if (isLiveness) 3 else 0, if (isLiveness) 245 else 91),
                            end = Locator(if (isLiveness) 3 else 0, if (isLiveness) 500 else 300),
                            words = words,
                            links = emptyList()
                        )
                        val bookUi = ReaderBookUiModel(
                            id = "fixture_malazan",
                            title = if (isLiveness) "The Complete Malazan (Live)" else "The Complete Malazan",
                            author = "Steven Erikson",
                            filePath = "/books/malazan.epub",
                            spineIndex = if (isLiveness) 3 else 0,
                            charOffset = if (isLiveness) 245 else 91,
                            progressPercent = if (isLiveness) 0.28f else 0.10f
                        )
                        val prog = Progress(
                            current = if (isLiveness) 245 else 91,
                            total = 879,
                            percent = if (isLiveness) 28f else 10f
                        )
                        val activeSheet = when (fixtureId) {
                            "D11" -> ActiveReaderSheet.CONTENTS
                            "D12" -> ActiveReaderSheet.SEARCH
                            "D13" -> ActiveReaderSheet.ANNOTATIONS
                            "D14" -> ActiveReaderSheet.ANNOTATIONS_FILTER
                            "D15" -> ActiveReaderSheet.ANNOTATIONS_SORT
                            "D16" -> ActiveReaderSheet.ANNOTATIONS_SHARE
                            else -> null
                        }
                        val annots = createMalazanAnnotations(isLiveness)
                        val uiAnnots = annots.map { annot ->
                            AnnotationUiModel(
                                id = annot.id,
                                bookId = annot.bookId,
                                spineIndex = annot.spineIndex,
                                startOffset = annot.startOffset,
                                endOffset = annot.endOffset,
                                selectedText = annot.selectedText,
                                noteText = annot.noteText,
                                colorHex = annot.colorHex,
                                isFavorite = annot.isFavorite,
                                createdTimestamp = annot.createdTimestamp,
                                type = when (annot.type) {
                                    AnnotationType.HIGHLIGHT -> AnnotationUiType.HIGHLIGHT
                                    AnnotationType.NOTE -> AnnotationUiType.NOTE
                                    AnnotationType.BOOKMARK -> AnnotationUiType.BOOKMARK
                                }
                            )
                        }
                        val searchHits = if (fixtureId == "D12" && isLiveness) createMalazanSearchHits(true) else emptyList()
                        val searchQ = if (fixtureId == "D12") (if (isLiveness) "emperor" else "") else ""

                        android.util.Log.d("OpusDebug", "Setting fixture state for $fixtureId, isLiveness=$isLiveness")
                        readerVm.setFixtureState(
                            book = bookUi,
                            page = page,
                            progress = prog,
                            toc = tocList,
                            chromeVisible = fixtureId == "D10",
                            activeSheet = activeSheet,
                            annotations = uiAnnots,
                            searchResults = searchHits,
                            searchQuery = searchQ
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("OpusDebug", "Error in handleFixtureDeepLink main: ${e.message}", e)
                }
            }
        }
    }

    private fun createMalazanToc(): List<TocEntry> = listOf(
        TocEntry("Preface to Gardens of the Moon — redux", Locator(0, 0), level = 0),
        TocEntry("Foreword to Gardens of the Moon — 10th Anniversary Edition", Locator(1, 0), level = 0),
        TocEntry("Maps", Locator(2, 0), level = 0),
        TocEntry("Dramatis Personae", Locator(3, 0), level = 0),
        TocEntry("Prologue", Locator(4, 0), level = 0),
        TocEntry("Book One: Pale", Locator(5, 0), level = 0),
        TocEntry("Chapter One", Locator(6, 0), level = 1),
        TocEntry("Chapter Two", Locator(7, 0), level = 1),
        TocEntry("Chapter Three", Locator(8, 0), level = 1),
        TocEntry("Chapter Four", Locator(9, 0), level = 1),
        TocEntry("Book Two: Darujhistan", Locator(10, 0), level = 0),
        TocEntry("Chapter Five", Locator(11, 0), level = 1),
        TocEntry("Chapter Six", Locator(12, 0), level = 1),
        TocEntry("Chapter Seven", Locator(13, 0), level = 1),
        TocEntry("Book Three: The Mission", Locator(14, 0), level = 0)
    )

    private fun createMalazanWords(): List<WordBox> {
        val text = "it was had brushed shoulders with more than his snare, An and in here and in the capital, Unta. scorch pire as “Is it true, then?” Ganoes asked boldly. comm “Is what true?” —only atten: “The First Sword of Empire. Dassem Ultor. We heard in the capital before we left. He’s dead. Is it true? Is Dassem dead?” The man seemed to flinch, his gaze unwavering on the Mouse. “Such is war,” he muttered, under his breath, as if the words were not meant for anyone else’s ears. “You're with the Third. I thought the Third was with him, in Seven Cities. At Y'ghatan—” “Hood's Breath, they're still looking for his body in the still-hot rubble of that damned city, and here you are, a merchant's son three thousand leagues from Seven Cities with information only a few are supposed to possess.” He still did not turn. “I know not your sources, but take my advice and keep what you know to yourself.” Ganoes shrugged. “It's said he betrayed a god.” Finally the man faced him. His face was scarred, and something that might have been a burn marred his jaw and left cheek. For all that, he looked young for a commander. “Heed the lesson there, son.” “What lesson?” “Every decision you make can change the world. The best life is the one the gods don't notice. You want to live free, boy, live quietly.” “I want to be a soldier. A hero.” “You'll grow out of it.” Mock's Vane squealed as a wayward gust from the harbor cleared the grainy smoke. Ganoes could now smell rotting fish and the waterfront's stink of humanity."
        var offset = 0
        return text.split("\\s+".toRegex()).map { word ->
            val start = offset
            offset += word.length + 1
            WordBox(
                text = word,
                rect = android.graphics.RectF(),
                start = Locator(0, start),
                end = Locator(0, offset - 1)
            )
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
        val baseBooks = createD01Books(false)
        val list = mutableListOf<BookEntity>()

        // 70 Docs total:
        // First 12 docs use the exact D01 books with their real covers, titles, authors:
        val readDocsTarget = if (isLiveness) 17 else 16
        baseBooks.forEachIndexed { idx, book ->
            val isRead = idx < readDocsTarget
            list.add(
                book.copy(
                    id = "d05_doc_${idx + 1}",
                    filePath = "/docs/${book.id}.pdf",
                    collectionName = "Docs",
                    isSample = false,
                    isDownloaded = true,
                    progressPercent = if (isRead) 100f else 0f
                )
            )
        }

        // Remaining 58 docs (to make 70 total docs)
        for (i in 13..70) {
            val isRead = (i - 1) < readDocsTarget
            val cycleIdx = (i - 1) % 12
            val template = baseBooks[cycleIdx]
            list.add(
                BookEntity(
                    id = "d05_doc_$i",
                    title = "${template.title} Vol. $i",
                    author = template.author,
                    filePath = "/docs/doc_$i.pdf",
                    coverPath = template.coverPath,
                    progressPercent = if (isRead) 100f else 0f,
                    isDownloaded = true,
                    isSample = false,
                    collectionName = "Docs",
                    lastReadTimestamp = 1700000000000L - i * 1000L
                )
            )
        }

        // 5 Samples (unread, not docs):
        for (i in 1..5) {
            val template = baseBooks[(i + 4) % 12]
            list.add(
                BookEntity(
                    id = "d05_sample_$i",
                    title = "${template.title} (Sample)",
                    author = template.author,
                    filePath = "/samples/sample_$i.epub",
                    coverPath = template.coverPath,
                    progressPercent = 0f,
                    isDownloaded = false,
                    isSample = true,
                    collectionName = null,
                    lastReadTimestamp = 0L
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

    private fun createMalazanAnnotations(isLiveness: Boolean): List<AnnotationEntity> = if (isLiveness) {
        listOf(
            AnnotationEntity(
                id = "fixture_annot_1",
                bookId = "fixture_malazan",
                spineIndex = 0,
                startOffset = 3971,
                endOffset = 4015,
                selectedText = "soft knock diverted his attention. “Yes?”",
                noteText = "Important meeting with the assassin.",
                colorHex = "#F9E054",
                isFavorite = true,
                type = app.opusreader.data.entity.AnnotationType.HIGHLIGHT
            ),
            AnnotationEntity(
                id = "fixture_annot_2",
                bookId = "fixture_malazan",
                spineIndex = 1,
                startOffset = 11240,
                endOffset = 11370,
                selectedText = "“Question,” Mallick Rel said. “Has the council begun?” “Not yet,” Coltaine said slowly. “We await my warlock.” The priest of",
                noteText = null,
                colorHex = "#68B6EF",
                isFavorite = false,
                type = app.opusreader.data.entity.AnnotationType.BOOKMARK
            ),
            AnnotationEntity(
                id = "fixture_annot_3",
                bookId = "fixture_malazan",
                spineIndex = 2,
                startOffset = 15200,
                endOffset = 15280,
                selectedText = "The Wickans rode north across the plains.",
                noteText = null,
                colorHex = "#F385A2",
                isFavorite = false,
                type = app.opusreader.data.entity.AnnotationType.HIGHLIGHT
            )
        )
    } else {
        listOf(
            AnnotationEntity(
                id = "fixture_annot_1",
                bookId = "fixture_malazan",
                spineIndex = 0,
                startOffset = 3971,
                endOffset = 4015,
                selectedText = "soft knock diverted his attention. “Yes?”",
                noteText = null,
                colorHex = "#F9E054",
                isFavorite = false,
                type = app.opusreader.data.entity.AnnotationType.HIGHLIGHT
            ),
            AnnotationEntity(
                id = "fixture_annot_2",
                bookId = "fixture_malazan",
                spineIndex = 1,
                startOffset = 11240,
                endOffset = 11370,
                selectedText = "“Question,” Mallick Rel said. “Has the council begun?” “Not yet,” Coltaine said slowly. “We await my warlock.” The priest of",
                noteText = null,
                colorHex = "#68B6EF",
                isFavorite = false,
                type = app.opusreader.data.entity.AnnotationType.BOOKMARK
            )
        )
    }

    private fun createMalazanSearchHits(isLiveness: Boolean): List<app.opusreader.engine.model.SearchHit> = listOf(
        app.opusreader.engine.model.SearchHit(
            locator = Locator(0, 91),
            snippet = "...Ganoes asked boldly. The stains of rust seep through the crumbling plaster..."
        ),
        app.opusreader.engine.model.SearchHit(
            locator = Locator(1, 245),
            snippet = "...In the year of the Moon's Tears, the empire was young and bold..."
        ),
        app.opusreader.engine.model.SearchHit(
            locator = Locator(4, 520),
            snippet = "...The Emperor Kellanved and Dancer walked through the dead gate..."
        )
    )
}
