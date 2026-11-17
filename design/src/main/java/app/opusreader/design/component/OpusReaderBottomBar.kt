package app.opusreader.design.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.opusreader.design.R
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions

@Composable
fun OpusReaderBottomBar(
    currentLocation: Int,
    totalLocations: Int,
    percent: Float,
    onThumbnailsClick: () -> Unit,
    onScrubberChange: (Int) -> Unit,
    onScrubberCommit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var liveLocation by remember(currentLocation) { mutableFloatStateOf(currentLocation.toFloat()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(OpusDimensions.ReaderBottomBarHeight)
            .background(OpusColors.ReaderChromeBottomDark)
            .testTag("reader_bottom_bar"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Location indicator text
        val safeTotal = if (totalLocations > 0) totalLocations else 1
        val safePercent = percent.coerceIn(0f, 100f).toInt()
        val locationText = "Location ${liveLocation.toInt()} of $safeTotal • $safePercent%"

        Text(
            text = locationText,
            color = OpusColors.TextPrimaryDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .padding(top = OpusDimensions.ReaderLocationPaddingTop)
                .testTag("reader_location_text")
        )

        Spacer(modifier = Modifier.height(OpusDimensions.ReaderLocationToScrubberSpacing))

        // Scrubber Row (Birds-eye grid + Track)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(OpusDimensions.ReaderScrubberHeight)
                .testTag("reader_scrubber_container")
        ) {
            // Birds-eye grid icon
            Box(
                modifier = Modifier
                    .width(OpusDimensions.ReaderBirdsEyeWidth)
                    .height(OpusDimensions.ReaderScrubberHeight)
                    .align(Alignment.CenterStart)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onThumbnailsClick
                    )
                    .testTag("reader_thumbnails_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_reader_birds_eye),
                    contentDescription = "Thumbnails",
                    tint = Color(0xFFC5C5C5),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Scrubber Slider
            val trackStartX = with(density) { OpusDimensions.ReaderTrackStartX.toPx() }
            val trackEndX = with(density) { OpusDimensions.ReaderTrackEndX.toPx() }
            val trackWidth = (trackEndX - trackStartX).coerceAtLeast(1f)
            val trackYPx = with(density) { 21.dp.toPx() }
            val trackHeightPx = with(density) { 3.dp.toPx() }
            val thumbRadiusPx = with(density) { 8.dp.toPx() }

            val fraction = if (safeTotal > 1) {
                ((liveLocation - 1) / (safeTotal - 1)).coerceIn(0f, 1f)
            } else 0f
            val thumbXPx = trackStartX + fraction * trackWidth

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(OpusDimensions.ReaderScrubberHeight)
                    .pointerInput(safeTotal) {
                        detectTapGestures { offset ->
                            if (offset.x >= trackStartX && offset.x <= trackEndX) {
                                val newFraction = ((offset.x - trackStartX) / trackWidth).coerceIn(0f, 1f)
                                val newLoc = (1 + newFraction * (safeTotal - 1)).toInt()
                                liveLocation = newLoc.toFloat()
                                onScrubberChange(newLoc)
                                onScrubberCommit(newLoc)
                            }
                        }
                    }
                    .pointerInput(safeTotal) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                val newFraction = ((change.position.x - trackStartX) / trackWidth).coerceIn(0f, 1f)
                                val newLoc = (1 + newFraction * (safeTotal - 1)).toInt()
                                liveLocation = newLoc.toFloat()
                                onScrubberChange(newLoc)
                            },
                            onDragEnd = {
                                onScrubberCommit(liveLocation.toInt())
                            }
                        )
                    }
                    .testTag("reader_scrubber_slider")
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(OpusDimensions.ReaderScrubberHeight)) {
                    val activeEnd = (thumbXPx - thumbRadiusPx).coerceAtLeast(trackStartX)
                    val inactiveStart = (thumbXPx + thumbRadiusPx).coerceAtMost(trackEndX)

                    // Active track (blue)
                    if (activeEnd > trackStartX) {
                        drawRect(
                            color = OpusColors.ReaderScrubberTrackActive,
                            topLeft = Offset(trackStartX, trackYPx - trackHeightPx / 2f),
                            size = Size(activeEnd - trackStartX, trackHeightPx)
                        )
                    }

                    // Inactive track (grey)
                    if (inactiveStart < trackEndX) {
                        drawRect(
                            color = OpusColors.ReaderScrubberTrackInactive,
                            topLeft = Offset(inactiveStart, trackYPx - trackHeightPx / 2f),
                            size = Size(trackEndX - inactiveStart, trackHeightPx)
                        )
                    }

                    // Thumb circle
                    drawCircle(
                        color = Color.White,
                        radius = thumbRadiusPx,
                        center = Offset(thumbXPx, trackYPx)
                    )
                }

                // Location number under thumb
                Text(
                    text = "${liveLocation.toInt()}",
                    color = Color(0xFFA0A0A0),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (thumbXPx - with(density) { 30.dp.toPx() }).toInt(),
                                y = (trackYPx + thumbRadiusPx + with(density) { 2.dp.toPx() }).toInt()
                            )
                        }
                        .width(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom divider line at y=2276
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OpusColors.ReaderDividerDark)
        )
    }
}
