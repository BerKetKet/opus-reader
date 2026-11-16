package app.opusreader.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.opusreader.R
import app.opusreader.design.component.OpusBottomSheet
import app.opusreader.design.component.OpusCheckboxRow
import app.opusreader.design.component.OpusChevronRow
import app.opusreader.design.component.OpusCoverTile
import app.opusreader.design.component.OpusListRow
import app.opusreader.design.component.OpusResumeCover
import app.opusreader.design.component.OpusSearchPill
import app.opusreader.design.component.OpusTopBar
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography
import coil.compose.AsyncImage

@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    onOpenBook: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (LibraryFilter) -> Unit,
    onViewModeChange: (LibraryViewMode) -> Unit,
    onSortModeChange: (LibrarySortMode) -> Unit,
    onSelectCollection: (String?) -> Unit,
    onOpenSheet: (ActiveSheet, BookUiModel?) -> Unit,
    onDismissSheet: () -> Unit,
    onToggleReadStatus: (String) -> Unit,
    onToggleDownload: (String) -> Unit,
    onDeleteBook: (String) -> Unit,
    onAssignCollection: (String, String?) -> Unit,
    onNewCollectionNameChanged: (String) -> Unit,
    onCreateCollection: (String) -> Unit,
    onGroupBySeriesChange: (Boolean) -> Unit,
    onApplyFilterState: (LibraryFilterState) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OpusColors.BackgroundDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            OpusTopBar(
                title = if (uiState.activeCollection != null) uiState.activeCollection else "Library",
                navigationIcon = if (uiState.activeCollection != null) {
                    {
                        IconButton(onClick = { onSelectCollection(null) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = OpusColors.TextPrimaryDark
                            )
                        }
                    }
                } else null,
                actions = {
                    IconButton(
                        onClick = { onOpenSheet(ActiveSheet.FILTER, null) },
                        modifier = Modifier.testTag("library_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = OpusColors.TextPrimaryDark
                        )
                    }
                    IconButton(
                        onClick = { onOpenSheet(ActiveSheet.VIEW_SORT, null) },
                        modifier = Modifier.testTag("library_view_sort_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "View and Sort",
                            tint = OpusColors.TextPrimaryDark
                        )
                    }
                },
                modifier = Modifier.testTag("library_top_bar")
            )

            // Search Bar immediately below top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal)
            ) {
                OpusSearchPill(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onClear = { onSearchQueryChange("") },
                    placeholder = stringResource(R.string.search_hint),
                    onSearchAction = { },
                    modifier = Modifier.testTag("library_search_pill")
                )
            }

            // Distance from search pill to divider
            Spacer(modifier = Modifier.height(OpusDimensions.SearchToDividerSpacing))

            // Divider below search bar
            HorizontalDivider(
                thickness = OpusDimensions.DividerThickness,
                color = OpusColors.DividerDark
            )

            // Main Content Area based on ViewMode
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (uiState.viewMode) {
                    LibraryViewMode.GRID -> {
                        LibraryGridView(
                            books = uiState.filteredBooks,
                            gridState = gridState,
                            onOpenBook = onOpenBook,
                            onLongPressBook = { book -> onOpenSheet(ActiveSheet.BOOK_MENU, book) }
                        )
                    }
                    LibraryViewMode.LIST -> {
                        LibraryListView(
                            books = uiState.filteredBooks,
                            listState = listState,
                            onOpenBook = onOpenBook,
                            onLongPressBook = { book -> onOpenSheet(ActiveSheet.BOOK_MENU, book) }
                        )
                    }
                    LibraryViewMode.COLLECTIONS -> {
                        LibraryCollectionsView(
                            collections = uiState.collections,
                            allBooks = uiState.allBooks,
                            onSelectCollection = { coll ->
                                onSelectCollection(coll)
                                onViewModeChange(LibraryViewMode.GRID)
                            },
                            onCreateCollectionClick = { onOpenSheet(ActiveSheet.CREATE_COLLECTION, null) }
                        )
                    }
                }
            }
        }

        // Bottom Sheets
        when (uiState.activeSheet) {
            ActiveSheet.FILTER -> {
                FilterBottomSheet(
                    filterState = uiState.filterState,
                    allBooks = uiState.allBooks,
                    onApplyFilter = onApplyFilterState,
                    onDismiss = onDismissSheet
                )
            }
            ActiveSheet.VIEW_SORT -> {
                ViewSortBottomSheet(
                    groupBySeries = uiState.groupBySeries,
                    viewMode = uiState.viewMode,
                    sortMode = uiState.sortMode,
                    onGroupBySeriesChange = onGroupBySeriesChange,
                    onViewModeChange = onViewModeChange,
                    onSortModeChange = onSortModeChange,
                    onDismiss = onDismissSheet
                )
            }
            ActiveSheet.BOOK_MENU -> {
                if (uiState.selectedBookForMenu != null) {
                    BookMenuBottomSheet(
                        book = uiState.selectedBookForMenu,
                        collections = uiState.collections,
                        onToggleReadStatus = { onToggleReadStatus(uiState.selectedBookForMenu.id) },
                        onToggleDownload = { onToggleDownload(uiState.selectedBookForMenu.id) },
                        onDeleteBook = { onDeleteBook(uiState.selectedBookForMenu.id) },
                        onAssignCollection = { colName ->
                            onAssignCollection(uiState.selectedBookForMenu.id, colName)
                        },
                        onCreateCollection = {
                            onOpenSheet(ActiveSheet.CREATE_COLLECTION, uiState.selectedBookForMenu)
                        },
                        onDismiss = onDismissSheet
                    )
                }
            }
            ActiveSheet.CREATE_COLLECTION -> {
                CreateCollectionBottomSheet(
                    input = uiState.newCollectionNameInput,
                    onInputChange = onNewCollectionNameChanged,
                    onSave = {
                        if (uiState.newCollectionNameInput.isNotBlank()) {
                            onCreateCollection(uiState.newCollectionNameInput.trim())
                        }
                    },
                    onDismiss = onDismissSheet
                )
            }
            null -> { }
        }
    }
}

@Composable
private fun LibraryGridView(
    books: List<BookUiModel>,
    gridState: LazyGridState,
    onOpenBook: (String) -> Unit,
    onLongPressBook: (BookUiModel) -> Unit
) {
    if (books.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("library_empty_state"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No books found",
                style = OpusTypography.headlineMedium,
                color = OpusColors.TextSecondaryDark
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("library_books_grid"),
            contentPadding = PaddingValues(
                start = OpusDimensions.GridSideMargin,
                end = OpusDimensions.GridSideMargin,
                top = OpusDimensions.DividerToGridSpacing,
                bottom = OpusDimensions.BottomNavHeight
            ),
            horizontalArrangement = Arrangement.spacedBy(OpusDimensions.GridColumnGap),
            verticalArrangement = Arrangement.spacedBy(OpusDimensions.GridRowGap)
        ) {
            items(books, key = { it.id }) { book ->
                OpusCoverTile(
                    title = book.title,
                    author = book.author,
                    isDownloaded = book.isDownloaded,
                    isSample = book.isSample,
                    isNew = book.isNew,
                    progressPercent = book.progressPercent,
                    coverContent = if (book.coverPath != null) {
                        {
                            AsyncImage(
                                model = book.coverPath,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else null,
                    onClick = { onOpenBook(book.id) },
                    onLongClick = { onLongPressBook(book) },
                    modifier = Modifier.testTag("book_tile_${book.id}")
                )
            }
        }
    }
}

@Composable
private fun LibraryListView(
    books: List<BookUiModel>,
    listState: LazyListState,
    onOpenBook: (String) -> Unit,
    onLongPressBook: (BookUiModel) -> Unit
) {
    if (books.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("library_empty_state"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No books found",
                style = OpusTypography.headlineMedium,
                color = OpusColors.TextSecondaryDark
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("library_books_list"),
            contentPadding = PaddingValues(
                top = OpusDimensions.DividerToListSpacing,
                bottom = OpusDimensions.BottomNavHeight
            )
        ) {
            items(books, key = { it.id }) { book ->
                OpusListRow(
                    title = book.title,
                    subtitle = book.author,
                    onClick = { onOpenBook(book.id) },
                    leadingContent = {
                        if (book.coverPath != null) {
                            AsyncImage(
                                model = book.coverPath,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(OpusDimensions.ListCoverWidth)
                                    .height(OpusDimensions.ListCoverHeight)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .width(OpusDimensions.ListCoverWidth)
                                    .height(OpusDimensions.ListCoverHeight)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(OpusColors.SurfaceVariantDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = book.title.take(1),
                                    style = OpusTypography.titleMedium,
                                    color = OpusColors.TextPrimaryDark
                                )
                            }
                        }
                    },
                    trailingContent = {
                        IconButton(
                            onClick = { onLongPressBook(book) },
                            modifier = Modifier.testTag("book_options_${book.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Book Options",
                                tint = OpusColors.TextSecondaryDark
                            )
                        }
                    },
                    modifier = Modifier.testTag("book_row_${book.id}")
                )
            }
        }
    }
}

@Composable
private fun LibraryCollectionsView(
    collections: List<String>,
    allBooks: List<BookUiModel>,
    onSelectCollection: (String) -> Unit,
    onCreateCollectionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("library_collections_list")
    ) {
        if (collections.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = OpusDimensions.SpacingXLarge)
                    .testTag("collections_empty_state"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Collections (0)",
                    style = OpusTypography.bodyMedium,
                    color = OpusColors.TextSecondaryDark
                )

                Spacer(modifier = Modifier.height(OpusDimensions.SpacingStandard))

                HorizontalDivider(
                    thickness = OpusDimensions.DividerThickness,
                    color = OpusColors.DividerDark
                )

                Spacer(modifier = Modifier.height(OpusDimensions.SpacingXXLarge))

                Row(
                    modifier = Modifier
                        .clickable(onClick = onCreateCollectionClick)
                        .padding(horizontal = OpusDimensions.ScreenMarginHorizontal, vertical = OpusDimensions.SpacingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create a new collection",
                        tint = OpusColors.AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(OpusDimensions.SpacingSmall))
                    Text(
                        text = "CREATE A NEW COLLECTION",
                        style = OpusTypography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpusColors.AccentBlue,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        } else {
            OpusListRow(
                title = "New Collection",
                subtitle = "Create a collection to organize your library",
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(OpusColors.SurfaceVariantDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Collection",
                            tint = OpusColors.TextPrimaryDark
                        )
                    }
                },
                onClick = onCreateCollectionClick,
                modifier = Modifier.testTag("create_collection_row")
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(collections) { collectionName ->
                    val count = allBooks.count { it.collectionName == collectionName }
                    OpusChevronRow(
                        title = collectionName,
                        subtitle = "$count ${if (count == 1) "item" else "items"}",
                        trailingValue = "",
                        onClick = { onSelectCollection(collectionName) },
                        modifier = Modifier.testTag("collection_row_$collectionName")
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterBottomSheet(
    filterState: LibraryFilterState,
    allBooks: List<BookUiModel>,
    onApplyFilter: (LibraryFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var state by remember { mutableStateOf(filterState) }
    var readStateExpanded by remember { mutableStateOf(true) }
    var contentTypeExpanded by remember { mutableStateOf(true) }

    val readCount = allBooks.count { it.isRead }
    val unreadCount = allBooks.count { !it.isRead }
    val booksCount = allBooks.count { !it.isSample && !it.isDoc }
    val samplesCount = allBooks.count { it.isSample }
    val docsCount = allBooks.count { it.isDoc }

    OpusBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        expandedHeight = OpusDimensions.FilterSheetHeight,
        showDragHandle = false,
        modifier = Modifier.testTag("library_filter_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OpusDimensions.SpacingLarge)
        ) {
            // Header: Filter & DONE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter",
                    style = OpusTypography.headlineMedium,
                    color = OpusColors.TextPrimaryDark
                )
                Text(
                    text = "DONE",
                    style = OpusTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OpusColors.HighlightBlue,
                    modifier = Modifier
                        .clickable { onApplyFilter(state) }
                        .testTag("filter_done_button")
                )
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingStandard))

            // Section 1: Read state
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { readStateExpanded = !readStateExpanded }
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal, vertical = OpusDimensions.SpacingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (readStateExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = OpusColors.HighlightBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(OpusDimensions.SpacingSmall))
                Text(
                    text = "Read state",
                    style = OpusTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = OpusColors.HighlightBlue
                )
            }

            if (readStateExpanded) {
                HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)
                FilterItemRow(
                    label = "Read ($readCount)",
                    selected = state.showRead,
                    onClick = { state = state.copy(showRead = !state.showRead) },
                    modifier = Modifier.testTag("filter_opt_read")
                )
                HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)
                FilterItemRow(
                    label = "Unread ($unreadCount)",
                    selected = state.showUnread,
                    onClick = { state = state.copy(showUnread = !state.showUnread) },
                    modifier = Modifier.testTag("filter_opt_unread")
                )
            }

            HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)

            // Section 2: Content type
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { contentTypeExpanded = !contentTypeExpanded }
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal, vertical = OpusDimensions.SpacingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (contentTypeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = OpusColors.HighlightBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(OpusDimensions.SpacingSmall))
                Text(
                    text = "Content type",
                    style = OpusTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = OpusColors.HighlightBlue
                )
            }

            if (contentTypeExpanded) {
                HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)
                FilterItemRow(
                    label = "Books ($booksCount)",
                    selected = state.showBooks,
                    onClick = { state = state.copy(showBooks = !state.showBooks) },
                    modifier = Modifier.testTag("filter_opt_books")
                )
                HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)
                FilterItemRow(
                    label = "Samples ($samplesCount)",
                    selected = state.showSamples,
                    onClick = { state = state.copy(showSamples = !state.showSamples) },
                    modifier = Modifier.testTag("filter_opt_samples")
                )
                HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)
                FilterItemRow(
                    label = "Docs ($docsCount)",
                    selected = state.showDocs,
                    onClick = { state = state.copy(showDocs = !state.showDocs) },
                    modifier = Modifier.testTag("filter_opt_docs")
                )
                HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)
            }
        }
    }
}

@Composable
private fun FilterItemRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = OpusDimensions.ScreenMarginHorizontal, vertical = OpusDimensions.SpacingMedium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = OpusTypography.bodyLarge,
            color = OpusColors.TextPrimaryDark
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = OpusColors.HighlightBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ViewSortBottomSheet(
    groupBySeries: Boolean,
    viewMode: LibraryViewMode,
    sortMode: LibrarySortMode,
    onGroupBySeriesChange: (Boolean) -> Unit,
    onViewModeChange: (LibraryViewMode) -> Unit,
    onSortModeChange: (LibrarySortMode) -> Unit,
    onDismiss: () -> Unit
) {
    OpusBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        expandedHeight = OpusDimensions.ViewSortSheetHeight,
        showDragHandle = false,
        modifier = Modifier.testTag("library_view_sort_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OpusDimensions.SpacingLarge)
        ) {
            // Header: View & sort / DONE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View & sort",
                    style = OpusTypography.headlineMedium,
                    color = OpusColors.TextPrimaryDark
                )
                Text(
                    text = "DONE",
                    style = OpusTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OpusColors.HighlightBlue,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .testTag("view_sort_done_button")
                )
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingStandard))
            HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)

            // Group by series toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OpusDimensions.ScreenMarginHorizontal, vertical = OpusDimensions.SpacingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Group by series",
                    style = OpusTypography.bodyLarge,
                    color = OpusColors.TextPrimaryDark
                )
                Switch(
                    checked = groupBySeries,
                    onCheckedChange = onGroupBySeriesChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OpusColors.HighlightBlue,
                        uncheckedThumbColor = Color(0xFF9E9E9E),
                        uncheckedTrackColor = Color(0xFF333333)
                    ),
                    modifier = Modifier.testTag("group_by_series_switch")
                )
            }

            HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)

            // View Section
            SheetOptionRow(
                icon = Icons.Default.GridView,
                label = "Grid",
                selected = viewMode == LibraryViewMode.GRID,
                onClick = { onViewModeChange(LibraryViewMode.GRID) },
                modifier = Modifier.testTag("view_opt_grid")
            )
            HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)
            SheetOptionRow(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                label = "List",
                selected = viewMode == LibraryViewMode.LIST,
                onClick = { onViewModeChange(LibraryViewMode.LIST) },
                modifier = Modifier.testTag("view_opt_list")
            )
            HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)
            SheetOptionRow(
                icon = Icons.Default.Folder,
                label = "Collections",
                selected = viewMode == LibraryViewMode.COLLECTIONS,
                onClick = { onViewModeChange(LibraryViewMode.COLLECTIONS) },
                modifier = Modifier.testTag("view_opt_collections")
            )
            HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)

            // Sort Section
            SheetOptionRow(
                label = "Most Recent",
                selected = sortMode == LibrarySortMode.RECENT,
                onClick = { onSortModeChange(LibrarySortMode.RECENT) },
                modifier = Modifier.testTag("sort_opt_recent")
            )
            HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)
            SheetOptionRow(
                label = "Title",
                selected = sortMode == LibrarySortMode.TITLE,
                onClick = { onSortModeChange(LibrarySortMode.TITLE) },
                modifier = Modifier.testTag("sort_opt_title")
            )
            HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)
            SheetOptionRow(
                label = "Author (A-Z)",
                selected = sortMode == LibrarySortMode.AUTHOR_AZ,
                onClick = { onSortModeChange(LibrarySortMode.AUTHOR_AZ) },
                modifier = Modifier.testTag("sort_opt_author_az")
            )
            HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)
            SheetOptionRow(
                label = "Author (Z-A)",
                selected = sortMode == LibrarySortMode.AUTHOR_ZA,
                onClick = { onSortModeChange(LibrarySortMode.AUTHOR_ZA) },
                modifier = Modifier.testTag("sort_opt_author_za")
            )
            HorizontalDivider(thickness = OpusDimensions.DividerThickness, color = OpusColors.DividerDark)
        }
    }
}

@Composable
private fun SheetOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = OpusDimensions.ScreenMarginHorizontal, vertical = OpusDimensions.SpacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OpusColors.TextPrimaryDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(OpusDimensions.SpacingMedium))
        }
        Text(
            text = label,
            style = OpusTypography.bodyLarge,
            color = OpusColors.TextPrimaryDark,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = OpusColors.HighlightBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun BookMenuBottomSheet(
    book: BookUiModel,
    collections: List<String>,
    onToggleReadStatus: () -> Unit,
    onToggleDownload: () -> Unit,
    onDeleteBook: () -> Unit,
    onAssignCollection: (String?) -> Unit,
    onCreateCollection: () -> Unit,
    onDismiss: () -> Unit
) {
    OpusBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("library_book_menu_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OpusDimensions.ScreenMarginHorizontal)
        ) {
            Text(
                text = book.title,
                style = OpusTypography.headlineMedium,
                color = OpusColors.TextPrimaryDark,
                modifier = Modifier.testTag("book_menu_title")
            )
            Text(
                text = book.author,
                style = OpusTypography.bodyMedium,
                color = OpusColors.TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingStandard))

            OpusListRow(
                title = if (book.isRead) "Mark as Unread" else "Mark as Read",
                onClick = onToggleReadStatus,
                modifier = Modifier.testTag("book_menu_toggle_read")
            )

            OpusListRow(
                title = if (book.isDownloaded) "Remove Download" else "Download Book",
                onClick = onToggleDownload,
                modifier = Modifier.testTag("book_menu_toggle_download")
            )

            OpusListRow(
                title = "Add to New Collection",
                onClick = onCreateCollection,
                modifier = Modifier.testTag("book_menu_new_collection")
            )

            if (collections.isNotEmpty()) {
                Text(
                    text = "Collections",
                    style = OpusTypography.titleMedium,
                    color = OpusColors.TextSecondaryDark,
                    modifier = Modifier.padding(top = OpusDimensions.SpacingSmall)
                )
                collections.forEach { collectionName ->
                    val isAssigned = book.collectionName == collectionName
                    OpusCheckboxRow(
                        title = collectionName,
                        checked = isAssigned,
                        onCheckedChange = { checked ->
                            onAssignCollection(if (checked) collectionName else null)
                        },
                        modifier = Modifier.testTag("book_menu_col_$collectionName")
                    )
                }
            }

            OpusListRow(
                title = "Delete from Library",
                onClick = onDeleteBook,
                modifier = Modifier.testTag("book_menu_delete")
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingXXLarge))
        }
    }
}

@Composable
private fun CreateCollectionBottomSheet(
    input: String,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    OpusBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("library_create_collection_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OpusDimensions.ScreenMarginHorizontal)
        ) {
            Text(
                text = "New Collection",
                style = OpusTypography.headlineMedium,
                color = OpusColors.TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(OpusDimensions.SpacingStandard))

            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                placeholder = { Text("Collection name", color = OpusColors.TextSecondaryDark) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OpusColors.AccentGold,
                    unfocusedBorderColor = OpusColors.BorderDark,
                    focusedTextColor = OpusColors.TextPrimaryDark,
                    unfocusedTextColor = OpusColors.TextPrimaryDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("collection_name_input")
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingLarge))

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = OpusColors.AccentGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("collection_save_button")
            ) {
                Text("Create Collection", color = OpusColors.BackgroundDark, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingXXLarge))
        }
    }
}
