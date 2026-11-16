package app.opusreader.design.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

@Composable
fun OpusToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    showDivider: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = OpusDimensions.ListRowMinHeight)
                .padding(horizontal = OpusDimensions.ScreenMarginHorizontal, vertical = OpusDimensions.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = OpusTypography.titleMedium,
                    color = OpusColors.TextPrimaryDark
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = OpusTypography.bodyMedium,
                        color = OpusColors.TextSecondaryDark
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = OpusColors.TextPrimaryDark,
                    checkedTrackColor = OpusColors.AccentBlue,
                    uncheckedThumbColor = OpusColors.TextSecondaryDark,
                    uncheckedTrackColor = OpusColors.SurfaceDark
                ),
                modifier = Modifier.testTag("toggle_${if (checked) "on" else "off"}")
            )
        }
        if (showDivider) {
            HorizontalDivider(
                thickness = OpusDimensions.DividerThickness,
                color = OpusColors.DividerDark
            )
        }
    }
}
