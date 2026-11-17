package app.opusreader

import android.content.Context
import app.opusreader.data.entity.AnnotationEntity
import app.opusreader.data.entity.BookEntity
import app.opusreader.data.entity.SpineItemEntity
import app.opusreader.data.entity.TocEntryEntity
import app.opusreader.data.importer.EpubImporter
import app.opusreader.data.repository.LibraryRepository
import app.opusreader.data.repository.ReaderSettings
import app.opusreader.data.repository.SettingsRepository
import app.opusreader.feature.library.ActiveSheet
import app.opusreader.feature.library.LibraryFilter
import app.opusreader.feature.library.LibrarySortMode
import app.opusreader.feature.library.LibraryViewModel
import app.opusreader.feature.library.LibraryViewMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppUnitTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val fakeBooks = MutableStateFlow<List<BookEntity>>(emptyList())
    private val fakeSettings = MutableStateFlow(ReaderSettings())

    private val fakeLibraryRepo = object : LibraryRepository {
        override fun getBooks(): Flow<List<BookEntity>> = fakeBooks
        override fun getDownloadedBooks(): Flow<List<BookEntity>> = fakeBooks.map { it.filter { b -> b.isDownloaded } }
        override fun getBooksByCollection(collectionName: String): Flow<List<BookEntity>> =
            fakeBooks.map { it.filter { b -> b.collectionName == collectionName } }

        override fun getDistinctCollections(): Flow<List<String>> =
            fakeBooks.map { it.mapNotNull { b -> b.collectionName }.filter { c -> c.isNotBlank() }.distinct().sorted() }

        override suspend fun getBook(id: String): BookEntity? = fakeBooks.value.firstOrNull { it.id == id }

        override suspend fun saveBook(book: BookEntity) {
            fakeBooks.update { current ->
                val filtered = current.filterNot { it.id == book.id }
                filtered + book
            }
        }

        override suspend fun deleteBook(id: String) {
            fakeBooks.update { it.filterNot { b -> b.id == id } }
        }

        override suspend fun updateProgress(
            id: String,
            spineIndex: Int,
            charOffset: Int,
            progressPercent: Float
        ) {
            fakeBooks.update { current ->
                current.map { b ->
                    if (b.id == id) {
                        b.copy(
                            spineIndex = spineIndex,
                            charOffset = charOffset,
                            progressPercent = progressPercent,
                            lastReadTimestamp = System.currentTimeMillis()
                        )
                    } else b
                }
            }
        }

        override suspend fun updateCollection(id: String, collectionName: String?) {
            fakeBooks.update { current ->
                current.map { if (it.id == id) it.copy(collectionName = collectionName) else it }
            }
        }

        override suspend fun getSpine(bookId: String): List<SpineItemEntity> = emptyList()
        override suspend fun getToc(bookId: String): List<TocEntryEntity> = emptyList()
        override fun getAnnotations(bookId: String): Flow<List<AnnotationEntity>> = flowOf(emptyList())
        override suspend fun addAnnotation(annotation: AnnotationEntity) { }
        override suspend fun deleteAnnotation(id: String) { }
        override suspend fun toggleAnnotationFavorite(id: String, isFavorite: Boolean) { }
        override fun createImporter(context: Context): EpubImporter = error("Not needed for test")
    }

    private val fakeSettingsRepo = object : SettingsRepository {
        override val settingsFlow: Flow<ReaderSettings> = fakeSettings
        override suspend fun updateTheme(themeIndex: Int) { fakeSettings.update { it.copy(themeIndex = themeIndex) } }
        override suspend fun updateFontFamily(fontFamily: String) { fakeSettings.update { it.copy(fontFamily = fontFamily) } }
        override suspend fun updateFontSize(fontSize: Float) { fakeSettings.update { it.copy(fontSize = fontSize) } }
        override suspend fun updateLineSpacing(lineSpacing: Float) { fakeSettings.update { it.copy(lineSpacing = lineSpacing) } }
        override suspend fun updateContinuousScroll(enabled: Boolean) { fakeSettings.update { it.copy(continuousScroll = enabled) } }
        override suspend fun updateBrightness(brightness: Float) { fakeSettings.update { it.copy(brightness = brightness) } }
        override suspend fun updateLibraryViewMode(mode: String) { fakeSettings.update { it.copy(libraryViewMode = mode) } }
        override suspend fun updateLibrarySortMode(mode: String) { fakeSettings.update { it.copy(librarySortMode = mode) } }
        override suspend fun updateLibraryFilter(filter: String) { fakeSettings.update { it.copy(libraryFilter = filter) } }
        override suspend fun updateGroupBySeries(groupBySeries: Boolean) { fakeSettings.update { it.copy(groupBySeries = groupBySeries) } }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeBooks.value = listOf(
            BookEntity(
                id = "b1",
                title = "Pride and Prejudice",
                author = "Jane Austen",
                filePath = "/path/1.epub",
                progressPercent = 0f,
                isDownloaded = true,
                isSample = false,
                lastReadTimestamp = 100L
            ),
            BookEntity(
                id = "b2",
                title = "Moby Dick",
                author = "Herman Melville",
                filePath = "/path/2.epub",
                progressPercent = 50f,
                isDownloaded = false,
                isSample = false,
                lastReadTimestamp = 200L
            ),
            BookEntity(
                id = "b3",
                title = "Sample Story",
                author = "Author X",
                filePath = "/path/3.epub",
                progressPercent = 100f,
                isDownloaded = true,
                isSample = true,
                lastReadTimestamp = 50L
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun searchQuery_filtersBooksByTitleAndAuthor() = testScope.runTest {
        val viewModel = LibraryViewModel(fakeLibraryRepo, fakeSettingsRepo)
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.filteredBooks.size)

        // Search by author
        viewModel.onSearchQueryChange("austen")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredBooks.size)
        assertEquals("Pride and Prejudice", viewModel.uiState.value.filteredBooks[0].title)

        // Search by title
        viewModel.onSearchQueryChange("moby")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredBooks.size)
        assertEquals("Moby Dick", viewModel.uiState.value.filteredBooks[0].title)

        // Clear query
        viewModel.onSearchQueryChange("")
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.filteredBooks.size)
    }

    @Test
    fun filterStatus_filtersDownloadedUnreadAndSamples() = testScope.runTest {
        val viewModel = LibraryViewModel(fakeLibraryRepo, fakeSettingsRepo)
        advanceUntilIdle()

        // Filter: Downloaded only
        viewModel.onFilterChange(LibraryFilter.DOWNLOADED)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.filteredBooks.size)
        assertTrue(viewModel.uiState.value.filteredBooks.all { it.isDownloaded })

        // Filter: Unread only (progress == 0)
        viewModel.onFilterChange(LibraryFilter.UNREAD)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredBooks.size)
        assertEquals("b1", viewModel.uiState.value.filteredBooks[0].id)

        // Filter: Samples only
        viewModel.onFilterChange(LibraryFilter.SAMPLES)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredBooks.size)
        assertEquals("b3", viewModel.uiState.value.filteredBooks[0].id)
    }

    @Test
    fun sortMode_sortsAlphabeticallyAndByRecent() = testScope.runTest {
        val viewModel = LibraryViewModel(fakeLibraryRepo, fakeSettingsRepo)
        advanceUntilIdle()

        // Sort: Title
        viewModel.onSortModeChange(LibrarySortMode.TITLE)
        advanceUntilIdle()
        val titles = viewModel.uiState.value.filteredBooks.map { it.title }
        assertEquals(listOf("Moby Dick", "Pride and Prejudice", "Sample Story"), titles)

        // Sort: Recent (highest lastReadTimestamp first: b2=200, b1=100, b3=50)
        viewModel.onSortModeChange(LibrarySortMode.RECENT)
        advanceUntilIdle()
        val recentIds = viewModel.uiState.value.filteredBooks.map { it.id }
        assertEquals(listOf("b2", "b1", "b3"), recentIds)
    }

    @Test
    fun bookActions_toggleReadStatusAndDownload() = testScope.runTest {
        val viewModel = LibraryViewModel(fakeLibraryRepo, fakeSettingsRepo)
        advanceUntilIdle()

        // Toggle read status on b1 (0% -> 100%)
        viewModel.onToggleReadStatus("b1")
        advanceUntilIdle()
        val book1 = viewModel.uiState.value.allBooks.first { it.id == "b1" }
        assertEquals(100f, book1.progressPercent)

        // Toggle read status again (100% -> 0%)
        viewModel.onToggleReadStatus("b1")
        advanceUntilIdle()
        val book1Unread = viewModel.uiState.value.allBooks.first { it.id == "b1" }
        assertEquals(0f, book1Unread.progressPercent)

        // Toggle download on b2 (false -> true)
        viewModel.onToggleDownload("b2")
        advanceUntilIdle()
        val book2Downloaded = viewModel.uiState.value.allBooks.first { it.id == "b2" }
        assertTrue(book2Downloaded.isDownloaded)
    }

    @Test
    fun collectionManagement_createAndAssign() = testScope.runTest {
        val viewModel = LibraryViewModel(fakeLibraryRepo, fakeSettingsRepo)
        advanceUntilIdle()

        // Assign b1 to collection "Classics"
        viewModel.onAssignCollection("b1", "Classics")
        advanceUntilIdle()

        assertEquals(listOf("Classics"), viewModel.uiState.value.collections)

        // Filter by collection "Classics"
        viewModel.onSelectCollection("Classics")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredBooks.size)
        assertEquals("Pride and Prejudice", viewModel.uiState.value.filteredBooks[0].title)

        // Clear collection filter
        viewModel.onSelectCollection(null)
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.filteredBooks.size)
    }

    @Test
    fun sheetManagement_openAndDismiss() = testScope.runTest {
        val viewModel = LibraryViewModel(fakeLibraryRepo, fakeSettingsRepo)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.activeSheet)

        viewModel.onOpenSheet(ActiveSheet.FILTER, null)
        assertEquals(ActiveSheet.FILTER, viewModel.uiState.value.activeSheet)

        viewModel.onDismissSheet()
        assertNull(viewModel.uiState.value.activeSheet)

        viewModel.onOpenSheet(ActiveSheet.VIEW_SORT, null)
        assertEquals(ActiveSheet.VIEW_SORT, viewModel.uiState.value.activeSheet)

        viewModel.onDismissSheet()
        assertNull(viewModel.uiState.value.activeSheet)
    }

    @Test
    fun persistence_survivesViewModelRecreation() = testScope.runTest {
        val vm1 = LibraryViewModel(fakeLibraryRepo, fakeSettingsRepo)
        advanceUntilIdle()

        vm1.onViewModeChange(LibraryViewMode.LIST)
        vm1.onSortModeChange(LibrarySortMode.AUTHOR_ZA)
        vm1.setGroupBySeries(true)
        advanceUntilIdle()

        // Create new ViewModel with the same repository simulating recreation
        val vm2 = LibraryViewModel(fakeLibraryRepo, fakeSettingsRepo)
        advanceUntilIdle()

        assertEquals(LibraryViewMode.LIST, vm2.uiState.value.viewMode)
        assertEquals(LibrarySortMode.AUTHOR_ZA, vm2.uiState.value.sortMode)
        assertTrue(vm2.uiState.value.groupBySeries)
    }

    @Test
    fun sortMode_authorAzAndZa() = testScope.runTest {
        val viewModel = LibraryViewModel(fakeLibraryRepo, fakeSettingsRepo)
        advanceUntilIdle()

        viewModel.onSortModeChange(LibrarySortMode.AUTHOR_AZ)
        advanceUntilIdle()
        val authorsAz = viewModel.uiState.value.filteredBooks.map { it.author }
        assertEquals(listOf("Author X", "Herman Melville", "Jane Austen"), authorsAz)

        viewModel.onSortModeChange(LibrarySortMode.AUTHOR_ZA)
        advanceUntilIdle()
        val authorsZa = viewModel.uiState.value.filteredBooks.map { it.author }
        assertEquals(listOf("Jane Austen", "Herman Melville", "Author X"), authorsZa)
    }
}
