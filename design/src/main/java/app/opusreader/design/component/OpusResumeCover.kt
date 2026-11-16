package app.opusreader.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

@Composable
fun OpusResumeCover(
    title: String,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier,
    coverContent: (@Composable () -> Unit)? = null
) {
    val cornerShape = RoundedCornerShape(3.dp)

    Card(
        shape = cornerShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .width(OpusDimensions.ResumeCoverWidth)
            .height(OpusDimensions.ResumeCoverHeight)
            .clickable { onResumeClick() }
            .testTag("resume_cover_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(cornerShape)
                .background(OpusColors.SurfaceVariantDark)
        ) {
            if (coverContent != null) {
                coverContent()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = OpusTypography.bodySmall,
                        color = OpusColors.TextPrimaryDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
