package app.opusreader.feature.reader

import app.opusreader.data.entity.AnnotationEntity
import app.opusreader.data.entity.AnnotationType
import app.opusreader.data.entity.BookEntity
import app.opusreader.data.entity.SpineItemEntity
import app.opusreader.data.entity.TocEntryEntity
import app.opusreader.data.importer.EpubImporter
import app.opusreader.data.repository.LibraryRepository
import app.opusreader.data.repository.SettingsRepository
import app.opusreader.engine.ReaderEngine
import app.opusreader.engine.model.BookInfo
import app.opusreader.engine.model.BookSource
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.LocatorRange
import app.opusreader.engine.model.Page
import app.opusreader.engine.model.PageTheme
import app.opusreader.engine.model.Progress
import app.opusreader.engine.model.SearchHit
import app.opusreader.engine.model.TocEntry
import app.opusreader.engine.model.WordBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val fakeBooks = MutableStateFlow<List<BookEntity>>(emptyList())
    private val fakeAnnotations = MutableStateFlow<List<AnnotationEntity>>(emptyList())

    private val fakeLibraryRepo = object : LibraryRepository {
        override fun getBooks(): Flow<List<BookEntity>> = fakeBooks
        override fun getDownloadedBooks(): Flow<List<BookEntity>> = fakeBooks
        override fun getBooksByCollection(collectionName: String): Flow<List<BookEntity>> = fakeBooks
        override fun getDistinctCollections(): Flow<List<String>> = flowOf(emptyList())

        override suspend fun getBook(id: String): BookEntity? = fakeBooks.value.firstOrNull { it.id == id }

        override suspend fun saveBook(book: BookEntity) {
            fakeBooks.update { current -> current.filterNot { it.id == book.id } + book }
        }

        override suspend fun deleteBook(id: String) {
            fakeBooks.update { current -> current.filterNot { it.id == id } }
        }

        override suspend fun updateProgress(id: String, spineIndex: Int, charOffset: Int, progressPercent: Float) {
            fakeBooks.update { current ->
                current.map { b ->
                    if (b.id == id) b.copy(spineIndex = spineIndex, charOffset = charOffset, progressPercent = progressPercent)
                    else b
                }
            }
        }

        override suspend fun updateCollection(id: String, collectionName: String?) {}
        override suspend fun getSpine(bookId: String): List<SpineItemEntity> = emptyList()
        override suspend fun getToc(bookId: String): List<TocEntryEntity> = emptyList()

        override fun getAnnotations(bookId: String): Flow<List<AnnotationEntity>> = fakeAnnotations

        override suspend fun addAnnotation(annotation: AnnotationEntity) {
            fakeAnnotations.update { it + annotation }
        }

        override suspend fun deleteAnnotation(id: String) {
            fakeAnnotations.update { current -> current.filterNot { it.id == id } }
        }

        override fun createImporter(context: android.content.Context): EpubImporter {
            throw UnsupportedOperationException()
        }
    }

    private val fakeSettingsRepo = object : SettingsRepository {
        override val settingsFlow: Flow<app.opusreader.data.repository.ReaderSettings> = flowOf(app.opusreader.data.repository.ReaderSettings())
        override suspend fun updateTheme(themeIndex: Int) {}
        override suspend fun updateFontFamily(fontFamily: String) {}
        override suspend fun updateFontSize(fontSize: Float) {}
        override suspend fun updateLineSpacing(lineSpacing: Float) {}
        override suspend fun updateContinuousScroll(enabled: Boolean) {}
        override suspend fun updateBrightness(brightness: Float) {}
        override suspend fun updateLibraryViewMode(mode: String) {}
        override suspend fun updateLibrarySortMode(mode: String) {}
        override suspend fun updateLibraryFilter(filter: String) {}
        override suspend fun updateGroupBySeries(groupBySeries: Boolean) {}
    }

    private val samplePages = listOf(
        Page(
            start = Locator(0, 0),
            end = Locator(0, 50),
            words = listOf(WordBox("First", android.graphics.RectF(), Locator(0, 0), Locator(0, 5))),
            links = emptyList()
        ),
        Page(
            start = Locator(0, 51),
            end = Locator(0, 100),
            words = listOf(WordBox("Second", android.graphics.RectF(), Locator(0, 51), Locator(0, 57))),
            links = emptyList()
        ),
        Page(
            start = Locator(1, 0),
            end = Locator(1, 80),
            words = listOf(WordBox("ChapterTwo", android.graphics.RectF(), Locator(1, 0), Locator(1, 10))),
            links = emptyList()
        )
    )

    private val sampleToc = listOf(
        TocEntry("Chapter 1", Locator(0, 0), level = 0),
        TocEntry("Chapter 2", Locator(1, 0), level = 0)
    )

    private val fakeEngine = object : ReaderEngine {
        override suspend fun open(book: BookSource, settings: app.opusreader.engine.model.ReaderSettings): BookInfo =
            BookInfo(book.id, "Test Title", "Test Author", 2)

        override fun pageAt(locator: Locator): Page {
            return samplePages.firstOrNull { it.start.spineIndex == locator.spineIndex && locator.charOffset in it.start.charOffset..it.end.charOffset }
                ?: if (locator.charOffset > 50) samplePages.last() else samplePages.first()
        }

        override fun next(page: Page): Page? {
            val idx = samplePages.indexOfFirst { it.start == page.start }
            return if (idx in 0 until samplePages.lastIndex) samplePages[idx + 1] else null
        }

        override fun previous(page: Page): Page? {
            val idx = samplePages.indexOfFirst { it.start == page.start }
            return if (idx > 0) samplePages[idx - 1] else null
        }

        override fun progress(page: Page): Progress {
            val idx = samplePages.indexOfFirst { it.start == page.start }.coerceAtLeast(0)
            val currentOffset = if (idx == 0) 1 else if (idx == 1) 51 else 101
            val pct = ((idx + 1).toFloat() / samplePages.size.toFloat()) * 100f
            return Progress(current = currentOffset, total = 101, percent = pct)
        }

        override fun hitTest(page: Page, x: Float, y: Float): Locator? = null
        override fun rectsFor(page: Page, range: LocatorRange): List<android.graphics.RectF> = emptyList()
        override fun draw(page: Page, canvas: android.graphics.Canvas, theme: PageTheme) {}
        override suspend fun search(query: String): List<SearchHit> = emptyList()
        override val toc: List<TocEntry> = sampleToc
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeBooks.value = listOf(
            BookEntity(
                id = "book_1",
                title = "Test Book",
                author = "Author Name",
                filePath = "/test/book.epub",
                spineIndex = 0,
                charOffset = 0,
                progressPercent = 0f
            )
        )
        fakeAnnotations.value = emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadBook_populatesInitialState_andLoadsPageAtSavedLocator() = testScope.runTest {
        val viewModel = ReaderViewModel(fakeLibraryRepo, fakeSettingsRepo, fakeEngine)
        viewModel.loadBook("book_1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.book)
        assertEquals("Test Book", state.book?.title)
        assertEquals(samplePages[0], state.currentPage)
        assertEquals(1, state.progress.current)
        assertEquals(101, state.progress.total)
        assertEquals(2, state.tocEntries.size)
    }

    @Test
    fun nextAndPreviousPage_transitionsPages_andPersistsProgress() = testScope.runTest {
        val viewModel = ReaderViewModel(fakeLibraryRepo, fakeSettingsRepo, fakeEngine)
        viewModel.loadBook("book_1")
        advanceUntilIdle()

        // Turn next
        viewModel.onNextPage()
        advanceUntilIdle()
        assertEquals(samplePages[1], viewModel.uiState.value.currentPage)
        assertEquals(51, viewModel.uiState.value.progress.current)

        // Turn next again
        viewModel.onNextPage()
        advanceUntilIdle()
        assertEquals(samplePages[2], viewModel.uiState.value.currentPage)
        assertEquals(101, viewModel.uiState.value.progress.current)

        // Turn previous
        viewModel.onPreviousPage()
        advanceUntilIdle()
        assertEquals(samplePages[1], viewModel.uiState.value.currentPage)

        // Verify persisted in repository
        val updatedBook = fakeLibraryRepo.getBook("book_1")
        assertNotNull(updatedBook)
        assertEquals(samplePages[1].start.spineIndex, updatedBook?.spineIndex)
        assertEquals(samplePages[1].start.charOffset, updatedBook?.charOffset)
    }

    @Test
    fun toggleChrome_updatesVisibilityState() = testScope.runTest {
        val viewModel = ReaderViewModel(fakeLibraryRepo, fakeSettingsRepo, fakeEngine)
        assertFalse(viewModel.uiState.value.isChromeVisible)

        viewModel.onToggleChrome()
        assertTrue(viewModel.uiState.value.isChromeVisible)

        viewModel.onToggleChrome()
        assertFalse(viewModel.uiState.value.isChromeVisible)
    }

    @Test
    fun toggleBookmark_addsAndRemovesBookmarkEntity() = testScope.runTest {
        val viewModel = ReaderViewModel(fakeLibraryRepo, fakeSettingsRepo, fakeEngine)
        viewModel.loadBook("book_1")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isBookmarked)

        // Add bookmark
        viewModel.onToggleBookmark()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isBookmarked)
        assertEquals(1, fakeAnnotations.value.size)
        assertEquals(AnnotationType.BOOKMARK, fakeAnnotations.value.first().type)

        // Remove bookmark
        viewModel.onToggleBookmark()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isBookmarked)
        assertEquals(0, fakeAnnotations.value.size)
    }

    @Test
    fun scrubberChangeAndCommit_updatesLocationAndJumpsPage() = testScope.runTest {
        val viewModel = ReaderViewModel(fakeLibraryRepo, fakeSettingsRepo, fakeEngine)
        viewModel.loadBook("book_1")
        advanceUntilIdle()

        viewModel.onScrubberChange(51)
        assertEquals(51, viewModel.uiState.value.scrubberLocation)

        viewModel.onScrubberCommit(101)
        advanceUntilIdle()
        assertEquals(101, viewModel.uiState.value.scrubberLocation)
    }

    @Test
    fun jumpToToc_navigatesToChapterLocator_andDismissesSheet() = testScope.runTest {
        val viewModel = ReaderViewModel(fakeLibraryRepo, fakeSettingsRepo, fakeEngine)
        viewModel.loadBook("book_1")
        advanceUntilIdle()

        viewModel.onOpenSheet(ActiveReaderSheet.CONTENTS)
        assertEquals(ActiveReaderSheet.CONTENTS, viewModel.uiState.value.activeSheet)

        // Jump to chapter 2
        viewModel.onJumpToToc(Locator(1, 0))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.activeSheet)
        assertEquals(samplePages[2], viewModel.uiState.value.currentPage)
    }
}
