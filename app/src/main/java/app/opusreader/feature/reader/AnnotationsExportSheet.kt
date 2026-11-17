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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import app.opusreader.design.component.OpusBottomSheet
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

@Composable
fun AnnotationsExportSheet(
    onExportMarkdown: () -> Unit,
    onExportText: () -> Unit,
    onShareEmail: () -> Unit,
    onDismiss: () -> Unit
) {
    OpusBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        expandedHeight = OpusDimensions.AnnotationsShareSheetHeight,
        backgroundColor = OpusColors.AnnotationsSortSheetDark,
        scrimColor = OpusColors.ScrimAnnotationsDark,
        showDragHandle = false,
        modifier = Modifier.testTag("annotations_export_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OpusDimensions.SpacingLarge)
        ) {
            // Header: Export Notes on left, X on right (matching D16 geometry)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Export Notes",
                    style = OpusTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OpusColors.TextPrimaryDark,
                    modifier = Modifier.testTag("annotations_export_title")
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("annotations_export_close")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = OpusColors.TextPrimaryDark,
                        modifier = Modifier.size(OpusDimensions.IconMedium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingStandard))
            HorizontalDivider(thickness = OpusDimensions.SheetDividerThickness, color = OpusColors.DividerDark)

            ExportOptionRow(
                icon = Icons.Default.Email,
                label = "Share via Email",
                onClick = onShareEmail,
                tag = "export_opt_email"
            )
            HorizontalDivider(thickness = OpusDimensions.SheetDividerThickness, color = OpusColors.DividerDark)

            ExportOptionRow(
                icon = Icons.Default.FormatAlignLeft,
                label = "Export as Markdown",
                onClick = onExportMarkdown,
                tag = "export_opt_markdown"
            )
            HorizontalDivider(thickness = OpusDimensions.SheetDividerThickness, color = OpusColors.DividerDark)

            ExportOptionRow(
                icon = Icons.Default.Share,
                label = "Export as Plain Text",
                onClick = onExportText,
                tag = "export_opt_text"
            )
            HorizontalDivider(thickness = OpusDimensions.SheetDividerThickness, color = OpusColors.DividerDark)
        }
    }
}

@Composable
private fun ExportOptionRow(
    icon: ImageVector,
    label: String,
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OpusColors.TextPrimaryDark,
            modifier = Modifier.size(OpusDimensions.IconSmall)
        )
        Spacer(modifier = Modifier.width(OpusDimensions.SpacingMedium))
        Text(
            text = label,
            style = OpusTypography.bodyLarge,
            color = OpusColors.TextPrimaryDark
        )
    }
}
