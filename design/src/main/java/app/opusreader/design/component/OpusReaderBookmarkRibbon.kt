package app.opusreader.design.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.opusreader.design.theme.OpusColors

@Composable
fun OpusReaderBookmarkRibbon(
    isBookmarked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isBookmarked) return

    Box(
        modifier = modifier
            .size(width = 36.dp, height = 56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .testTag("reader_bookmark_ribbon")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val cutout = height * 0.25f

            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(width, 0f)
                lineTo(width, height)
                lineTo(width / 2f, height - cutout)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = path,
                color = OpusColors.BookmarkRed
            )
        }
    }
}
