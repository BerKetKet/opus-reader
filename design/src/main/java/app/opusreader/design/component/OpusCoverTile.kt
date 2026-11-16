package app.opusreader.design.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OpusCoverTile(
    title: String,
    author: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = OpusDimensions.GridCoverWidth,
    height: Dp = OpusDimensions.GridCoverHeight,
    isDownloaded: Boolean = true,
    isSample: Boolean = false,
    isNew: Boolean = false,
    progressPercent: Float = 0f,
    coverContent: (@Composable () -> Unit)? = null
) {
    val cornerShape = RoundedCornerShape(4.dp)

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(cornerShape)
            .background(OpusColors.SurfaceDark)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        if (coverContent != null) {
            coverContent()
        } else {
            // Elegant fallback styled book cover
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OpusColors.SurfaceVariantDark)
                    .padding(OpusDimensions.SpacingSmall),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        style = OpusTypography.titleSmall,
                        color = OpusColors.TextPrimaryDark,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = author,
                        style = OpusTypography.bodySmall,
                        color = OpusColors.TextSecondaryDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Ribbons: SAMPLE (top-left) or NEW (top-right)
        if (isSample) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(OpusColors.HighlightBlue, shape = RoundedCornerShape(bottomEnd = 4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "SAMPLE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else if (isNew) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(OpusColors.AccentGold, shape = RoundedCornerShape(bottomStart = 4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "NEW",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Download check badge (bottom-left)
        if (isDownloaded) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(OpusColors.AccentBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Downloaded",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // Reading Progress Bar along bottom edge
        if (progressPercent > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progressPercent.coerceIn(0f, 1f))
                        .background(OpusColors.AccentBlue)
                )
            }
        }
    }
}
