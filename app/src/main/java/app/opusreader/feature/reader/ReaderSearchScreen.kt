package app.opusreader.feature.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography
import app.opusreader.engine.model.SearchHit

@Composable
fun ReaderSearchScreen(
    searchQuery: String,
    searchResults: List<SearchHit>,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onSelectHit: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OpusColors.SearchBodyDark)
            .testTag("reader_search_screen")
    ) {
        // Search Header Bar (matching D12, y=90..250)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OpusDimensions.SearchHeaderHeight)
                .background(OpusColors.SearchHeaderDark)
                .padding(horizontal = OpusDimensions.ScreenMarginHorizontal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("search_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = OpusColors.TextPrimaryDark,
                    modifier = Modifier.size(OpusDimensions.IconMedium)
                )
            }

            Spacer(modifier = Modifier.width(OpusDimensions.SpacingSmall))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = "Search this book",
                            style = OpusTypography.bodyLarge,
                            color = OpusColors.TextSecondaryDark
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OpusColors.TextPrimaryDark,
                        unfocusedTextColor = OpusColors.TextPrimaryDark,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = OpusColors.AccentBlue
                    ),
                    textStyle = OpusTypography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_input")
                )
                HorizontalDivider(
                    thickness = OpusDimensions.SearchFieldUnderlineThickness,
                    color = OpusColors.SearchFieldUnderline
                )
            }

            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.testTag("search_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = OpusColors.TextSecondaryDark,
                        modifier = Modifier.size(OpusDimensions.IconSmall)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(OpusDimensions.IconButtonMedium)
                        .testTag("search_icon_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = OpusColors.TextSecondaryDark,
                        modifier = Modifier.size(OpusDimensions.IconSmall)
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = OpusDimensions.SearchDividerThickness,
            color = OpusColors.DividerDark
        )

        // Results or Empty Prompt
        if (searchQuery.isBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = OpusDimensions.SearchPromptPaddingTop)
                    .testTag("search_empty_prompt"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Find exactly what you're looking for in",
                    style = OpusTypography.bodyLarge,
                    color = OpusColors.TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(OpusDimensions.SpacingSmall))
                Text(
                    text = "your book and notes.",
                    style = OpusTypography.bodyLarge,
                    color = OpusColors.TextSecondaryDark
                )
            }
        } else if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(OpusDimensions.ScreenMarginHorizontal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSearching) "Searching..." else "No results found for \"$searchQuery\"",
                    style = OpusTypography.bodyLarge,
                    color = OpusColors.TextSecondaryDark,
                    modifier = Modifier.testTag("search_no_results")
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("search_results_list")
            ) {
                itemsIndexed(searchResults) { index, hit ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectHit(index) }
                            .padding(
                                horizontal = OpusDimensions.ScreenMarginHorizontal,
                                vertical = OpusDimensions.SpacingMedium
                            )
                            .testTag("search_hit_$index")
                    ) {
                        Text(
                            text = "Location ${hit.locator.charOffset}",
                            style = OpusTypography.labelSmall,
                            color = OpusColors.LinkBlueDark
                        )
                        Spacer(modifier = Modifier.height(OpusDimensions.SpacingXSmall))
                        Text(
                            text = hit.snippet,
                            style = OpusTypography.bodyMedium,
                            color = OpusColors.TextPrimaryDark
                        )
                    }
                    HorizontalDivider(
                        thickness = OpusDimensions.SearchDividerThickness,
                        color = OpusColors.DividerDark
                    )
                }
            }
        }
    }
}
