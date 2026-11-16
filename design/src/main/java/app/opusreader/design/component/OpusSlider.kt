package app.opusreader.design.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions

@Composable
fun OpusSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    tag: String = "opus_slider"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingContent != null) {
            leadingContent()
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = OpusColors.AccentBlue,
                activeTrackColor = OpusColors.AccentBlue,
                inactiveTrackColor = OpusColors.DividerDark
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = OpusDimensions.SpacingSmall)
                .testTag(tag)
        )
        if (trailingContent != null) {
            trailingContent()
        }
    }
}
