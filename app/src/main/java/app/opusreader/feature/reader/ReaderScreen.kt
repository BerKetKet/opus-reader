package app.opusreader.feature.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.opusreader.design.component.OpusReaderBookmarkRibbon
import app.opusreader.design.component.OpusReaderBottomBar
import app.opusreader.design.component.OpusReaderTopBar
import app.opusreader.design.theme.BookerlyFontFamily
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.engine.model.Locator

@Composable
fun ReaderScreen(
    uiState: ReaderUiState,
    onBackClick: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onToggleChrome: () -> Unit,
    onToggleBookmark: () -> Unit,
    onScrubberChange: (Int) -> Unit,
    onScrubberCommit: (Int) -> Unit,
    onOpenSheet: (ActiveReaderSheet) -> Unit,
    onDismissSheet: () -> Unit,
    onJumpToToc: (Locator) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val canvasBg = Color(uiState.theme.backgroundColor)
    val textColor = Color(uiState.theme.textColor)

    DisposableEffect(uiState.isChromeVisible) {
        val window = (context as? android.app.Activity)?.window
        val prevStatusBarColor = window?.statusBarColor
        val prevNavBarColor = window?.navigationBarColor
        if (window != null) {
            window.statusBarColor = Color(0xFF232323).toArgb()
            window.navigationBarColor = Color(0xFF141414).toArgb()
        }
        onDispose {
            if (window != null && prevStatusBarColor != null && prevNavBarColor != null) {
                window.statusBarColor = prevStatusBarColor
                window.navigationBarColor = prevNavBarColor
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(canvasBg)
            .testTag("reader_screen")
    ) {
        // Main reading content and gesture interaction area
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val totalWidthPx = with(density) { maxWidth.toPx() }
            val leftZoneWidthPx = totalWidthPx * 0.20f
            val rightZoneStartPx = totalWidthPx * 0.80f

            var totalDragDistance by remember { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            when {
                                offset.x < leftZoneWidthPx -> onPreviousPage()
                                offset.x > rightZoneStartPx -> onNextPage()
                                else -> onToggleChrome()
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDragDistance += dragAmount.x
                            },
                            onDragEnd = {
                                if (totalDragDistance < -50f) {
                                    onNextPage()
                                } else if (totalDragDistance > 50f) {
                                    onPreviousPage()
                                }
                                totalDragDistance = 0f
                            },
                            onDragCancel = {
                                totalDragDistance = 0f
                            }
                        )
                    }
                    .testTag("reader_gesture_surface")
            ) {
                // Reading Canvas
                val page = uiState.currentPage
                if (page != null && page.words.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 28.dp,
                                end = 28.dp,
                                top = OpusDimensions.StatusBarHeight + OpusDimensions.SpacingStandard,
                                bottom = OpusDimensions.SpacingStandard
                            )
                            .testTag("reading_canvas")
                    ) {
                        val fullText = page.words.joinToString(separator = " ") { it.text }
                        Text(
                            text = fullText,
                            color = textColor,
                            fontFamily = BookerlyFontFamily,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.fillMaxWidth().testTag("reader_page_text")
                        )
                    }
                }

                // Bookmark ribbon top right
                OpusReaderBookmarkRibbon(
                    isBookmarked = uiState.isBookmarked,
                    onToggle = onToggleBookmark,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = OpusDimensions.SpacingLarge)
                )
            }
        }

        // Animated Top Chrome Bar
        AnimatedVisibility(
            visible = uiState.isChromeVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            OpusReaderTopBar(
                isBookmarked = uiState.isBookmarked,
                onBackClick = onBackClick,
                onContentsClick = { onOpenSheet(ActiveReaderSheet.CONTENTS) },
                onSearchClick = { onOpenSheet(ActiveReaderSheet.SEARCH) },
                onBookmarkClick = onToggleBookmark,
                onDisplaySettingsClick = { onOpenSheet(ActiveReaderSheet.SETTINGS) },
                onOverflowClick = { onOpenSheet(ActiveReaderSheet.OVERFLOW) }
            )
        }

        // Animated Bottom Chrome Bar
        AnimatedVisibility(
            visible = uiState.isChromeVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            OpusReaderBottomBar(
                currentLocation = uiState.scrubberLocation,
                totalLocations = uiState.totalLocations,
                percent = uiState.progress.percent,
                onThumbnailsClick = { /* Thumbnails mode */ },
                onScrubberChange = onScrubberChange,
                onScrubberCommit = onScrubberCommit
            )
        }

        // Modal Sheet: Table of Contents (D11)
        if (uiState.activeSheet == ActiveReaderSheet.CONTENTS) {
            ContentsTocScreen(
                bookTitle = uiState.book?.title ?: "The Complete Malazan",
                bookAuthor = uiState.book?.author ?: "Steven Erikson",
                tocEntries = uiState.tocEntries,
                currentSpineIndex = uiState.currentPage?.start?.spineIndex ?: 0,
                onSelectChapter = onJumpToToc,
                onClose = onDismissSheet,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
