package app.opusreader.feature.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import app.opusreader.design.component.OpusBottomSheet
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

@Composable
fun AnnotationsSortSheet(
    currentSort: AnnotationSort,
    onSelectSort: (AnnotationSort) -> Unit,
    onDismiss: () -> Unit
) {
    OpusBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        expandedHeight = OpusDimensions.AnnotationsSortSheetHeight,
        backgroundColor = OpusColors.AnnotationsSortSheetDark,
        scrimColor = OpusColors.ScrimAnnotationsDark,
        showDragHandle = false,
        modifier = Modifier.testTag("annotations_sort_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OpusDimensions.SpacingLarge)
        ) {
            // Header: Sort (matching D15)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort",
                    style = OpusTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OpusColors.TextPrimaryDark,
                    modifier = Modifier.testTag("annotations_sort_title")
                )
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingStandard))

            SortOptionRow(
                label = "Location",
                selected = currentSort == AnnotationSort.LOCATION_ASC || currentSort == AnnotationSort.LOCATION_DESC,
                onClick = { onSelectSort(AnnotationSort.LOCATION_ASC) },
                tag = "sort_opt_loc_asc"
            )
            SortOptionRow(
                label = "Date created",
                selected = false,
                onClick = { onSelectSort(AnnotationSort.DATE_MODIFIED) },
                tag = "sort_opt_date_created"
            )
            SortOptionRow(
                label = "Date modified",
                selected = currentSort == AnnotationSort.DATE_MODIFIED,
                onClick = { onSelectSort(AnnotationSort.DATE_MODIFIED) },
                tag = "sort_opt_recent"
            )

            HorizontalDivider(
                thickness = OpusDimensions.SheetDividerThickness,
                color = OpusColors.DividerDark
            )

            SortOptionRow(
                label = "Ascending",
                selected = currentSort == AnnotationSort.LOCATION_ASC,
                onClick = { onSelectSort(AnnotationSort.LOCATION_ASC) },
                tag = "sort_opt_asc"
            )
            SortOptionRow(
                label = "Descending",
                selected = currentSort == AnnotationSort.LOCATION_DESC,
                onClick = { onSelectSort(AnnotationSort.LOCATION_DESC) },
                tag = "sort_opt_loc_desc"
            )
        }
    }
}

@Composable
private fun SortOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(OpusDimensions.SheetRowHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = OpusDimensions.ScreenMarginHorizontal)
            .testTag(tag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = OpusTypography.bodyLarge,
            color = OpusColors.TextPrimaryDark
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = OpusColors.LinkBlueDark,
                modifier = Modifier.size(OpusDimensions.IconSmall)
            )
        }
    }
}
