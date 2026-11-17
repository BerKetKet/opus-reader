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

    // Corner Radii
    val CornerRadiusXSmall: Dp = 2.dp
    val CornerRadiusSmall: Dp = 4.dp

    // Icon sizes
    val IconSmall: Dp = 20.dp
    val IconMedium: Dp = 24.dp
    val IconButtonSmall: Dp = 40.dp
    val IconButtonMedium: Dp = 48.dp

    // Measured component dimensions
    val MinTouchTarget: Dp = 48.dp
    val TopBarHeight: Dp = 171.refPx
    val TopBarActionsEndMargin: Dp = 48.refPx
    val SearchToDividerSpacing: Dp = 42.refPx
    val DividerToGridSpacing: Dp = 68.refPx
    val DividerToListSpacing: Dp = 33.refPx
    val ScreenMarginHorizontal: Dp = 44.refPx // ~16.8 dp
    val DividerThickness: Dp = 1.dp
    val SheetDividerThickness: Dp = 1.dp

    // Bottom Navigation Bar
    val BottomNavHeight: Dp = 62.dp // 61dp background + 1dp top border (~163 refPx)
    val BottomNavIconSize: Dp = 24.dp

    // Search Bar
    val SearchBarHeight: Dp = 105.refPx // ~40.0 dp
    val SearchBarRadius: Dp = 52.5f.refPx // ~20.0 dp
    val SearchBarMarginHorizontal: Dp = 42.refPx // ~16.0 dp

    // Cover Tiles
    val GridCoverWidth: Dp = 279.refPx // ~106.3 dp
    val GridCoverHeight: Dp = 427.refPx // ~162.7 dp
    val GridColumnGap: Dp = 46.refPx
    val GridRowPitch: Dp = 474.refPx // ~180.6 dp
    val GridRowGap: Dp = 47.refPx // ~17.9 dp
    val GridSideMargin: Dp = 75.refPx // ~28.6 dp

    val ListCoverWidth: Dp = 168.refPx // ~64.0 dp
    val ListCoverHeight: Dp = 253.refPx // ~96.4 dp
    val ListRowPitch: Dp = 318.refPx // ~121.1 dp

    val ResumeCoverWidth: Dp = 124.refPx // ~47.2 dp
    val ResumeCoverHeight: Dp = 207.refPx // ~78.8 dp
    val ResumeCoverOffsetBottom: Dp = 34.refPx // ~13.0 dp (calibrated for 6dp card elevation to align bottom at y=2242)
    val ResumeCoverMarginStart: Dp = 73.refPx // ~27.8 dp

    // Sheet
    val SheetHandleWidth: Dp = 32.dp
    val SheetHandleHeight: Dp = 4.dp
    val SheetHandlePaddingTop: Dp = 7.dp
    val SheetHandlePaddingBottom: Dp = 12.dp
    val SheetCornerRadius: Dp = 10.dp
    val FilterSheetHeight: Dp = 1200.refPx // ~457.1 dp, top aligns at y=1076
    val ViewSortSheetHeight: Dp = 1518.refPx // ~578.3 dp, top aligns at y=758
    val SheetRowHeight: Dp = 150.refPx // ~57.1 dp row pitch
    val SheetHeaderHeight: Dp = 108.refPx // ~41.1 dp
    val SheetSectionHeaderHeight: Dp = 150.refPx // ~57.1 dp
    val SheetHeaderPaddingTop: Dp = 20.refPx

    // Chips
    val ChipHeight: Dp = 36.dp
    val ChipCornerRadius: Dp = 18.dp

    // List rows
    val ListRowMinHeight: Dp = 56.dp

    // System bars
    val StatusBarHeight: Dp = 90.refPx // ~34.3 dp

    // Reader Chrome (measured from D10 reference 1080x2340 @ 420 dpi)
    val ReaderTopBarHeight: Dp = 160.refPx // ~61.0 dp (y=83..243)
    val ReaderBottomBarHeight: Dp = 275.refPx // ~104.8 dp (y=2001..2276, above navigation bar)
    val ReaderLocationPaddingTop: Dp = 41.refPx // ~15.6 dp (y=2042)
    val ReaderLocationToScrubberSpacing: Dp = 63.refPx // ~24.0 dp
    val ReaderScrubberHeight: Dp = 110.refPx // ~41.9 dp (y=2137..2247)
    val ReaderScrubberToDividerSpacing: Dp = 30.refPx // ~11.4 dp
    val ReaderBirdsEyeWidth: Dp = 137.refPx // ~52.2 dp
    val ReaderTrackStartX: Dp = 169.refPx // ~64.4 dp
    val ReaderTrackEndX: Dp = 1012.refPx // ~385.5 dp
    val ReaderBottomGestureBarHeight: Dp = 63.refPx // ~24.0 dp

    // Reader Card Mode (D10)
    val ReaderMarginHorizontal: Dp = 28.dp
    val ReaderCardWidth: Dp = 680.refPx // ~259.0 dp (x=200..880)
    val ReaderCardHeight: Dp = 1521.refPx // ~579.4 dp (y=474..1995)
    val ReaderCardTopY: Dp = 474.refPx // ~180.6 dp
    val ReaderCardBorderThickness: Dp = 2.dp
    val ReaderCardGap: Dp = 9.refPx // ~3.4 dp
    val ReaderCardCornerRadius: Dp = 4.dp
    val ReaderPeekWidth: Dp = 191.refPx
    val ReaderSampleBannerTop: Dp = 300.refPx // ~114.3 dp
    val ReaderSampleBannerHeight: Dp = 48.refPx
    val ReaderThumbnailsGridPadding: Dp = 16.dp
    val ReaderThumbnailWidth: Dp = 96.dp
    val ReaderThumbnailHeight: Dp = 144.dp

    // Reader Contents / TOC (measured from D11 reference)
    val ReaderContentsHeaderHeight: Dp = 189.refPx // ~72.0 dp (y=90..279)
    val ReaderContentsTabsHeight: Dp = 134.refPx // ~51.0 dp (y=280..414)
    val ReaderContentsRowHeight: Dp = 132.refPx // ~50.3 dp pitch
    val ReaderContentsFirstRowHeight: Dp = 49.refPx // ~18.7 dp
    val ReaderContentsMultiLineRowHeight: Dp = 185.refPx // ~70.5 dp (two-line TOC entry)
    val ReaderContentsIndentLevel0: Dp = 87.refPx // ~33.1 dp
    val ReaderContentsIndentLevel1: Dp = 129.refPx // ~49.1 dp
    val ReaderTocIndicatorWidth: Dp = 4.dp
    val ReaderTocIndicatorHeight: Dp = 24.dp

    // Selection & Toolbar (S06)
    val SelectionHandleSize: Dp = 16.dp
    val SelectionHandleTouchSize: Dp = 44.dp
    val SelectionToolbarHeight: Dp = 48.dp
    val ColorSwatchSize: Dp = 28.dp

    // Search (D12)
    val SearchHeaderHeight: Dp = 160.refPx
    val SearchItemHeight: Dp = 180.refPx
    val SearchDividerThickness: Dp = 1.dp
    val SearchFieldUnderlineThickness: Dp = 2.refPx
    val SearchPromptPaddingTop: Dp = 120.refPx

    // Annotations (D13 - D16)
    val AnnotationsHeaderHeight: Dp = 160.refPx
    val AnnotationsFilterChipHeight: Dp = 40.dp
    val AnnotationsRowPitch: Dp = 220.refPx
    val AnnotationsFilterSheetHeight: Dp = 1190.refPx
    val AnnotationsSortSheetHeight: Dp = 965.refPx
    val AnnotationsShareSheetHeight: Dp = 987.refPx
    val AnnotationsColorDotSize: Dp = 10.refPx
    val AnnotationsColorBarWidth: Dp = 6.refPx
    val AnnotationsColorBarHeight: Dp = 48.refPx
    val AnnotationsNotePaddingStart: Dp = 22.refPx
}
