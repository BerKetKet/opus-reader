package app.opusreader.feature.reader

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.opusreader.design.component.OpusReaderBookmarkRibbon
import app.opusreader.design.component.OpusReaderBottomBar
import app.opusreader.design.component.OpusReaderTopBar
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography
import app.opusreader.engine.ReaderEngine
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.Page
import app.opusreader.engine.model.PageTheme

@Composable
fun ReaderScreen(
    uiState: ReaderUiState,
    engine: ReaderEngine,
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
    onToggleThumbnails: () -> Unit,
    onWordLongPress: (Locator) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context as? Activity
    val canvasBg = Color(uiState.theme.backgroundColor)

    DisposableEffect(uiState.isChromeVisible) {
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowInsetsControllerCompat(window, view)
            if (uiState.isChromeVisible) {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                window.statusBarColor = OpusColors.ReaderChromeTopDark.toArgb()
                window.navigationBarColor = OpusColors.ReaderChromeBottomDark.toArgb()
            } else {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                window.statusBarColor = OpusColors.PageBlack.toArgb()
                window.navigationBarColor = OpusColors.PageBlack.toArgb()
            }
        }
        onDispose {
            val window = activity?.window
            if (window != null) {
                WindowInsetsControllerCompat(window, view).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (uiState.isChromeVisible) OpusColors.PageBlack else canvasBg)
            .testTag("reader_screen")
    ) {
        if (!uiState.isChromeVisible) {
            // Mode A: Chrome Hidden (Reading mode, immersive fullscreen)
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
                            detectTapGestures(
                                onTap = { offset ->
                                    when {
                                        offset.x < leftZoneWidthPx -> onPreviousPage()
                                        offset.x > rightZoneStartPx -> onNextPage()
                                        else -> onToggleChrome()
                                    }
                                },
                                onLongPress = { offset ->
                                    val p = uiState.currentPage
                                    if (p != null) {
                                        val loc = engine.hitTest(p, offset.x, offset.y)
                                        if (loc != null) {
                                            onWordLongPress(loc)
                                        }
                                    }
                                }
                            )
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
                    val page = uiState.currentPage
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("reading_canvas")
                    ) {
                        drawIntoCanvas { canvas ->
                            if (page != null) {
                                engine.draw(page, canvas.nativeCanvas, uiState.theme)
                            }
                        }
                    }

                    OpusReaderBookmarkRibbon(
                        isBookmarked = uiState.isBookmarked,
                        onToggle = onToggleBookmark,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = OpusDimensions.SpacingLarge)
                    )
                }
            }
        } else {
            // Mode B: Chrome Visible (D10 Card mode with peek cards and sample banner)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onToggleChrome()
                            }
                        )
                    }
                    .testTag("reader_card_mode_surface")
            ) {
                // Top Chapter / Book title line
                val bannerText = remember(uiState.currentPage, uiState.tocEntries) {
                    val p = uiState.currentPage
                    if (p != null) {
                        uiState.tocEntries.find { it.locator.spineIndex == p.start.spineIndex }?.title
                            ?: uiState.book?.title ?: ""
                    } else ""
                }

                if (bannerText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = OpusDimensions.ReaderSampleBannerTop)
                            .height(OpusDimensions.ReaderSampleBannerHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bannerText,
                            style = OpusTypography.bodyMedium,
                            color = OpusColors.ReaderSampleBannerText,
                            maxLines = 1
                        )
                    }
                }

                // Left peek card (previous page)
                val prevPage = remember(uiState.currentPage) {
                    uiState.currentPage?.let { engine.previous(it) }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = OpusDimensions.ReaderCardTopY)
                        .size(
                            width = OpusDimensions.ReaderPeekWidth,
                            height = OpusDimensions.ReaderCardHeight
                        )
                        .clip(RoundedCornerShape(OpusDimensions.ReaderCardCornerRadius))
                        .background(OpusColors.ReaderPeekBackground)
                ) {
                    if (prevPage != null) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawIntoCanvas { canvas ->
                                val scale = size.width / 1080f
                                canvas.nativeCanvas.save()
                                canvas.nativeCanvas.scale(scale, scale)
                                engine.draw(prevPage, canvas.nativeCanvas, uiState.theme)
                                canvas.nativeCanvas.restore()
                            }
                        }
                    }
                }

                // Central framed card
                val page = uiState.currentPage
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = OpusDimensions.ReaderCardTopY)
                        .size(
                            width = OpusDimensions.ReaderCardWidth,
                            height = OpusDimensions.ReaderCardHeight
                        )
                        .clip(RoundedCornerShape(OpusDimensions.ReaderCardCornerRadius))
                        .border(
                            width = OpusDimensions.ReaderCardBorderThickness,
                            color = OpusColors.ReaderCardBorder,
                            shape = RoundedCornerShape(OpusDimensions.ReaderCardCornerRadius)
                        )
                        .background(Color(uiState.theme.backgroundColor))
                        .testTag("reader_card_frame")
                ) {
                    if (page != null) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("reader_card_canvas")
                        ) {
                            drawIntoCanvas { canvas ->
                                val scale = size.width / 1080f
                                canvas.nativeCanvas.save()
                                canvas.nativeCanvas.scale(scale, scale)
                                engine.draw(page, canvas.nativeCanvas, uiState.theme)
                                canvas.nativeCanvas.restore()
                            }
                        }
                    }

                    OpusReaderBookmarkRibbon(
                        isBookmarked = uiState.isBookmarked,
                        onToggle = onToggleBookmark,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = OpusDimensions.SpacingSmall)
                    )
                }

                // Right peek card (next page)
                val nextPage = remember(uiState.currentPage) {
                    uiState.currentPage?.let { engine.next(it) }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = OpusDimensions.ReaderCardTopY)
                        .size(
                            width = OpusDimensions.ReaderPeekWidth,
                            height = OpusDimensions.ReaderCardHeight
                        )
                        .clip(RoundedCornerShape(OpusDimensions.ReaderCardCornerRadius))
                        .background(OpusColors.ReaderPeekBackground)
                ) {
                    if (nextPage != null) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawIntoCanvas { canvas ->
                                val scale = size.width / 1080f
                                canvas.nativeCanvas.save()
                                canvas.nativeCanvas.scale(scale, scale)
                                engine.draw(nextPage, canvas.nativeCanvas, uiState.theme)
                                canvas.nativeCanvas.restore()
                            }
                        }
                    }
                }
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
                onThumbnailsClick = onToggleThumbnails,
                onScrubberChange = onScrubberChange,
                onScrubberCommit = onScrubberCommit
            )
        }

        // Thumbnails Grid Overlay
        if (uiState.isThumbnailsVisible) {
            ReaderThumbnailsGrid(
                pages = listOfNotNull(
                    engine.previous(uiState.currentPage ?: Page(Locator(0, 0), Locator(0, 0), emptyList(), emptyList())),
                    uiState.currentPage,
                    engine.next(uiState.currentPage ?: Page(Locator(0, 0), Locator(0, 0), emptyList(), emptyList()))
                ),
                engine = engine,
                theme = uiState.theme,
                onSelectPage = { loc ->
                    onJumpToToc(loc)
                    onToggleThumbnails()
                },
                onClose = onToggleThumbnails,
                modifier = Modifier.fillMaxSize()
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

@Composable
private fun ReaderThumbnailsGrid(
    pages: List<Page>,
    engine: ReaderEngine,
    theme: PageTheme,
    onSelectPage: (Locator) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OpusColors.BackgroundDark)
            .testTag("reader_thumbnails_grid")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OpusDimensions.TopBarHeight)
                .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Thumbnails",
                style = OpusTypography.titleLarge,
                color = OpusColors.TextPrimaryDark
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("thumbnails_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = OpusColors.TextPrimaryDark,
                    modifier = Modifier.size(OpusDimensions.IconMedium)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = OpusDimensions.ReaderThumbnailsGridPadding),
            contentPadding = PaddingValues(vertical = OpusDimensions.ReaderThumbnailsGridPadding),
            verticalArrangement = Arrangement.spacedBy(OpusDimensions.ReaderThumbnailsGridPadding),
            horizontalArrangement = Arrangement.spacedBy(OpusDimensions.ReaderThumbnailsGridPadding)
        ) {
            items(pages.size) { index ->
                val p = pages[index]
                Column(
                    modifier = Modifier
                        .clickable { onSelectPage(p.start) }
                        .padding(OpusDimensions.SpacingXXSmall),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(
                                width = OpusDimensions.ReaderThumbnailWidth,
                                height = OpusDimensions.ReaderThumbnailHeight
                            )
                            .clip(RoundedCornerShape(OpusDimensions.CornerRadiusSmall))
                            .border(
                                width = OpusDimensions.DividerThickness,
                                color = OpusColors.BorderDark,
                                shape = RoundedCornerShape(OpusDimensions.CornerRadiusSmall)
                            )
                            .background(Color(theme.backgroundColor))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawIntoCanvas { canvas ->
                                val scale = size.width / 1080f
                                canvas.nativeCanvas.save()
                                canvas.nativeCanvas.scale(scale, scale)
                                engine.draw(p, canvas.nativeCanvas, theme)
                                canvas.nativeCanvas.restore()
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(OpusDimensions.SpacingXSmall))
                    Text(
                        text = "${index + 1}",
                        style = OpusTypography.labelSmall,
                        color = OpusColors.TextSecondaryDark
                    )
                }
            }
        }
    }
}
