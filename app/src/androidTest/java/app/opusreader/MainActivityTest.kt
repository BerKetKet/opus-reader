package app.opusreader

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.opusreader.data.entity.BookEntity
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunches_andShowsHomeTab() {
        composeTestRule.onNodeWithTag("tab_home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_library").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_more").assertIsDisplayed()
        composeTestRule.onNodeWithTag("screen_home").assertIsDisplayed()
    }

    @Test
    fun appNavigatesBetweenTabs() {
        composeTestRule.onNodeWithTag("tab_library").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("screen_library").assertIsDisplayed()

        composeTestRule.onNodeWithTag("tab_more").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("screen_more").assertIsDisplayed()

        composeTestRule.onNodeWithTag("tab_home").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("screen_home").assertIsDisplayed()
    }

    @Test
    fun scrollPosition_preservedAcrossTabSwitches() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val app = context.applicationContext as OpusApplication
        val repo = app.container.libraryRepository

        val baseTimestamp = System.currentTimeMillis() + 10_000_000_000L
        runBlocking {
            for (i in 0 until 24) {
                repo.saveBook(
                    BookEntity(
                        id = "scroll_book_%02d".format(i),
                        title = "Scroll Book %02d".format(i),
                        author = "Author %02d".format(i),
                        filePath = "/fake/scroll_$i.epub",
                        isDownloaded = true,
                        lastReadTimestamp = baseTimestamp - (i * 1000L)
                    )
                )
            }
        }

        // Navigate to Library tab
        composeTestRule.onNodeWithTag("tab_library").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("screen_library").assertIsDisplayed()

        // Wait for first book to load and be displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("book_tile_scroll_book_00").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("book_tile_scroll_book_00").assertIsDisplayed()

        // Scroll the grid to item 12
        composeTestRule.onNodeWithTag("library_books_grid").performScrollToIndex(12)
        composeTestRule.waitForIdle()

        // Assert item 12 is displayed and item 0 is no longer displayed
        composeTestRule.onNodeWithTag("book_tile_scroll_book_12").assertIsDisplayed()
        composeTestRule.onNodeWithTag("book_tile_scroll_book_00").assertIsNotDisplayed()

        // Switch to Home tab
        composeTestRule.onNodeWithTag("tab_home").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("screen_home").assertIsDisplayed()

        // Switch back to Library tab
        composeTestRule.onNodeWithTag("tab_library").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("screen_library").assertIsDisplayed()

        // Assert that item 12 is still displayed and item 0 is still not displayed (scroll position was preserved!)
        composeTestRule.onNodeWithTag("book_tile_scroll_book_12").assertIsDisplayed()
        composeTestRule.onNodeWithTag("book_tile_scroll_book_00").assertIsNotDisplayed()
    }
}
