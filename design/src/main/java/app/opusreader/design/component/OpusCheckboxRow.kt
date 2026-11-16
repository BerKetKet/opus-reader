package app.opusreader.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

@Composable
fun OpusCheckboxRow(
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
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (checked) OpusColors.AccentBlue else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (checked) OpusColors.AccentBlue else OpusColors.TextSecondaryDark,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .testTag("checkbox_${if (checked) "checked" else "unchecked"}"),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Checked",
                        tint = OpusColors.TextPrimaryDark,
                        modifier = Modifier.size(16.dp)
                    )
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
