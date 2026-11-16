package app.opusreader.design.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

@Composable
fun OpusListRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    showDivider: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = OpusDimensions.ListRowMinHeight)
                .padding(horizontal = OpusDimensions.ScreenMarginHorizontal, vertical = OpusDimensions.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingContent != null) {
                Box(modifier = Modifier.padding(end = OpusDimensions.SpacingStandard)) {
                    leadingContent()
                }
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = OpusTypography.titleMedium,
                    color = OpusColors.TextPrimaryDark
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = OpusTypography.bodyMedium,
                        color = OpusColors.TextSecondaryDark
                    )
                }
            }
            if (trailingContent != null) {
                Box(modifier = Modifier.padding(start = OpusDimensions.SpacingSmall)) {
                    trailingContent()
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(
                thickness = OpusDimensions.DividerThickness,
                color = OpusColors.DividerDark
            )
        }
    }
}
