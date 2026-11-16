package app.opusreader.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.opusreader.design.component.OpusBottomSheet
import app.opusreader.design.gallery.ComponentGalleryScreen
import app.opusreader.design.theme.OpusTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentGalleryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun topBarAndNav_rendersCorrectly() {
        composeTestRule.setContent {
            OpusTheme {
                ComponentGalleryScreen()
            }
        }
        composeTestRule.onNodeWithTag("gallery_top_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("gallery_nav_home").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("gallery_nav_library").performScrollTo().performClick()
    }

    @Test
    fun searchPill_typingAndClearingWorks() {
        composeTestRule.setContent {
            OpusTheme {
                ComponentGalleryScreen()
            }
        }
        composeTestRule.onNodeWithTag("search_text_input").performTextInput("Austen")
        composeTestRule.onNodeWithTag("search_text_input").assertTextEquals("Austen")
        composeTestRule.onNodeWithTag("search_clear_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search_clear_button").performClick()
        composeTestRule.onNodeWithTag("search_text_input").assertTextEquals("")
        Espresso.closeSoftKeyboard()
    }

    @Test
    fun chipsAndToggles_selectionWorks() {
        composeTestRule.setContent {
            OpusTheme {
                ComponentGalleryScreen()
            }
        }
        composeTestRule.onNodeWithTag("gallery_chip_downloaded").performClick()
        composeTestRule.onNodeWithTag("gallery_chip_books").performClick()
        composeTestRule.onNodeWithTag("gallery_checkbox_row").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("gallery_toggle_row").performScrollTo().performClick()
    }

    @Test
    fun bottomSheet_openAndDismissWorks() {
        var visible by mutableStateOf(true)
        var dismissed by mutableStateOf(false)

        composeTestRule.setContent {
            OpusTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    OpusBottomSheet(
                        visible = visible,
                        onDismissRequest = {
                            visible = false
                            dismissed = true
                        }
                    ) {
                        Text(
                            text = "Sheet Title",
                            modifier = Modifier.testTag("test_sheet_title")
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("test_sheet_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sheet_drag_handle").assertIsDisplayed()

        // Dismiss via top scrim area
        composeTestRule.onNodeWithTag("sheet_scrim_top").performClick()
        composeTestRule.waitForIdle()

        assertTrue(dismissed)
        composeTestRule.onNodeWithTag("test_sheet_title").assertDoesNotExist()
    }
}
