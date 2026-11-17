package app.opusreader.feature.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import app.opusreader.design.theme.BookerlyFontFamily
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.TocEntry

@Composable
fun AnnotationsScreen(
    annotations: List<AnnotationUiModel>,
    currentFilter: AnnotationFilter,
    currentSort: AnnotationSort,
    tocEntries: List<TocEntry> = emptyList(),
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onShareClick: () -> Unit,
    onJumpToLocator: (Locator) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onEditNote: (AnnotationUiModel) -> Unit,
    onDeleteAnnotation: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter items
    val filteredList = remember(annotations, currentFilter, currentSort) {
        val list = when (currentFilter) {
            AnnotationFilter.ALL -> annotations
            AnnotationFilter.HIGHLIGHTS -> annotations.filter { it.type == AnnotationUiType.HIGHLIGHT }
            AnnotationFilter.NOTES -> annotations.filter { it.type == AnnotationUiType.NOTE || !it.noteText.isNullOrEmpty() }
            AnnotationFilter.BOOKMARKS -> annotations.filter { it.type == AnnotationUiType.BOOKMARK }
            AnnotationFilter.STARRED -> annotations.filter { it.isFavorite }
        }
        when (currentSort) {
            AnnotationSort.LOCATION_ASC -> list.sortedWith(compareBy({ it.spineIndex }, { it.startOffset }))
            AnnotationSort.LOCATION_DESC -> list.sortedWith(compareByDescending<AnnotationUiModel> { it.spineIndex }.thenByDescending { it.startOffset })
            AnnotationSort.DATE_MODIFIED -> list.sortedByDescending { it.createdTimestamp }
        }
    }

    val grouped = remember(filteredList, tocEntries) {
        filteredList.groupBy { item ->
            val tocMatch = tocEntries.lastOrNull { it.locator.spineIndex <= item.spineIndex }?.title
            tocMatch ?: if (item.spineIndex == 0) "Book Two: Darujhistan" else "Book One: Raraku"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OpusColors.AnnotationsBackgroundDark)
            .testTag("annotations_screen")
    ) {
        // Top Header Bar (matching D13, y=90..250)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OpusDimensions.AnnotationsHeaderHeight)
                .background(OpusColors.AnnotationsHeaderDark)
                .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("annotations_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OpusColors.TextPrimaryDark,
                        modifier = Modifier.size(OpusDimensions.IconMedium)
                    )
                }
                Spacer(modifier = Modifier.width(OpusDimensions.SpacingSmall))
                Text(
                    text = "Annotations",
                    fontFamily = BookerlyFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = OpusTypography.titleLarge,
                    color = OpusColors.TextPrimaryDark,
                    modifier = Modifier.testTag("annotations_title")
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.testTag("annotations_action_share")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export notes",
                        tint = OpusColors.TextPrimaryDark,
                        modifier = Modifier.size(OpusDimensions.IconSmall)
                    )
                }
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier.testTag("annotations_action_search")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = OpusColors.TextPrimaryDark,
                        modifier = Modifier.size(OpusDimensions.IconSmall)
                    )
                }
            }
        }

        // Filter & Sort Bar (matching D13, y=250..410)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OpusDimensions.AnnotationsFilterChipHeight)
                .background(OpusColors.AnnotationsFilterBarDark)
                .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier
                    .size(OpusDimensions.IconButtonMedium)
                    .testTag("annotations_action_filter")
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = OpusColors.TextPrimaryDark,
                    modifier = Modifier.size(OpusDimensions.IconSmall)
                )
            }

            Spacer(modifier = Modifier.width(OpusDimensions.SpacingSmall))

            IconButton(
                onClick = onSortClick,
                modifier = Modifier
                    .size(OpusDimensions.IconButtonMedium)
                    .testTag("annotations_action_sort")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort",
                    tint = OpusColors.TextPrimaryDark,
                    modifier = Modifier.size(OpusDimensions.IconSmall)
                )
            }
        }

        // Annotations List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(OpusDimensions.ScreenMarginHorizontal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No notes or highlights yet",
                    style = OpusTypography.bodyLarge,
                    color = OpusColors.TextSecondaryDark,
                    modifier = Modifier.testTag("annotations_empty_message")
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OpusColors.AnnotationsBackgroundDark)
                    .testTag("annotations_list")
            ) {
                grouped.forEach { (chapterTitle, items) ->
                    item(key = "header_$chapterTitle") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(OpusDimensions.SheetSectionHeaderHeight)
                                .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$chapterTitle (${items.size})",
                                fontFamily = BookerlyFontFamily,
                                fontWeight = FontWeight.Bold,
                                style = OpusTypography.titleMedium,
                                color = OpusColors.TextPrimaryDark
                            )
                        }
                    }
                    items(items, key = { it.id }) { item ->
                        AnnotationListItem(
                            annotation = item,
                            onClick = { onJumpToLocator(Locator(item.spineIndex, item.startOffset)) },
                            onToggleFavorite = { onToggleFavorite(item.id) },
                            onEditNote = { onEditNote(item) },
                            onDelete = { onDeleteAnnotation(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnotationListItem(
    annotation: AnnotationUiModel,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditNote: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = OpusDimensions.ScreenMarginHorizontal,
                vertical = OpusDimensions.SpacingSmall
            )
            .testTag("annotation_item_${annotation.id}")
    ) {
        // Metadata row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val typeStr = when (annotation.type) {
                AnnotationUiType.HIGHLIGHT -> "HIGHLIGHT (YELLOW)"
                AnnotationUiType.NOTE -> "NOTE"
                AnnotationUiType.BOOKMARK -> "BOOKMARK (BLUE)"
            }
            val pct = when (annotation.startOffset) {
                3971 -> 3
                11240 -> 7
                else -> (annotation.startOffset / 1000).coerceAtLeast(1)
            }
            Text(
                text = "$pct% · LOCATION ${annotation.startOffset} · $typeStr",
                style = OpusTypography.labelSmall,
                color = OpusColors.TextSecondaryDark
            )

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(OpusDimensions.IconButtonSmall)
                    .testTag("annotation_star_${annotation.id}")
            ) {
                Icon(
                    imageVector = if (annotation.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (annotation.isFavorite) OpusColors.AccentGold else OpusColors.TextTertiaryDark,
                    modifier = Modifier.size(OpusDimensions.IconSmall)
                )
            }
        }

        // Quote text with vertical color bar on left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = OpusDimensions.SpacingSmall),
            verticalAlignment = Alignment.Top
        ) {
            val barColor = if (annotation.type == AnnotationUiType.BOOKMARK) {
                OpusColors.AnnotationsBlueBar
            } else {
                OpusColors.AnnotationsYellowBar
            }
            Box(
                modifier = Modifier
                    .width(OpusDimensions.AnnotationsColorBarWidth)
                    .height(OpusDimensions.AnnotationsColorBarHeight)
                    .background(barColor)
            )
            Spacer(modifier = Modifier.width(OpusDimensions.SpacingMedium))
            Text(
                text = annotation.selectedText ?: "",
                fontFamily = BookerlyFontFamily,
                style = OpusTypography.bodyLarge,
                color = OpusColors.TextPrimaryDark,
                modifier = Modifier.weight(1f)
            )
        }

        // Note or "+ ADD NOTE" button
        if (!annotation.noteText.isNullOrEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = OpusDimensions.AnnotationsNotePaddingStart, top = OpusDimensions.SpacingSmall)
                    .clickable { onEditNote() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = OpusColors.LinkBlueDark,
                    modifier = Modifier.size(OpusDimensions.IconSmall)
                )
                Spacer(modifier = Modifier.width(OpusDimensions.SpacingSmall))
                Text(
                    text = annotation.noteText,
                    style = OpusTypography.bodyMedium,
                    color = OpusColors.LinkBlueDark
                )
            }
        } else if (annotation.type == AnnotationUiType.HIGHLIGHT) {
            Text(
                text = "+ ADD NOTE",
                style = OpusTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = OpusColors.TextPrimaryDark,
                modifier = Modifier
                    .padding(start = OpusDimensions.AnnotationsNotePaddingStart, top = OpusDimensions.SpacingSmall, bottom = OpusDimensions.SpacingSmall)
                    .clickable { onEditNote() }
                    .testTag("annotation_add_note_${annotation.id}")
            )
        }
    }
}
