package app.opusreader.design.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.opusreader.design.component.OpusBottomSheet
import app.opusreader.design.component.OpusBottomNavBar
import app.opusreader.design.component.OpusCheckboxRow
import app.opusreader.design.component.OpusChevronRow
import app.opusreader.design.component.OpusChip
import app.opusreader.design.component.OpusCoverTile
import app.opusreader.design.component.OpusListRow
import app.opusreader.design.component.OpusNavItem
import app.opusreader.design.component.OpusResumeCover
import app.opusreader.design.component.OpusSearchPill
import app.opusreader.design.component.OpusSheetValue
import app.opusreader.design.component.OpusSlider
import app.opusreader.design.component.OpusToggleRow
import app.opusreader.design.component.OpusTopBar
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

@Composable
fun ComponentGalleryScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedNavId by remember { mutableIntStateOf(1) }
    var checkboxState by remember { mutableStateOf(true) }
    var toggleState by remember { mutableStateOf(false) }
    var chipSelected by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(14f) }
    var sheetVisible by remember { mutableStateOf(false) }
    var sheetValue by remember { mutableStateOf(OpusSheetValue.EXPANDED) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OpusColors.BackgroundDark)
            .testTag("gallery_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. OpusTopBar
            OpusTopBar(
                title = "Design Gallery",
                modifier = Modifier.testTag("gallery_top_bar")
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingMedium))

            // 2. OpusSearchPill
            Box(modifier = Modifier.padding(horizontal = OpusDimensions.ScreenMarginHorizontal)) {
                OpusSearchPill(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClear = { searchQuery = "" },
                    placeholder = "Search components...",
                    onSearchAction = { },
                    modifier = Modifier.testTag("gallery_search_pill")
                )
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingMedium))

            // 3. OpusChips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingSmall)
            ) {
                OpusChip(
                    label = "Books",
                    selected = !chipSelected,
                    onClick = { chipSelected = false },
                    modifier = Modifier.testTag("gallery_chip_books")
                )
                OpusChip(
                    label = "Downloaded",
                    selected = chipSelected,
                    onClick = { chipSelected = true },
                    modifier = Modifier.testTag("gallery_chip_downloaded")
                )
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingMedium))

            // 4. Rows (ListRow, ChevronRow, CheckboxRow, ToggleRow)
            OpusListRow(
                title = "Standard List Row",
                subtitle = "Subtitle with Amazon Ember typography",
                onClick = { sheetVisible = true },
                modifier = Modifier.testTag("gallery_list_row")
            )

            OpusChevronRow(
                title = "Chevron Setting Row",
                subtitle = "Opens details",
                trailingValue = "Enabled",
                onClick = {
                    sheetValue = if (sheetValue == OpusSheetValue.EXPANDED) OpusSheetValue.PEEK else OpusSheetValue.EXPANDED
                    sheetVisible = true
                },
                modifier = Modifier.testTag("gallery_chevron_row")
            )

            OpusCheckboxRow(
                title = "Checkbox Option",
                checked = checkboxState,
                onCheckedChange = { checkboxState = it },
                modifier = Modifier.testTag("gallery_checkbox_row")
            )

            OpusToggleRow(
                title = "Continuous Scrolling",
                checked = toggleState,
                onCheckedChange = { toggleState = it },
                modifier = Modifier.testTag("gallery_toggle_row")
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingMedium))

            // 5. Sliders
            OpusSlider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 8f..32f,
                steps = 12,
                modifier = Modifier.testTag("gallery_slider")
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingMedium))

            // 6. Covers (Grid, Sample, New, Resume Cover)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingStandard)
            ) {
                OpusCoverTile(
                    title = "Pride and Prejudice",
                    author = "Jane Austen",
                    isDownloaded = true,
                    isSample = false,
                    isNew = true,
                    progressPercent = 0.45f,
                    onClick = { },
                    onLongClick = { },
                    modifier = Modifier.testTag("gallery_cover_tile")
                )

                OpusResumeCover(
                    title = "War and Peace",
                    onResumeClick = { },
                    modifier = Modifier.testTag("gallery_resume_cover")
                )
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingLarge))

            // 7. Bottom Navigation
            OpusBottomNavBar(
                items = listOf(
                    OpusNavItem(0, "Home", Icons.Default.Home, "gallery_nav_home"),
                    OpusNavItem(1, "Library", Icons.Default.LocalLibrary, "gallery_nav_library"),
                    OpusNavItem(2, "More", Icons.Default.MoreHoriz, "gallery_nav_more")
                ),
                selectedId = selectedNavId,
                onItemSelected = { selectedNavId = it },
                modifier = Modifier.testTag("gallery_bottom_nav")
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingXXLarge))
        }

        // 8. Bottom Sheet
        OpusBottomSheet(
            visible = sheetVisible,
            onDismissRequest = { sheetVisible = false },
            sheetValue = sheetValue,
            modifier = Modifier.testTag("gallery_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(OpusDimensions.ScreenMarginHorizontal)
            ) {
                Text(
                    text = "Sheet Content",
                    style = OpusTypography.headlineMedium,
                    color = OpusColors.TextPrimaryDark,
                    modifier = Modifier.testTag("gallery_sheet_title")
                )
                Text(
                    text = "This is a real bottom sheet with drag handle, scrim, and dynamic height.",
                    style = OpusTypography.bodyMedium,
                    color = OpusColors.TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(OpusDimensions.SpacingLarge))
            }
        }
    }
}
