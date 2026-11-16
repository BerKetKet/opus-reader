package app.opusreader

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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
}
