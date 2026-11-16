package app.opusreader.design.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions

enum class OpusSheetValue {
    PEEK,
    EXPANDED
}

@Composable
fun OpusBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetValue: OpusSheetValue = OpusSheetValue.EXPANDED,
    onSheetValueChange: ((OpusSheetValue) -> Unit)? = null,
    peekHeight: Dp = 440.dp,
    expandedHeight: Dp = 680.dp,
    backgroundColor: Color = OpusColors.SurfaceDark,
    content: @Composable ColumnScope.() -> Unit
) {
    if (visible) {
        BackHandler {
            onDismissRequest()
        }

        var dragOffsetY by remember { mutableFloatStateOf(0f) }
        val draggableState = rememberDraggableState { delta ->
            if (delta > 0 || dragOffsetY > 0) {
                dragOffsetY += delta
                if (dragOffsetY > 150f) {
                    onDismissRequest()
                    dragOffsetY = 0f
                }
            }
        }

        val currentHeight = if (sheetValue == OpusSheetValue.PEEK) peekHeight else expandedHeight
        val shape = RoundedCornerShape(
            topStart = OpusDimensions.SheetCornerRadius,
            topEnd = OpusDimensions.SheetCornerRadius
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Full scrim behind sheet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OpusColors.ScrimDark)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismissRequest() }
                    .testTag("sheet_scrim")
            )

            // Top area dismiss zone explicitly above sheet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.TopCenter)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismissRequest() }
                    .testTag("sheet_scrim_top")
            )

            // Bottom sheet container
            Column(
                modifier = modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .heightIn(max = currentHeight)
                    .clip(shape)
                    .background(backgroundColor)
                    .pointerInput(Unit) {
                        detectTapGestures { /* Consume taps on sheet body without merging child semantics */ }
                    }
                    .draggable(
                        state = draggableState,
                        orientation = Orientation.Vertical
                    )
                    .testTag("sheet_container"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag Handle
                Spacer(modifier = Modifier.height(OpusDimensions.SheetHandlePaddingTop))
                Box(
                    modifier = Modifier
                        .width(OpusDimensions.SheetHandleWidth)
                        .height(OpusDimensions.SheetHandleHeight)
                        .clip(RoundedCornerShape(2.dp))
                        .background(OpusColors.DragHandleDark)
                        .testTag("sheet_drag_handle")
                )
                Spacer(modifier = Modifier.height(OpusDimensions.SheetHandlePaddingBottom))

                content()
            }
        }
    }
}
