package app.opusreader.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

data class HighlightColorOption(
    val colorHex: String,
    val color: Color
)

val DefaultHighlightColors = listOf(
    HighlightColorOption("#F9E054", OpusColors.HighlightYellow),
    HighlightColorOption("#68B6EF", OpusColors.HighlightAnnotationBlue),
    HighlightColorOption("#F48FB1", OpusColors.HighlightPink),
    HighlightColorOption("#FFA726", OpusColors.HighlightOrange)
)

@Composable
fun OpusSelectionToolbar(
    onColorSelected: (String) -> Unit,
    onAddNoteClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(OpusDimensions.CornerRadiusSmall))
            .background(OpusColors.SurfaceDark)
            .border(
                width = OpusDimensions.DividerThickness,
                color = OpusColors.BorderDark,
                shape = RoundedCornerShape(OpusDimensions.CornerRadiusSmall)
            )
            .padding(horizontal = OpusDimensions.SpacingSmall, vertical = OpusDimensions.SpacingXSmall)
            .testTag("selection_toolbar")
    ) {
        // Color row
        Row(
            modifier = Modifier
                .padding(vertical = OpusDimensions.SpacingXXSmall),
            horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultHighlightColors.forEach { option ->
                Box(
                    modifier = Modifier
                        .size(OpusDimensions.ColorSwatchSize)
                        .clip(CircleShape)
                        .background(option.color)
                        .clickable { onColorSelected(option.colorHex) }
                        .testTag("swatch_${option.colorHex.removePrefix("#")}")
                )
            }

            if (onDeleteClick != null) {
                Spacer(modifier = Modifier.width(OpusDimensions.SpacingSmall))
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(OpusDimensions.ColorSwatchSize)
                        .testTag("selection_action_delete")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete highlight",
                        tint = OpusColors.TextSecondaryDark,
                        modifier = Modifier.size(OpusDimensions.IconSmall)
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = OpusDimensions.DividerThickness,
            color = OpusColors.DividerDark,
            modifier = Modifier.padding(vertical = OpusDimensions.SpacingXXSmall)
        )

        // Actions row
        Row(
            horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionActionButton(
                icon = Icons.Default.Edit,
                label = "Note",
                onClick = onAddNoteClick,
                tag = "selection_action_note"
            )
            SelectionActionButton(
                icon = Icons.Default.Search,
                label = "Search",
                onClick = onSearchClick,
                tag = "selection_action_search"
            )
            SelectionActionButton(
                icon = Icons.Default.ContentCopy,
                label = "Copy",
                onClick = onCopyClick,
                tag = "selection_action_copy"
            )
            SelectionActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = onShareClick,
                tag = "selection_action_share"
            )
        }
    }
}

@Composable
private fun SelectionActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = OpusDimensions.SpacingXSmall, vertical = OpusDimensions.SpacingXXSmall)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = OpusColors.TextPrimaryDark,
            modifier = Modifier.size(OpusDimensions.IconSmall)
        )
        Spacer(modifier = Modifier.width(OpusDimensions.SpacingXXSmall))
        Text(
            text = label,
            style = OpusTypography.labelSmall,
            color = OpusColors.TextPrimaryDark
        )
    }
}
