package app.opusreader.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.opusreader.design.component.OpusCoverTile
import app.opusreader.design.component.OpusResumeCover
import app.opusreader.design.component.OpusSearchPill
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography
import app.opusreader.feature.library.BookUiModel

@Composable
fun HomeScreen(
    books: List<BookUiModel>,
    resumeBook: BookUiModel?,
    onOpenBook: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OpusColors.BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = OpusDimensions.BottomNavHeight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = OpusDimensions.SearchBarMarginHorizontal,
                        vertical = OpusDimensions.SpacingStandard
                    )
            ) {
                OpusSearchPill(
                    query = "",
                    onQueryChange = { _ -> },
                    onClear = onSearchClick,
                    placeholder = "Search Kindle",
                    onSearchAction = onSearchClick
                )
            }

            if (books.isNotEmpty()) {
                Text(
                    text = "From your Library",
                    style = OpusTypography.titleLarge,
                    color = OpusColors.TextPrimaryDark,
                    modifier = Modifier.padding(
                        horizontal = OpusDimensions.ScreenMarginHorizontal,
                        vertical = OpusDimensions.SpacingSmall
                    )
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = OpusDimensions.ScreenMarginHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(OpusDimensions.GridColumnGap),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(books, key = { it.id }) { book ->
                        OpusCoverTile(
                            title = book.title,
                            author = book.author,
                            onClick = { onOpenBook(book.id) },
                            onLongClick = { onOpenBook(book.id) },
                            progressPercent = book.progressPercent,
                            isDownloaded = book.isDownloaded,
                            isSample = book.isSample,
                            isNew = book.isNew
                        )
                    }
                }
            }
        }

        if (resumeBook != null) {
            OpusResumeCover(
                title = resumeBook.title,
                onResumeClick = { onOpenBook(resumeBook.id) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = OpusDimensions.ResumeCoverMarginStart,
                        bottom = OpusDimensions.BottomNavHeight + OpusDimensions.ResumeCoverOffsetBottom
                    )
            )
        }
    }
}
