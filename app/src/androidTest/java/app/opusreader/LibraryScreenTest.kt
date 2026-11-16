package app.opusreader

import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.opusreader.data.db.OpusDatabase
import app.opusreader.data.entity.BookEntity
import app.opusreader.data.repository.LibraryRepository
import app.opusreader.data.repository.SettingsRepository
import app.opusreader.design.theme.OpusTheme
import app.opusreader.feature.library.LibraryFilter
import app.opusreader.feature.library.LibraryScreen
import app.opusreader.feature.library.LibraryViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: OpusDatabase
    private lateinit var libraryRepo: LibraryRepository
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var viewModel: LibraryViewModel

    @Before
    fun setUp() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        database = OpusDatabase.createInMemory(targetContext)
        libraryRepo = LibraryRepository.createForDatabase(database)
        settingsRepo = SettingsRepository.create(targetContext)

        runBlocking {
            settingsRepo.updateLibraryViewMode("GRID")
            settingsRepo.updateLibraryFilter("ALL")
            settingsRepo.updateLibrarySortMode("RECENT")
            settingsRepo.updateGroupBySeries(false)

            libraryRepo.saveBook(
                BookEntity(
                    id = "book-austen",
                    title = "Pride and Prejudice",
                    author = "Jane Austen",
                    filePath = "/fake/austen.epub",
                    isDownloaded = true,
                    isSample = false,
                    lastReadTimestamp = System.currentTimeMillis()
                )
            )
            libraryRepo.saveBook(
                BookEntity(
                    id = "book-melville",
                    title = "Moby Dick",
                    author = "Herman Melville",
                    filePath = "/fake/melville.epub",
                    isDownloaded = false,
                    isSample = true,
                    lastReadTimestamp = 0L
                )
            )
        }

        viewModel = LibraryViewModel(libraryRepo, settingsRepo)
    }

    private fun waitForBooksToLoad() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("book_tile_book-austen").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun library_rendersGridWithBooksAndControls() {
        composeTestRule.setContent {
            OpusTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LibraryScreen(
                    uiState = uiState,
                    onOpenBook = { },
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onFilterChange = viewModel::onFilterChange,
                    onViewModeChange = viewModel::onViewModeChange,
                    onSortModeChange = viewModel::onSortModeChange,
                    onSelectCollection = viewModel::onSelectCollection,
                    onOpenSheet = viewModel::onOpenSheet,
                    onDismissSheet = viewModel::onDismissSheet,
                    onToggleReadStatus = viewModel::onToggleReadStatus,
                    onToggleDownload = viewModel::onToggleDownload,
                    onDeleteBook = viewModel::onDeleteBook,
                    onAssignCollection = viewModel::onAssignCollection,
                    onNewCollectionNameChanged = viewModel::onNewCollectionNameChanged,
                    onCreateCollection = viewModel::onCreateCollection,
                    onGroupBySeriesChange = viewModel::setGroupBySeries,
                    onApplyFilterState = viewModel::onApplyFilterState,
                    modifier = Modifier.testTag("library_screen_test")
                )
            }
        }

        waitForBooksToLoad()
        composeTestRule.waitForIdle()

        // 1. Controls are displayed
        composeTestRule.onNodeWithTag("library_top_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("library_search_pill").assertIsDisplayed()

        // 2. Books grid is displayed
        composeTestRule.onNodeWithTag("library_books_grid").assertIsDisplayed()
        composeTestRule.onNodeWithTag("book_tile_book-austen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("book_tile_book-melville").assertIsDisplayed()
    }

    @Test
    fun searchAndFiltering_updatesGridContent() {
        composeTestRule.setContent {
            OpusTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LibraryScreen(
                    uiState = uiState,
                    onOpenBook = { },
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onFilterChange = viewModel::onFilterChange,
                    onViewModeChange = viewModel::onViewModeChange,
                    onSortModeChange = viewModel::onSortModeChange,
                    onSelectCollection = viewModel::onSelectCollection,
                    onOpenSheet = viewModel::onOpenSheet,
                    onDismissSheet = viewModel::onDismissSheet,
                    onToggleReadStatus = viewModel::onToggleReadStatus,
                    onToggleDownload = viewModel::onToggleDownload,
                    onDeleteBook = viewModel::onDeleteBook,
                    onAssignCollection = viewModel::onAssignCollection,
                    onNewCollectionNameChanged = viewModel::onNewCollectionNameChanged,
                    onCreateCollection = viewModel::onCreateCollection,
                    onGroupBySeriesChange = viewModel::setGroupBySeries,
                    onApplyFilterState = viewModel::onApplyFilterState,
                    modifier = Modifier.testTag("library_screen_test")
                )
            }
        }

        waitForBooksToLoad()
        composeTestRule.waitForIdle()

        // Filter via Filter sheet
        composeTestRule.onNodeWithTag("library_filter_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("filter_opt_samples").performClick()
        composeTestRule.onNodeWithTag("filter_done_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("book_tile_book-melville").assertIsDisplayed()
        composeTestRule.onNodeWithTag("book_tile_book-austen").assertDoesNotExist()

        // Reset filter
        composeTestRule.onNodeWithTag("library_filter_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("filter_opt_samples").performClick()
        composeTestRule.onNodeWithTag("filter_done_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("book_tile_book-austen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("book_tile_book-melville").assertIsDisplayed()

        // Type search query
        composeTestRule.onNodeWithTag("search_text_input").performTextInput("Austen")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_text_input").assertTextEquals("Austen")
        composeTestRule.onNodeWithTag("book_tile_book-austen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("book_tile_book-melville").assertDoesNotExist()

        // Clear search
        composeTestRule.onNodeWithTag("search_clear_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("book_tile_book-melville").assertIsDisplayed()

        Espresso.closeSoftKeyboard()
    }

    @Test
    fun sheets_filterAndViewSortSheetsFunctionCorrectly() {
        composeTestRule.setContent {
            OpusTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LibraryScreen(
                    uiState = uiState,
                    onOpenBook = { },
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onFilterChange = viewModel::onFilterChange,
                    onViewModeChange = viewModel::onViewModeChange,
                    onSortModeChange = viewModel::onSortModeChange,
                    onSelectCollection = viewModel::onSelectCollection,
                    onOpenSheet = viewModel::onOpenSheet,
                    onDismissSheet = viewModel::onDismissSheet,
                    onToggleReadStatus = viewModel::onToggleReadStatus,
                    onToggleDownload = viewModel::onToggleDownload,
                    onDeleteBook = viewModel::onDeleteBook,
                    onAssignCollection = viewModel::onAssignCollection,
                    onNewCollectionNameChanged = viewModel::onNewCollectionNameChanged,
                    onCreateCollection = viewModel::onCreateCollection,
                    onGroupBySeriesChange = viewModel::setGroupBySeries,
                    onApplyFilterState = viewModel::onApplyFilterState,
                    modifier = Modifier.testTag("library_screen_test")
                )
            }
        }

        waitForBooksToLoad()
        composeTestRule.waitForIdle()

        // 1. Open Filter sheet
        composeTestRule.onNodeWithTag("library_filter_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("library_filter_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("filter_opt_read").assertIsDisplayed()
        composeTestRule.onNodeWithTag("filter_opt_unread").assertIsDisplayed()
        composeTestRule.onNodeWithTag("filter_opt_books").assertIsDisplayed()
        composeTestRule.onNodeWithTag("filter_opt_samples").assertIsDisplayed()
        composeTestRule.onNodeWithTag("filter_opt_docs").assertIsDisplayed()

        // Dismiss filter sheet via Done button
        composeTestRule.onNodeWithTag("filter_done_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("library_filter_sheet").assertDoesNotExist()

        // 2. Open View and Sort sheet
        composeTestRule.onNodeWithTag("library_view_sort_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("library_view_sort_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("group_by_series_switch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("view_opt_list").performClick()
        composeTestRule.waitForIdle()

        // Dismiss via top scrim area
        composeTestRule.onNodeWithTag("sheet_scrim_top").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("library_view_sort_sheet").assertDoesNotExist()

        // Verifies list view is now shown instead of grid
        composeTestRule.onNodeWithTag("library_books_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("book_row_book-austen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("book_row_book-melville").assertIsDisplayed()
    }
}
