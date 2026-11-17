package app.opusreader.feature.reader

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.opusreader.design.component.OpusReaderBookmarkRibbon
import app.opusreader.design.component.OpusReaderBottomBar
import app.opusreader.design.component.OpusReaderTopBar
import app.opusreader.design.component.OpusSelectionToolbar
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography
import app.opusreader.engine.ReaderEngine
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.LocatorRange
import app.opusreader.engine.model.Page
import app.opusreader.engine.model.PageTheme
import kotlin.math.roundToInt

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
    onUpdateSelectionRange: (LocatorRange) -> Unit,
    onClearSelection: () -> Unit,
    onAnnotationClick: (AnnotationUiModel) -> Unit,
    onCreateHighlight: (String) -> Unit,
    onChangeHighlightColor: (String, String) -> Unit,
    onDeleteAnnotation: (String) -> Unit,
    onOpenNoteEditor: () -> Unit,
    onDismissNoteEditor: () -> Unit,
    onSaveNote: (String?, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectSearchHit: (Int) -> Unit,
    onNextSearchHit: () -> Unit,
    onPreviousSearchHit: () -> Unit,
    onSetAnnotationFilter: (AnnotationFilter) -> Unit,
    onSetAnnotationSort: (AnnotationSort) -> Unit,
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
                                    val page = uiState.currentPage
                                    if (page != null) {
                                        // Check if tapped on existing highlight
                                        val clickedAnnotation = uiState.annotations.firstOrNull { annot ->
                                            if (annot.spineIndex == page.start.spineIndex && annot.type != AnnotationUiType.BOOKMARK) {
                                                val rList = engine.rectsFor(
                                                    page,
                                                    LocatorRange(
                                                        Locator(annot.spineIndex, annot.startOffset),
                                                        Locator(annot.spineIndex, annot.endOffset)
                                                    )
                                                )
                                                rList.any { r ->
                                                    offset.x >= r.left && offset.x <= r.right &&
                                                        offset.y >= r.top && offset.y <= r.bottom
                                                }
                                            } else false
                                        }

                                        if (clickedAnnotation != null) {
                                            onAnnotationClick(clickedAnnotation)
                                            return@detectTapGestures
                                        }
                                    }

                                    if (uiState.selectedRange != null) {
                                        onClearSelection()
                                        return@detectTapGestures
                                    }

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

                        // Draw Highlights
                        if (page != null) {
                            for (annotation in uiState.annotations) {
                                if (annotation.spineIndex == page.start.spineIndex && annotation.type != AnnotationUiType.BOOKMARK) {
                                    val rList = engine.rectsFor(
                                        page,
                                        LocatorRange(
                                            Locator(annotation.spineIndex, annotation.startOffset),
                                            Locator(annotation.spineIndex, annotation.endOffset)
                                        )
                                    )
                                    val hColor = when (annotation.colorHex.uppercase()) {
                                        "#68B6EF", "#2491FF" -> OpusColors.HighlightAnnotationBlue
                                        "#F48FB1" -> OpusColors.HighlightPink
                                        "#FFA726" -> OpusColors.HighlightOrange
                                        else -> OpusColors.HighlightYellow
                                    }
                                    for (rect in rList) {
                                        drawRect(
                                            color = hColor.copy(alpha = 0.35f),
                                            topLeft = Offset(rect.left, rect.top),
                                            size = Size(rect.width(), rect.height())
                                        )
                                    }
                                }
                            }

                            // Draw active Search hit
                            if (uiState.activeSearchHitIndex in uiState.searchResults.indices) {
                                val hit = uiState.searchResults[uiState.activeSearchHitIndex]
                                if (hit.locator.spineIndex == page.start.spineIndex) {
                                    val matchRange = LocatorRange(
                                        start = hit.locator,
                                        end = Locator(
                                            hit.locator.spineIndex,
                                            hit.locator.charOffset + uiState.searchQuery.length
                                        )
                                    )
                                    val searchRects = engine.rectsFor(page, matchRange)
                                    for (rect in searchRects) {
                                        drawRect(
                                            color = OpusColors.AccentGold.copy(alpha = 0.5f),
                                            topLeft = Offset(rect.left, rect.top),
                                            size = Size(rect.width(), rect.height())
                                        )
                                    }
                                }
                            }

                            // Draw Selection Highlight
                            val sel = uiState.selectedRange
                            if (sel != null && sel.start.spineIndex == page.start.spineIndex) {
                                val selRects = engine.rectsFor(page, sel)
                                for (rect in selRects) {
                                    drawRect(
                                        color = OpusColors.AccentBlue.copy(alpha = 0.35f),
                                        topLeft = Offset(rect.left, rect.top),
                                        size = Size(rect.width(), rect.height())
                                    )
                                }
                            }
                        }
                    }

                    // Selection Handles & Toolbar
                    val pageForSel = uiState.currentPage
                    val selRange = uiState.selectedRange
                    if (pageForSel != null && selRange != null && selRange.start.spineIndex == pageForSel.start.spineIndex) {
                        val selRects = engine.rectsFor(pageForSel, selRange)
                        if (selRects.isNotEmpty()) {
                            val firstRect = selRects.first()
                            val lastRect = selRects.last()

                            // Start handle
                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            x = (firstRect.left - 16).roundToInt(),
                                            y = (firstRect.top - 24).roundToInt()
                                        )
                                    }
                                    .size(OpusDimensions.SelectionHandleSize)
                                    .clip(CircleShape)
                                    .background(OpusColors.AccentBlue)
                                    .testTag("selection_start_handle")
                            )

                            // End handle
                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            x = lastRect.right.roundToInt(),
                                            y = lastRect.bottom.roundToInt()
                                        )
                                    }
                                    .size(OpusDimensions.SelectionHandleSize)
                                    .clip(CircleShape)
                                    .background(OpusColors.AccentBlue)
                                    .testTag("selection_end_handle")
                            )

                            // Selection Toolbar
                            val toolbarTop = (firstRect.top - 140).coerceAtLeast(80f)
                            val toolbarLeft = (firstRect.left).coerceIn(40f, totalWidthPx - 400f)

                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            x = toolbarLeft.roundToInt(),
                                            y = toolbarTop.roundToInt()
                                        )
                                    }
                            ) {
                                OpusSelectionToolbar(
                                    onColorSelected = { colorHex ->
                                        if (uiState.activeAnnotation != null) {
                                            onChangeHighlightColor(uiState.activeAnnotation.id, colorHex)
                                        } else {
                                            onCreateHighlight(colorHex)
                                        }
                                    },
                                    onAddNoteClick = onOpenNoteEditor,
                                    onSearchClick = {
                                        val text = uiState.selectedText ?: ""
                                        onSearchQueryChange(text)
                                        onOpenSheet(ActiveReaderSheet.SEARCH)
                                        onClearSelection()
                                    },
                                    onCopyClick = {
                                        val text = uiState.selectedText ?: ""
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Opus Reader", text))
                                        onClearSelection()
                                    },
                                    onShareClick = {
                                        val text = uiState.selectedText ?: ""
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, text)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share quote"))
                                        onClearSelection()
                                    },
                                    onDeleteClick = if (uiState.activeAnnotation != null) {
                                        { onDeleteAnnotation(uiState.activeAnnotation.id) }
                                    } else null
                                )
                            }
                        }
                    }

                    // Floating Search Hit Match Stepper
                    if (uiState.activeSearchHitIndex != -1 && uiState.searchResults.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = OpusDimensions.SpacingXLarge)
                                .clip(RoundedCornerShape(OpusDimensions.CornerRadiusSmall))
                                .background(OpusColors.SurfaceDark)
                                .border(
                                    width = OpusDimensions.DividerThickness,
                                    color = OpusColors.BorderDark,
                                    shape = RoundedCornerShape(OpusDimensions.CornerRadiusSmall)
                                )
                                .padding(horizontal = OpusDimensions.SpacingMedium, vertical = OpusDimensions.SpacingSmall)
                                .testTag("search_match_stepper")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingSmall)
                            ) {
                                Text(
                                    text = "Match ${uiState.activeSearchHitIndex + 1} of ${uiState.searchResults.size}",
                                    style = OpusTypography.bodyMedium,
                                    color = OpusColors.TextPrimaryDark
                                )
                                IconButton(
                                    onClick = onPreviousSearchHit,
                                    modifier = Modifier.size(OpusDimensions.IconMedium).testTag("search_prev_match")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Previous match",
                                        tint = OpusColors.TextPrimaryDark,
                                        modifier = Modifier.size(OpusDimensions.IconSmall)
                                    )
                                }
                                IconButton(
                                    onClick = onNextSearchHit,
                                    modifier = Modifier.size(OpusDimensions.IconMedium).testTag("search_next_match")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Next match",
                                        tint = OpusColors.TextPrimaryDark,
                                        modifier = Modifier.size(OpusDimensions.IconSmall)
                                    )
                                }
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.size(OpusDimensions.IconMedium).testTag("search_dismiss_matches")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss search",
                                        tint = OpusColors.TextSecondaryDark,
                                        modifier = Modifier.size(OpusDimensions.IconSmall)
                                    )
                                }
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
                onNotebookClick = { onOpenSheet(ActiveReaderSheet.ANNOTATIONS) },
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

        // Modal Sheet: In-Book Search (D12)
        if (uiState.activeSheet == ActiveReaderSheet.SEARCH) {
            ReaderSearchScreen(
                searchQuery = uiState.searchQuery,
                searchResults = uiState.searchResults,
                isSearching = uiState.isSearching,
                onQueryChange = onSearchQueryChange,
                onSelectHit = onSelectSearchHit,
                onClose = onDismissSheet,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Modal Screen: Annotations / Notebook (D13)
        if (uiState.activeSheet in listOf(
                ActiveReaderSheet.ANNOTATIONS,
                ActiveReaderSheet.ANNOTATIONS_FILTER,
                ActiveReaderSheet.ANNOTATIONS_SORT,
                ActiveReaderSheet.ANNOTATIONS_SHARE
            )
        ) {
            AnnotationsScreen(
                annotations = uiState.annotations,
                currentFilter = uiState.annotationsFilter,
                currentSort = uiState.annotationsSort,
                tocEntries = uiState.tocEntries,
                onFilterClick = { onOpenSheet(ActiveReaderSheet.ANNOTATIONS_FILTER) },
                onSortClick = { onOpenSheet(ActiveReaderSheet.ANNOTATIONS_SORT) },
                onShareClick = { onOpenSheet(ActiveReaderSheet.ANNOTATIONS_SHARE) },
                onJumpToLocator = { loc ->
                    onJumpToToc(loc)
                    onDismissSheet()
                },
                onToggleFavorite = onToggleFavorite,
                onEditNote = { annot ->
                    onAnnotationClick(annot)
                    onOpenNoteEditor()
                },
                onDeleteAnnotation = onDeleteAnnotation,
                onClose = onDismissSheet,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Modal Sheet: Annotations Filter (D14)
        if (uiState.activeSheet == ActiveReaderSheet.ANNOTATIONS_FILTER) {
            AnnotationsFilterSheet(
                currentFilter = uiState.annotationsFilter,
                totalCount = uiState.annotations.size,
                highlightsCount = uiState.annotations.count { it.type == AnnotationUiType.HIGHLIGHT },
                notesCount = uiState.annotations.count { it.type == AnnotationUiType.NOTE || !it.noteText.isNullOrEmpty() },
                bookmarksCount = uiState.annotations.count { it.type == AnnotationUiType.BOOKMARK },
                starredCount = uiState.annotations.count { it.isFavorite },
                onSelectFilter = { filter ->
                    onSetAnnotationFilter(filter)
                    onOpenSheet(ActiveReaderSheet.ANNOTATIONS)
                },
                onDismiss = { onOpenSheet(ActiveReaderSheet.ANNOTATIONS) }
            )
        }

        // Modal Sheet: Annotations Sort (D15)
        if (uiState.activeSheet == ActiveReaderSheet.ANNOTATIONS_SORT) {
            AnnotationsSortSheet(
                currentSort = uiState.annotationsSort,
                onSelectSort = { sort ->
                    onSetAnnotationSort(sort)
                    onOpenSheet(ActiveReaderSheet.ANNOTATIONS)
                },
                onDismiss = { onOpenSheet(ActiveReaderSheet.ANNOTATIONS) }
            )
        }

        // Modal Sheet: Annotations Share / Export (D16)
        if (uiState.activeSheet == ActiveReaderSheet.ANNOTATIONS_SHARE) {
            AnnotationsExportSheet(
                onExportMarkdown = {
                    val md = buildString {
                        appendLine("# Notes & Highlights for ${uiState.book?.title ?: "Book"}")
                        appendLine("by ${uiState.book?.author ?: "Author"}\n")
                        uiState.annotations.forEach { a ->
                            appendLine("- **Location ${a.startOffset}**: ${a.selectedText}")
                            if (!a.noteText.isNullOrEmpty()) appendLine("  - Note: ${a.noteText}")
                        }
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, md)
                        putExtra(Intent.EXTRA_SUBJECT, "Notes for ${uiState.book?.title}")
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Export Notes as Markdown"))
                    onOpenSheet(ActiveReaderSheet.ANNOTATIONS)
                },
                onExportText = {
                    val txt = buildString {
                        appendLine("Notes for ${uiState.book?.title ?: "Book"}\n")
                        uiState.annotations.forEach { a ->
                            appendLine("[Location ${a.startOffset}] ${a.selectedText}")
                            if (!a.noteText.isNullOrEmpty()) appendLine("Note: ${a.noteText}")
                            appendLine()
                        }
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, txt)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Export Notes as Text"))
                    onOpenSheet(ActiveReaderSheet.ANNOTATIONS)
                },
                onShareEmail = {
                    val txt = buildString {
                        appendLine("Notes from Opus Reader for ${uiState.book?.title}\n")
                        uiState.annotations.forEach { a ->
                            appendLine("• Location ${a.startOffset}: \"${a.selectedText}\"")
                            if (!a.noteText.isNullOrEmpty()) appendLine("  Note: ${a.noteText}")
                        }
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_TEXT, txt)
                        putExtra(Intent.EXTRA_SUBJECT, "Notes for ${uiState.book?.title}")
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Email Notes"))
                    onOpenSheet(ActiveReaderSheet.ANNOTATIONS)
                },
                onDismiss = { onOpenSheet(ActiveReaderSheet.ANNOTATIONS) }
            )
        }

        // Note Editor Dialog
        if (uiState.isNoteEditorVisible) {
            NoteEditorDialog(
                initialText = uiState.activeAnnotation?.noteText ?: "",
                onSave = { noteText ->
                    onSaveNote(uiState.activeAnnotation?.id, noteText)
                },
                onDismiss = onDismissNoteEditor
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
