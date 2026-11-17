package app.opusreader.feature.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import app.opusreader.design.component.OpusBottomSheet
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

@Composable
fun AnnotationsFilterSheet(
    currentFilter: AnnotationFilter,
    totalCount: Int,
    highlightsCount: Int,
    notesCount: Int,
    bookmarksCount: Int,
    starredCount: Int,
    onSelectFilter: (AnnotationFilter) -> Unit,
    onDismiss: () -> Unit
) {
    OpusBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        expandedHeight = OpusDimensions.AnnotationsFilterSheetHeight,
        backgroundColor = OpusColors.BackgroundDark,
        scrimColor = OpusColors.ScrimAnnotationsDark,
        showDragHandle = false,
        modifier = Modifier.testTag("annotations_filter_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OpusDimensions.SpacingMedium)
        ) {
            // Header: Filter on left, X on right (matching D14, y=1190..1310)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(OpusDimensions.IconButtonMedium)
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter",
                    style = OpusTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OpusColors.TextPrimaryDark,
                    modifier = Modifier.testTag("annotations_filter_title")
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("annotations_filter_close")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = OpusColors.TextPrimaryDark,
                        modifier = Modifier.size(OpusDimensions.IconMedium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingSmall))

            HorizontalDivider(
                thickness = OpusDimensions.SheetDividerThickness,
                color = OpusColors.DividerDark
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingMedium))

            // Section: Type
            Text(
                text = "Type",
                style = OpusTypography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = OpusColors.TextSecondaryDark,
                modifier = Modifier.padding(horizontal = OpusDimensions.ScreenMarginHorizontal)
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingSmall))

            // Row 1 of Type
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingSmall)
            ) {
                FilterPill(
                    label = "Favorite ($starredCount)",
                    selected = currentFilter == AnnotationFilter.STARRED,
                    onClick = {
                        onSelectFilter(if (currentFilter == AnnotationFilter.STARRED) AnnotationFilter.ALL else AnnotationFilter.STARRED)
                    },
                    tag = "filter_opt_starred"
                )
                FilterPill(
                    label = "Note ($notesCount)",
                    selected = currentFilter == AnnotationFilter.NOTES,
                    onClick = {
                        onSelectFilter(if (currentFilter == AnnotationFilter.NOTES) AnnotationFilter.ALL else AnnotationFilter.NOTES)
                    },
                    tag = "filter_opt_notes"
                )
                FilterPill(
                    label = "Highlight ($highlightsCount)",
                    selected = currentFilter == AnnotationFilter.HIGHLIGHTS,
                    onClick = {
                        onSelectFilter(if (currentFilter == AnnotationFilter.HIGHLIGHTS) AnnotationFilter.ALL else AnnotationFilter.HIGHLIGHTS)
                    },
                    tag = "filter_opt_highlights"
                )
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingSmall))

            // Row 2 of Type
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingSmall)
            ) {
                FilterPill(
                    label = "Underline (0)",
                    selected = false,
                    onClick = { onSelectFilter(AnnotationFilter.ALL) },
                    tag = "filter_opt_underline"
                )
                FilterPill(
                    label = "Outline (0)",
                    selected = false,
                    onClick = { onSelectFilter(AnnotationFilter.ALL) },
                    tag = "filter_opt_outline"
                )
                FilterPill(
                    label = "Bookmark ($bookmarksCount)",
                    selected = currentFilter == AnnotationFilter.BOOKMARKS,
                    onClick = {
                        onSelectFilter(if (currentFilter == AnnotationFilter.BOOKMARKS) AnnotationFilter.ALL else AnnotationFilter.BOOKMARKS)
                    },
                    tag = "filter_opt_bookmarks"
                )
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingMedium))

            HorizontalDivider(
                thickness = OpusDimensions.SheetDividerThickness,
                color = OpusColors.DividerDark
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingMedium))

            // Section: Color
            Text(
                text = "Color",
                style = OpusTypography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = OpusColors.TextSecondaryDark,
                modifier = Modifier.padding(horizontal = OpusDimensions.ScreenMarginHorizontal)
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingSmall))

            // Row 1 of Color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingSmall)
            ) {
                ColorFilterPill(label = "Default (0)", dotColor = OpusColors.HighlightDefault, tag = "color_opt_default")
                ColorFilterPill(label = "Gray (0)", dotColor = OpusColors.HighlightGray, tag = "color_opt_gray")
                ColorFilterPill(label = "Red (0)", dotColor = OpusColors.HighlightRed, tag = "color_opt_red")
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingSmall))

            // Row 2 of Color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingSmall)
            ) {
                ColorFilterPill(label = "Orange (0)", dotColor = OpusColors.HighlightOrange, tag = "color_opt_orange")
                ColorFilterPill(label = "Yellow ($highlightsCount)", dotColor = OpusColors.HighlightYellow, tag = "color_opt_yellow")
                ColorFilterPill(label = "Green (0)", dotColor = OpusColors.HighlightGreen, tag = "color_opt_green")
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingSmall))

            // Row 3 of Color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingSmall)
            ) {
                ColorFilterPill(label = "Aqua (0)", dotColor = OpusColors.HighlightAqua, tag = "color_opt_aqua")
                ColorFilterPill(label = "Blue ($bookmarksCount)", dotColor = OpusColors.HighlightAnnotationBlue, tag = "color_opt_blue")
                ColorFilterPill(label = "Purple (0)", dotColor = OpusColors.HighlightPurple, tag = "color_opt_purple")
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val borderColor = if (selected) OpusColors.AccentBlue else OpusColors.BorderDark
    val bg = if (selected) OpusColors.AccentBlue.copy(alpha = 0.15f) else Color.Transparent

    Box(
        modifier = Modifier
            .height(OpusDimensions.AnnotationsFilterChipHeight)
            .border(width = OpusDimensions.DividerThickness, color = borderColor, shape = RoundedCornerShape(OpusDimensions.ChipCornerRadius))
            .background(bg, shape = RoundedCornerShape(OpusDimensions.ChipCornerRadius))
            .clickable(onClick = onClick)
            .padding(horizontal = OpusDimensions.SpacingMedium)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = OpusTypography.bodyMedium,
            color = if (selected) OpusColors.AccentBlue else OpusColors.TextPrimaryDark
        )
    }
}

@Composable
private fun ColorFilterPill(
    label: String,
    dotColor: Color,
    tag: String
) {
    Box(
        modifier = Modifier
            .height(OpusDimensions.AnnotationsFilterChipHeight)
            .border(width = OpusDimensions.DividerThickness, color = OpusColors.BorderDark, shape = RoundedCornerShape(OpusDimensions.ChipCornerRadius))
            .padding(horizontal = OpusDimensions.SpacingMedium)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingSmall)
        ) {
            Box(
                modifier = Modifier
                    .size(OpusDimensions.AnnotationsColorDotSize)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                style = OpusTypography.bodyMedium,
                color = OpusColors.TextPrimaryDark
            )
        }
    }
}
