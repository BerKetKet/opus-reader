package app.opusreader.design.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Reference density for 1080x2340 @ 420 dpi is 2.625
internal val Float.refPx: Dp get() = (this / 2.625f).dp
internal val Int.refPx: Dp get() = (this.toFloat() / 2.625f).dp

object OpusDimensions {
    val SpacingXXSmall: Dp = 2.dp
    val SpacingXSmall: Dp = 4.dp
    val SpacingSmall: Dp = 8.dp
    val SpacingMedium: Dp = 12.dp
    val SpacingStandard: Dp = 16.dp
    val SpacingLarge: Dp = 20.dp
    val SpacingXLarge: Dp = 24.dp
    val SpacingXXLarge: Dp = 32.dp

    // Measured component dimensions
    val MinTouchTarget: Dp = 48.dp
    val TopBarHeight: Dp = 56.dp
    val ScreenMarginHorizontal: Dp = 44.refPx // ~16.8 dp
    val DividerThickness: Dp = 3.refPx // ~1.14 dp

    // Bottom Navigation Bar
    val BottomNavHeight: Dp = 166.refPx // ~63.24 dp
    val BottomNavIconSize: Dp = 24.dp

    // Search Bar
    val SearchBarHeight: Dp = 105.refPx // ~40.0 dp
    val SearchBarRadius: Dp = 52.5f.refPx // ~20.0 dp
    val SearchBarMarginHorizontal: Dp = 42.refPx // ~16.0 dp

    // Cover Tiles
    val GridCoverWidth: Dp = 279.refPx // ~106.3 dp
    val GridCoverHeight: Dp = 427.refPx // ~162.7 dp
    val GridRowPitch: Dp = 474.refPx // ~180.6 dp
    val GridRowGap: Dp = 47.refPx // ~17.9 dp
    val GridSideMargin: Dp = 75.refPx // ~28.6 dp

    val ListCoverWidth: Dp = 168.refPx // ~64.0 dp
    val ListCoverHeight: Dp = 253.refPx // ~96.4 dp
    val ListRowPitch: Dp = 318.refPx // ~121.1 dp

    val ResumeCoverWidth: Dp = 124.refPx // ~47.2 dp
    val ResumeCoverHeight: Dp = 209.refPx // ~79.6 dp
    val ResumeCoverOffsetBottom: Dp = 98.refPx // ~37.3 dp

    // Sheet
    val SheetHandleWidth: Dp = 32.dp
    val SheetHandleHeight: Dp = 4.dp
    val SheetHandlePaddingTop: Dp = 7.dp
    val SheetHandlePaddingBottom: Dp = 12.dp
    val SheetCornerRadius: Dp = 16.dp

    // Chips
    val ChipHeight: Dp = 36.dp
    val ChipCornerRadius: Dp = 18.dp

    // List rows
    val ListRowMinHeight: Dp = 56.dp
}
