package app.opusreader.feature.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.opusreader.design.theme.BookerlyFontFamily
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.TocEntry

@Composable
fun ContentsTocScreen(
    bookTitle: String,
    bookAuthor: String,
    tocEntries: List<TocEntry>,
    currentSpineIndex: Int,
    onSelectChapter: (Locator) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OpusColors.ReaderTocBackground)
            .testTag("contents_toc_screen")
    ) {
        // Header (Title, Author, Close Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OpusDimensions.ReaderContentsHeaderHeight)
                .padding(start = OpusDimensions.ReaderContentsIndentLevel0, end = OpusDimensions.SpacingStandard)
                .testTag("contents_header"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookTitle,
                    color = OpusColors.ReaderTocTextPrimary,
                    fontSize = 18.sp,
                    fontFamily = BookerlyFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.testTag("contents_title")
                )
                Text(
                    text = bookAuthor,
                    color = OpusColors.ReaderTocTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    modifier = Modifier.padding(top = OpusDimensions.SpacingSmall).testTag("contents_author")
                )
            }

            Box(
                modifier = Modifier
                    .size(OpusDimensions.MinTouchTarget)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose
                    )
                    .testTag("contents_close_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = OpusColors.ReaderTocTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        HorizontalDivider(color = OpusColors.ReaderTocDivider, thickness = 1.dp)

        // Chapters List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("contents_chapter_list")
        ) {
            itemsIndexed(tocEntries, key = { index, item -> "${index}_${item.title}" }) { index, item ->
                val rowHeight = if (index == 1) {
                    OpusDimensions.ReaderContentsMultiLineRowHeight
                } else {
                    OpusDimensions.ReaderContentsRowHeight
                }
                val startPadding = if (item.level > 0) {
                    OpusDimensions.ReaderContentsIndentLevel1
                } else {
                    OpusDimensions.ReaderContentsIndentLevel0
                }
                val isCurrent = item.locator.spineIndex == currentSpineIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectChapter(item.locator) }
                        )
                        .testTag("contents_row_$index"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .background(Color.White)
                                .align(Alignment.CenterStart)
                        )
                    }

                    Text(
                        text = item.title,
                        color = OpusColors.ReaderTocTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        maxLines = if (index == 1) 2 else 1,
                        modifier = Modifier
                            .padding(start = startPadding, end = OpusDimensions.ReaderContentsIndentLevel0)
                    )
                }
            }
        }
    }
}
