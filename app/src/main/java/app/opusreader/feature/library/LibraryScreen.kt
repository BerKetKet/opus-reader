package app.opusreader.feature.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.opusreader.design.component.OpusBottomSheet
import app.opusreader.design.component.OpusCheckboxRow
import app.opusreader.design.component.OpusChevronRow
import app.opusreader.design.component.OpusChip
import app.opusreader.design.component.OpusCoverTile
import app.opusreader.design.component.OpusListRow
import app.opusreader.design.component.OpusResumeCover
import app.opusreader.design.component.OpusSearchPill
import app.opusreader.design.component.OpusTopBar
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

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
    modifier: Modifier = Modifier
) {
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
                                imageVector = Icons.Default.ArrowBack,
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
                            imageVector = Icons.Default.Sort,
                            contentDescription = "View and Sort",
                            tint = OpusColors.TextPrimaryDark
                        )
                    }
                },
                modifier = Modifier.testTag("library_top_bar")
            )

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = OpusDimensions.ScreenMarginHorizontal,
                        vertical = OpusDimensions.SpacingSmall
                    )
            ) {
                OpusSearchPill(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onClear = { onSearchQueryChange("") },
                    placeholder = "Search your books...",
                    onSearchAction = { },
                    modifier = Modifier.testTag("library_search_pill")
                )
            }

            // Filter Chips Bar (D01 / D07)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_filter_chips_row"),
                contentPadding = PaddingValues(horizontal = OpusDimensions.ScreenMarginHorizontal),
                horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingSmall)
            ) {
                item {
                    OpusChip(
                        label = "Downloaded",
                        selected = uiState.activeFilter == LibraryFilter.DOWNLOADED,
                        onClick = {
                            val next = if (uiState.activeFilter == LibraryFilter.DOWNLOADED) LibraryFilter.ALL else LibraryFilter.DOWNLOADED
                            onFilterChange(next)
                        },
                        modifier = Modifier.testTag("library_chip_downloaded")
                    )
                }
                item {
                    OpusChip(
                        label = "Unread",
                        selected = uiState.activeFilter == LibraryFilter.UNREAD,
                        onClick = {
                            val next = if (uiState.activeFilter == LibraryFilter.UNREAD) LibraryFilter.ALL else LibraryFilter.UNREAD
                            onFilterChange(next)
                        },
                        modifier = Modifier.testTag("library_chip_unread")
                    )
                }
                item {
                    OpusChip(
                        label = "Books",
                        selected = uiState.activeFilter == LibraryFilter.BOOKS,
                        onClick = {
                            val next = if (uiState.activeFilter == LibraryFilter.BOOKS) LibraryFilter.ALL else LibraryFilter.BOOKS
                            onFilterChange(next)
                        },
                        modifier = Modifier.testTag("library_chip_books")
                    )
                }
                item {
                    OpusChip(
                        label = "Samples",
                        selected = uiState.activeFilter == LibraryFilter.SAMPLES,
                        onClick = {
                            val next = if (uiState.activeFilter == LibraryFilter.SAMPLES) LibraryFilter.ALL else LibraryFilter.SAMPLES
                            onFilterChange(next)
                        },
                        modifier = Modifier.testTag("library_chip_samples")
                    )
                }
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingSmall))

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
                            onOpenBook = onOpenBook,
                            onLongPressBook = { book -> onOpenSheet(ActiveSheet.BOOK_MENU, book) }
                        )
                    }
                    LibraryViewMode.LIST -> {
                        LibraryListView(
                            books = uiState.filteredBooks,
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

        // Floating Resume Cover (D01 / D07)
        if (uiState.lastReadBook != null && uiState.activeSheet == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = OpusDimensions.ScreenMarginHorizontal,
                        bottom = OpusDimensions.SpacingXLarge
                    )
                    .testTag("library_resume_cover")
            ) {
                OpusResumeCover(
                    title = uiState.lastReadBook.title,
                    onResumeClick = { onOpenBook(uiState.lastReadBook.id) }
                )
            }
        }

        // Sheets
        when (uiState.activeSheet) {
            ActiveSheet.FILTER -> {
                FilterBottomSheet(
                    activeFilter = uiState.activeFilter,
                    totalCount = uiState.allBooks.size,
                    downloadedCount = uiState.allBooks.count { it.isDownloaded },
                    unreadCount = uiState.allBooks.count { it.progressPercent == 0f },
                    booksCount = uiState.allBooks.count { !it.isSample },
                    samplesCount = uiState.allBooks.count { it.isSample },
                    onFilterChange = onFilterChange,
                    onDismiss = onDismissSheet
                )
            }
            ActiveSheet.VIEW_SORT -> {
                ViewSortBottomSheet(
                    viewMode = uiState.viewMode,
                    sortMode = uiState.sortMode,
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
                        onAssignCollection = { coll -> onAssignCollection(uiState.selectedBookForMenu.id, coll) },
                        onCreateCollection = { onOpenSheet(ActiveSheet.CREATE_COLLECTION, uiState.selectedBookForMenu) },
                        onDismiss = onDismissSheet
                    )
                }
            }
            ActiveSheet.CREATE_COLLECTION -> {
                CreateCollectionBottomSheet(
                    input = uiState.newCollectionNameInput,
                    onInputChange = onNewCollectionNameChanged,
                    onSave = { onCreateCollection(uiState.newCollectionNameInput) },
                    onDismiss = onDismissSheet
                )
            }
            null -> { /* No sheet open */ }
        }
    }
}

@Composable
private fun LibraryGridView(
    books: List<BookUiModel>,
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
            modifier = Modifier
                .fillMaxSize()
                .testTag("library_books_grid"),
            contentPadding = PaddingValues(
                horizontal = OpusDimensions.ScreenMarginHorizontal,
                vertical = OpusDimensions.SpacingSmall
            ),
            horizontalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingStandard),
            verticalArrangement = Arrangement.spacedBy(OpusDimensions.SpacingLarge)
        ) {
            items(books, key = { it.id }) { book ->
                OpusCoverTile(
                    title = book.title,
                    author = book.author,
                    isDownloaded = book.isDownloaded,
                    isSample = book.isSample,
                    isNew = book.isNew,
                    progressPercent = book.progressPercent,
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
            modifier = Modifier
                .fillMaxSize()
                .testTag("library_books_list")
        ) {
            items(books, key = { it.id }) { book ->
                OpusListRow(
                    title = book.title,
                    subtitle = book.author,
                    onClick = { onOpenBook(book.id) },
                    leadingContent = {
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
        // "New collection" creation row
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

        if (collections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("collections_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No collections yet",
                    style = OpusTypography.bodyLarge,
                    color = OpusColors.TextSecondaryDark
                )
            }
        } else {
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
    activeFilter: LibraryFilter,
    totalCount: Int,
    downloadedCount: Int,
    unreadCount: Int,
    booksCount: Int,
    samplesCount: Int,
    onFilterChange: (LibraryFilter) -> Unit,
    onDismiss: () -> Unit
) {
    OpusBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("library_filter_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OpusDimensions.ScreenMarginHorizontal)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter",
                    style = OpusTypography.headlineMedium,
                    color = OpusColors.TextPrimaryDark
                )
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OpusColors.SurfaceVariantDark),
                    modifier = Modifier.testTag("filter_done_button")
                ) {
                    Text("Done", color = OpusColors.TextPrimaryDark)
                }
            }

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingStandard))

            OpusCheckboxRow(
                title = "All ($totalCount)",
                checked = activeFilter == LibraryFilter.ALL,
                onCheckedChange = { onFilterChange(LibraryFilter.ALL) },
                modifier = Modifier.testTag("filter_opt_all")
            )

            OpusCheckboxRow(
                title = "Downloaded ($downloadedCount)",
                checked = activeFilter == LibraryFilter.DOWNLOADED,
                onCheckedChange = { onFilterChange(LibraryFilter.DOWNLOADED) },
                modifier = Modifier.testTag("filter_opt_downloaded")
            )

            OpusCheckboxRow(
                title = "Unread ($unreadCount)",
                checked = activeFilter == LibraryFilter.UNREAD,
                onCheckedChange = { onFilterChange(LibraryFilter.UNREAD) },
                modifier = Modifier.testTag("filter_opt_unread")
            )

            OpusCheckboxRow(
                title = "Books ($booksCount)",
                checked = activeFilter == LibraryFilter.BOOKS,
                onCheckedChange = { onFilterChange(LibraryFilter.BOOKS) },
                modifier = Modifier.testTag("filter_opt_books")
            )

            OpusCheckboxRow(
                title = "Samples ($samplesCount)",
                checked = activeFilter == LibraryFilter.SAMPLES,
                onCheckedChange = { onFilterChange(LibraryFilter.SAMPLES) },
                modifier = Modifier.testTag("filter_opt_samples")
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingXXLarge))
        }
    }
}

@Composable
private fun ViewSortBottomSheet(
    viewMode: LibraryViewMode,
    sortMode: LibrarySortMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    onSortModeChange: (LibrarySortMode) -> Unit,
    onDismiss: () -> Unit
) {
    OpusBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("library_view_sort_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OpusDimensions.ScreenMarginHorizontal)
        ) {
            Text(
                text = "View",
                style = OpusTypography.headlineMedium,
                color = OpusColors.TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(OpusDimensions.SpacingSmall))

            OpusCheckboxRow(
                title = "Grid View",
                checked = viewMode == LibraryViewMode.GRID,
                onCheckedChange = { onViewModeChange(LibraryViewMode.GRID) },
                modifier = Modifier.testTag("view_opt_grid")
            )
            OpusCheckboxRow(
                title = "List View",
                checked = viewMode == LibraryViewMode.LIST,
                onCheckedChange = { onViewModeChange(LibraryViewMode.LIST) },
                modifier = Modifier.testTag("view_opt_list")
            )
            OpusCheckboxRow(
                title = "Collections View",
                checked = viewMode == LibraryViewMode.COLLECTIONS,
                onCheckedChange = { onViewModeChange(LibraryViewMode.COLLECTIONS) },
                modifier = Modifier.testTag("view_opt_collections")
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingStandard))

            Text(
                text = "Sort",
                style = OpusTypography.headlineMedium,
                color = OpusColors.TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(OpusDimensions.SpacingSmall))

            OpusCheckboxRow(
                title = "Recent",
                checked = sortMode == LibrarySortMode.RECENT,
                onCheckedChange = { onSortModeChange(LibrarySortMode.RECENT) },
                modifier = Modifier.testTag("sort_opt_recent")
            )
            OpusCheckboxRow(
                title = "Title",
                checked = sortMode == LibrarySortMode.TITLE,
                onCheckedChange = { onSortModeChange(LibrarySortMode.TITLE) },
                modifier = Modifier.testTag("sort_opt_title")
            )
            OpusCheckboxRow(
                title = "Author",
                checked = sortMode == LibrarySortMode.AUTHOR,
                onCheckedChange = { onSortModeChange(LibrarySortMode.AUTHOR) },
                modifier = Modifier.testTag("sort_opt_author")
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingXXLarge))
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
                title = if (book.progressPercent >= 100f) "Mark as Unread" else "Mark as Read",
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
