package app.opusreader

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.opusreader.design.theme.OpusTheme
import app.opusreader.engine.FakeReaderEngine
import app.opusreader.engine.model.BookSource
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.LocatorRange
import app.opusreader.engine.model.Page
import app.opusreader.engine.model.Progress
import app.opusreader.engine.model.ReaderSettings
import app.opusreader.engine.model.SearchHit
import app.opusreader.engine.model.TocEntry
import app.opusreader.engine.model.WordBox
import app.opusreader.feature.reader.ActiveReaderSheet
import app.opusreader.feature.reader.AnnotationFilter
import app.opusreader.feature.reader.AnnotationSort
import app.opusreader.feature.reader.AnnotationUiModel
import app.opusreader.feature.reader.AnnotationUiType
import app.opusreader.feature.reader.ReaderBookUiModel
import app.opusreader.feature.reader.ReaderScreen
import app.opusreader.feature.reader.ReaderUiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ReaderScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var testEpubFile: File
    private val fakeEngine = FakeReaderEngine()

    private val sampleWords = listOf(
        WordBox("Ganoes", android.graphics.RectF(50f, 150f, 200f, 210f), Locator(0, 0), Locator(0, 6)),
        WordBox("asked", android.graphics.RectF(210f, 150f, 350f, 210f), Locator(0, 7), Locator(0, 12)),
        WordBox("boldly.", android.graphics.RectF(360f, 150f, 520f, 210f), Locator(0, 13), Locator(0, 20))
    )

    private val samplePage = Page(
        start = Locator(0, 0),
        end = Locator(0, 20),
        words = sampleWords,
        links = emptyList()
    )

    private val sampleToc = listOf(
        TocEntry("Preface to Gardens of the Moon — redux", Locator(0, 0), level = 0),
        TocEntry("Chapter One", Locator(1, 0), level = 1)
    )

    private val sampleAnnotations = listOf(
        AnnotationUiModel(
            id = "annot_1",
            bookId = "id_1",
            spineIndex = 0,
            startOffset = 0,
            endOffset = 6,
            selectedText = "Ganoes",
            colorHex = "#F9E054",
            type = AnnotationUiType.HIGHLIGHT
        ),
        AnnotationUiModel(
            id = "annot_2",
            bookId = "id_1",
            spineIndex = 0,
            startOffset = 7,
            endOffset = 12,
            selectedText = "asked",
            noteText = "Ganoes questioning authority",
            colorHex = "#68B6EF",
            type = AnnotationUiType.NOTE
        ),
        AnnotationUiModel(
            id = "annot_3",
            bookId = "id_1",
            spineIndex = 0,
            startOffset = 13,
            endOffset = 20,
            selectedText = "boldly.",
            isFavorite = true,
            type = AnnotationUiType.BOOKMARK
        )
    )

    private val sampleHits = listOf(
        SearchHit(
            locator = Locator(0, 0),
            snippet = "...Ganoes asked boldly..."
        )
    )

    @Before
    fun setUp() {
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val cacheDir = targetContext.cacheDir
        testEpubFile = File(cacheDir, "app_device_test_valid.epub")
        if (!testEpubFile.exists() || testEpubFile.length() == 0L) {
            val stream = try {
                testContext.assets.open("epubs/valid_book.epub")
            } catch (_: Exception) {
                targetContext.assets.open("epubs/valid_book.epub")
            }
            stream.use { input ->
                testEpubFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    @Test
    fun readerCanvas_rendersPageAndRespondsToCenterTap_toToggleChrome() {
        var chromeToggled = false
        val state = ReaderUiState(
            book = ReaderBookUiModel("id_1", "The Complete Malazan", "Steven Erikson", ""),
            currentPage = samplePage,
            progress = Progress(current = 91, total = 879, percent = 10f),
            isChromeVisible = false,
            tocEntries = sampleToc,
            isLoading = false
        )

        composeTestRule.setContent {
            OpusTheme {
                ReaderScreen(
                    uiState = state,
                    engine = fakeEngine,
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = { chromeToggled = true },
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = {},
                    onToggleThumbnails = {},
                    onWordLongPress = {},
                    onUpdateSelectionRange = {},
                    onClearSelection = {},
                    onAnnotationClick = {},
                    onCreateHighlight = {},
                    onChangeHighlightColor = { _, _ -> },
                    onDeleteAnnotation = {},
                    onOpenNoteEditor = {},
                    onDismissNoteEditor = {},
                    onSaveNote = { _, _ -> },
                    onToggleFavorite = {},
                    onSearchQueryChange = {},
                    onSelectSearchHit = {},
                    onNextSearchHit = {},
                    onPreviousSearchHit = {},
                    onSetAnnotationFilter = {},
                    onSetAnnotationSort = {}
                )
            }
        }

        // Verify canvas is displayed
        composeTestRule.onNodeWithTag("reading_canvas")
            .assertIsDisplayed()

        // Tap center to toggle chrome
        composeTestRule.onNodeWithTag("reader_gesture_surface").performTouchInput {
            click(center)
        }

        assertTrue("Center tap must trigger chrome toggle", chromeToggled)
    }

    @Test
    fun tapEdgeZones_navigatePreviousAndNextPages() {
        var prevCalled = false
        var nextCalled = false

        val state = ReaderUiState(
            book = ReaderBookUiModel("id_1", "The Complete Malazan", "Steven Erikson", ""),
            currentPage = samplePage,
            progress = Progress(current = 91, total = 879, percent = 10f),
            isChromeVisible = false,
            isLoading = false
        )

        composeTestRule.setContent {
            OpusTheme {
                ReaderScreen(
                    uiState = state,
                    engine = fakeEngine,
                    onBackClick = {},
                    onPreviousPage = { prevCalled = true },
                    onNextPage = { nextCalled = true },
                    onToggleChrome = {},
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = {},
                    onToggleThumbnails = {},
                    onWordLongPress = {},
                    onUpdateSelectionRange = {},
                    onClearSelection = {},
                    onAnnotationClick = {},
                    onCreateHighlight = {},
                    onChangeHighlightColor = { _, _ -> },
                    onDeleteAnnotation = {},
                    onOpenNoteEditor = {},
                    onDismissNoteEditor = {},
                    onSaveNote = { _, _ -> },
                    onToggleFavorite = {},
                    onSearchQueryChange = {},
                    onSelectSearchHit = {},
                    onNextSearchHit = {},
                    onPreviousSearchHit = {},
                    onSetAnnotationFilter = {},
                    onSetAnnotationSort = {}
                )
            }
        }

        // Tap left edge (x=10%)
        composeTestRule.onNodeWithTag("reader_gesture_surface").performTouchInput {
            click(percentOffset(0.10f, 0.5f))
        }
        assertTrue("Left edge tap must navigate to previous page", prevCalled)

        // Tap right edge (x=90%)
        composeTestRule.onNodeWithTag("reader_gesture_surface").performTouchInput {
            click(percentOffset(0.90f, 0.5f))
        }
        assertTrue("Right edge tap must navigate to next page", nextCalled)
    }

    @Test
    fun bookmarkButton_togglesBookmarkRibbonVisibility() {
        var bookmarkToggled = false
        val state = ReaderUiState(
            book = ReaderBookUiModel("id_1", "The Complete Malazan", "Steven Erikson", ""),
            currentPage = samplePage,
            progress = Progress(current = 91, total = 879, percent = 10f),
            isChromeVisible = true,
            isBookmarked = true,
            isLoading = false
        )

        composeTestRule.setContent {
            OpusTheme {
                ReaderScreen(
                    uiState = state,
                    engine = fakeEngine,
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = {},
                    onToggleBookmark = { bookmarkToggled = true },
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = {},
                    onToggleThumbnails = {},
                    onWordLongPress = {},
                    onUpdateSelectionRange = {},
                    onClearSelection = {},
                    onAnnotationClick = {},
                    onCreateHighlight = {},
                    onChangeHighlightColor = { _, _ -> },
                    onDeleteAnnotation = {},
                    onOpenNoteEditor = {},
                    onDismissNoteEditor = {},
                    onSaveNote = { _, _ -> },
                    onToggleFavorite = {},
                    onSearchQueryChange = {},
                    onSelectSearchHit = {},
                    onNextSearchHit = {},
                    onPreviousSearchHit = {},
                    onSetAnnotationFilter = {},
                    onSetAnnotationSort = {}
                )
            }
        }

        // Top bar bookmark button is displayed
        composeTestRule.onNodeWithTag("reader_bookmark_button")
            .assertIsDisplayed()
            .performClick()

        assertTrue("Bookmark button must trigger toggle", bookmarkToggled)

        // Bookmark ribbon is displayed on the page
        composeTestRule.onNodeWithTag("reader_bookmark_ribbon")
            .assertIsDisplayed()
    }

    @Test
    fun contentsButton_opensTocSheet_andSelectingChapterNavigates() {
        var sheetOpened: ActiveReaderSheet? = null
        var jumpedLocator: Locator? = null

        val state = ReaderUiState(
            book = ReaderBookUiModel("id_1", "The Complete Malazan", "Steven Erikson", ""),
            currentPage = samplePage,
            progress = Progress(current = 91, total = 879, percent = 10f),
            isChromeVisible = true,
            activeSheet = ActiveReaderSheet.CONTENTS,
            tocEntries = sampleToc,
            isLoading = false
        )

        composeTestRule.setContent {
            OpusTheme {
                ReaderScreen(
                    uiState = state,
                    engine = fakeEngine,
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = {},
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = { sheetOpened = it },
                    onDismissSheet = {},
                    onJumpToToc = { jumpedLocator = it },
                    onToggleThumbnails = {},
                    onWordLongPress = {},
                    onUpdateSelectionRange = {},
                    onClearSelection = {},
                    onAnnotationClick = {},
                    onCreateHighlight = {},
                    onChangeHighlightColor = { _, _ -> },
                    onDeleteAnnotation = {},
                    onOpenNoteEditor = {},
                    onDismissNoteEditor = {},
                    onSaveNote = { _, _ -> },
                    onToggleFavorite = {},
                    onSearchQueryChange = {},
                    onSelectSearchHit = {},
                    onNextSearchHit = {},
                    onPreviousSearchHit = {},
                    onSetAnnotationFilter = {},
                    onSetAnnotationSort = {}
                )
            }
        }

        // Verify TOC header and rows
        composeTestRule.onNodeWithTag("contents_title")
            .assertIsDisplayed()
            .assertTextContains("The Complete Malazan")

        composeTestRule.onNodeWithTag("contents_row_1")
            .assertIsDisplayed()
            .assertTextContains("Chapter One")
            .performClick()

        assertEquals(Locator(1, 0), jumpedLocator)
    }

    @Test
    fun scrubberSlider_isDisplayedInChrome() {
        val state = ReaderUiState(
            book = ReaderBookUiModel("id_1", "The Complete Malazan", "Steven Erikson", ""),
            currentPage = samplePage,
            progress = Progress(current = 91, total = 879, percent = 10f),
            isChromeVisible = true,
            scrubberLocation = 91,
            totalLocations = 879,
            isLoading = false
        )

        composeTestRule.setContent {
            OpusTheme {
                ReaderScreen(
                    uiState = state,
                    engine = fakeEngine,
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = {},
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = {},
                    onToggleThumbnails = {},
                    onWordLongPress = {},
                    onUpdateSelectionRange = {},
                    onClearSelection = {},
                    onAnnotationClick = {},
                    onCreateHighlight = {},
                    onChangeHighlightColor = { _, _ -> },
                    onDeleteAnnotation = {},
                    onOpenNoteEditor = {},
                    onDismissNoteEditor = {},
                    onSaveNote = { _, _ -> },
                    onToggleFavorite = {},
                    onSearchQueryChange = {},
                    onSelectSearchHit = {},
                    onNextSearchHit = {},
                    onPreviousSearchHit = {},
                    onSetAnnotationFilter = {},
                    onSetAnnotationSort = {}
                )
            }
        }

        // Verify bottom bar location text and scrubber slider
        composeTestRule.onNodeWithTag("reader_location_text")
            .assertIsDisplayed()
            .assertTextContains("Location 91 of 879 • 10%")

        composeTestRule.onNodeWithTag("reader_scrubber_slider")
            .assertIsDisplayed()
    }

    @Test
    fun realEngineHitTest_tapsWordBoxCenter_returnsWordLocator() = runBlocking {
        val engine = FakeReaderEngine(viewportWidth = 1080, viewportHeight = 2202)
        engine.open(
            BookSource(id = "book_dev", filePath = testEpubFile.absolutePath),
            ReaderSettings()
        )

        val realPage = engine.pageAt(Locator(0, 0))
        assertTrue("Real page must contain words", realPage.words.isNotEmpty())

        val targetWord = realPage.words.first()
        val centerX = (targetWord.rect.left + targetWord.rect.right) / 2f
        val centerY = (targetWord.rect.top + targetWord.rect.bottom) / 2f

        var receivedLocator: Locator? = null
        val state = ReaderUiState(
            book = ReaderBookUiModel("id_1", "The Complete Malazan", "Steven Erikson", ""),
            currentPage = realPage,
            progress = Progress(current = 1, total = 100, percent = 1f),
            isChromeVisible = false,
            isLoading = false
        )

        composeTestRule.setContent {
            OpusTheme {
                ReaderScreen(
                    uiState = state,
                    engine = engine,
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = {},
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = {},
                    onToggleThumbnails = {},
                    onWordLongPress = { loc -> receivedLocator = loc },
                    onUpdateSelectionRange = {},
                    onClearSelection = {},
                    onAnnotationClick = {},
                    onCreateHighlight = {},
                    onChangeHighlightColor = { _, _ -> },
                    onDeleteAnnotation = {},
                    onOpenNoteEditor = {},
                    onDismissNoteEditor = {},
                    onSaveNote = { _, _ -> },
                    onToggleFavorite = {},
                    onSearchQueryChange = {},
                    onSelectSearchHit = {},
                    onNextSearchHit = {},
                    onPreviousSearchHit = {},
                    onSetAnnotationFilter = {},
                    onSetAnnotationSort = {}
                )
            }
        }

        // Tap the center of the known WordBox on the drawn canvas gesture surface
        composeTestRule.onNodeWithTag("reader_gesture_surface").performTouchInput {
            longClick(Offset(centerX, centerY))
        }

        assertNotNull("Word hit test must return a locator", receivedLocator)
        assertEquals(targetWord.start.spineIndex, receivedLocator!!.spineIndex)
        assertTrue(
            "Hit locator charOffset (${receivedLocator!!.charOffset}) must be within word bounds [${targetWord.start.charOffset}..${targetWord.end.charOffset}]",
            receivedLocator!!.charOffset >= targetWord.start.charOffset &&
                receivedLocator!!.charOffset <= targetWord.end.charOffset
        )
    }

    @Test
    fun selection_toolbarAppearsAndCreatesHighlight() {
        var createdColor: String? = null
        val state = ReaderUiState(
            book = ReaderBookUiModel("id_1", "The Complete Malazan", "Steven Erikson", ""),
            currentPage = samplePage,
            isChromeVisible = false,
            selectedRange = LocatorRange(Locator(0, 0), Locator(0, 6)),
            selectedText = "Ganoes",
            isLoading = false
        )

        composeTestRule.setContent {
            OpusTheme {
                ReaderScreen(
                    uiState = state,
                    engine = fakeEngine,
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = {},
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = {},
                    onToggleThumbnails = {},
                    onWordLongPress = {},
                    onUpdateSelectionRange = {},
                    onClearSelection = {},
                    onAnnotationClick = {},
                    onCreateHighlight = { createdColor = it },
                    onChangeHighlightColor = { _, _ -> },
                    onDeleteAnnotation = {},
                    onOpenNoteEditor = {},
                    onDismissNoteEditor = {},
                    onSaveNote = { _, _ -> },
                    onToggleFavorite = {},
                    onSearchQueryChange = {},
                    onSelectSearchHit = {},
                    onNextSearchHit = {},
                    onPreviousSearchHit = {},
                    onSetAnnotationFilter = {},
                    onSetAnnotationSort = {}
                )
            }
        }

        // Toolbar is displayed
        composeTestRule.onNodeWithTag("selection_toolbar").assertIsDisplayed()

        // Click yellow swatch
        composeTestRule.onNodeWithTag("swatch_F9E054").assertIsDisplayed().performClick()
        assertEquals("#F9E054", createdColor)
    }

    @Test
    fun searchScreen_rendersResultsAndNavigatesToHit() {
        var selectedHit: Int? = null
        val state = ReaderUiState(
            book = ReaderBookUiModel("id_1", "The Complete Malazan", "Steven Erikson", ""),
            currentPage = samplePage,
            isChromeVisible = false,
            activeSheet = ActiveReaderSheet.SEARCH,
            searchQuery = "Ganoes",
            searchResults = sampleHits,
            isLoading = false
        )

        composeTestRule.setContent {
            OpusTheme {
                ReaderScreen(
                    uiState = state,
                    engine = fakeEngine,
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = {},
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = {},
                    onToggleThumbnails = {},
                    onWordLongPress = {},
                    onUpdateSelectionRange = {},
                    onClearSelection = {},
                    onAnnotationClick = {},
                    onCreateHighlight = {},
                    onChangeHighlightColor = { _, _ -> },
                    onDeleteAnnotation = {},
                    onOpenNoteEditor = {},
                    onDismissNoteEditor = {},
                    onSaveNote = { _, _ -> },
                    onToggleFavorite = {},
                    onSearchQueryChange = {},
                    onSelectSearchHit = { selectedHit = it },
                    onNextSearchHit = {},
                    onPreviousSearchHit = {},
                    onSetAnnotationFilter = {},
                    onSetAnnotationSort = {}
                )
            }
        }

        // Verify search screen
        composeTestRule.onNodeWithTag("reader_search_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search_hit_0")
            .assertIsDisplayed()
            .assertTextContains("Location 0")
            .performClick()

        assertEquals(0, selectedHit)
    }

    @Test
    fun annotationsScreen_rendersAnnotationsList() {
        var jumpedLocator: Locator? = null
        val state = ReaderUiState(
            book = ReaderBookUiModel("id_1", "The Complete Malazan", "Steven Erikson", ""),
            currentPage = samplePage,
            isChromeVisible = false,
            activeSheet = ActiveReaderSheet.ANNOTATIONS,
            annotations = sampleAnnotations,
            isLoading = false
        )

        composeTestRule.setContent {
            OpusTheme {
                ReaderScreen(
                    uiState = state,
                    engine = fakeEngine,
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = {},
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = { jumpedLocator = it },
                    onToggleThumbnails = {},
                    onWordLongPress = {},
                    onUpdateSelectionRange = {},
                    onClearSelection = {},
                    onAnnotationClick = {},
                    onCreateHighlight = {},
                    onChangeHighlightColor = { _, _ -> },
                    onDeleteAnnotation = {},
                    onOpenNoteEditor = {},
                    onDismissNoteEditor = {},
                    onSaveNote = { _, _ -> },
                    onToggleFavorite = {},
                    onSearchQueryChange = {},
                    onSelectSearchHit = {},
                    onNextSearchHit = {},
                    onPreviousSearchHit = {},
                    onSetAnnotationFilter = {},
                    onSetAnnotationSort = {}
                )
            }
        }

        // Verify annotations screen
        composeTestRule.onNodeWithTag("annotations_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("annotation_item_annot_1")
            .assertIsDisplayed()
            .assertTextContains("Ganoes")
            .performClick()

        assertEquals(Locator(0, 0), jumpedLocator)
    }

    @Test
    fun annotationsFilterAndSortSheets_renderAndTriggerCallbacks() {
        var selectedFilter: AnnotationFilter? = null
        val filterState = ReaderUiState(
            book = ReaderBookUiModel("id_1", "The Complete Malazan", "Steven Erikson", ""),
            currentPage = samplePage,
            activeSheet = ActiveReaderSheet.ANNOTATIONS_FILTER,
            annotations = sampleAnnotations,
            isLoading = false
        )

        composeTestRule.setContent {
            OpusTheme {
                ReaderScreen(
                    uiState = filterState,
                    engine = fakeEngine,
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = {},
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = {},
                    onToggleThumbnails = {},
                    onWordLongPress = {},
                    onUpdateSelectionRange = {},
                    onClearSelection = {},
                    onAnnotationClick = {},
                    onCreateHighlight = {},
                    onChangeHighlightColor = { _, _ -> },
                    onDeleteAnnotation = {},
                    onOpenNoteEditor = {},
                    onDismissNoteEditor = {},
                    onSaveNote = { _, _ -> },
                    onToggleFavorite = {},
                    onSearchQueryChange = {},
                    onSelectSearchHit = {},
                    onNextSearchHit = {},
                    onPreviousSearchHit = {},
                    onSetAnnotationFilter = { selectedFilter = it },
                    onSetAnnotationSort = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("annotations_filter_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("filter_opt_highlights").assertIsDisplayed().performClick()
        assertEquals(AnnotationFilter.HIGHLIGHTS, selectedFilter)
    }
}
