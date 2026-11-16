package app.opusreader.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

@Composable
fun OpusChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(OpusDimensions.ChipCornerRadius)
    val backgroundColor = if (selected) OpusColors.AccentBlue else OpusColors.SurfaceDark
    val textColor = if (selected) OpusColors.TextPrimaryDark else OpusColors.TextSecondaryDark
    val borderModifier = if (selected) Modifier else Modifier.border(1.dp, OpusColors.BorderDark, shape)

    Box(
        modifier = modifier
            .height(OpusDimensions.ChipHeight)
            .clip(shape)
            .background(backgroundColor)
            .then(borderModifier)
            .clickable { onClick() }
            .padding(horizontal = OpusDimensions.SpacingStandard),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Box(modifier = Modifier.padding(end = OpusDimensions.SpacingSmall)) {
                    leadingIcon()
                }
            }
            Text(
                text = label,
                style = OpusTypography.labelLarge,
                color = textColor
            )
        }
    }
}
