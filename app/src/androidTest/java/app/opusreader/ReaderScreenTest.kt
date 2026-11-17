package app.opusreader

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.opusreader.design.theme.OpusTheme
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.Page
import app.opusreader.engine.model.Progress
import app.opusreader.engine.model.TocEntry
import app.opusreader.engine.model.WordBox
import app.opusreader.feature.reader.ActiveReaderSheet
import app.opusreader.feature.reader.ReaderBookUiModel
import app.opusreader.feature.reader.ReaderScreen
import app.opusreader.feature.reader.ReaderUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaderScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleWords = listOf(
        WordBox("Ganoes", android.graphics.RectF(), Locator(0, 0), Locator(0, 6)),
        WordBox("asked", android.graphics.RectF(), Locator(0, 7), Locator(0, 12)),
        WordBox("boldly.", android.graphics.RectF(), Locator(0, 13), Locator(0, 20))
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

    @Test
    fun readerCanvas_rendersPageTextAndRespondsToCenterTap_toToggleChrome() {
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
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = { chromeToggled = true },
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = {}
                )
            }
        }

        // Verify page text is displayed
        composeTestRule.onNodeWithTag("reader_page_text")
            .assertIsDisplayed()
            .assertTextContains("Ganoes asked boldly.")

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
                    onBackClick = {},
                    onPreviousPage = { prevCalled = true },
                    onNextPage = { nextCalled = true },
                    onToggleChrome = {},
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = {}
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
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = {},
                    onToggleBookmark = { bookmarkToggled = true },
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = {}
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
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = {},
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = { sheetOpened = it },
                    onDismissSheet = {},
                    onJumpToToc = { jumpedLocator = it }
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
                    onBackClick = {},
                    onPreviousPage = {},
                    onNextPage = {},
                    onToggleChrome = {},
                    onToggleBookmark = {},
                    onScrubberChange = {},
                    onScrubberCommit = {},
                    onOpenSheet = {},
                    onDismissSheet = {},
                    onJumpToToc = {}
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
}
