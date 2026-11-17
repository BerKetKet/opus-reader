package app.opusreader.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.opusreader.design.R
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions

@Composable
fun OpusReaderTopBar(
    isBookmarked: Boolean,
    onBackClick: () -> Unit,
    onContentsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onDisplaySettingsClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(OpusDimensions.ReaderTopBarHeight)
            .background(OpusColors.ReaderChromeTopDark)
            .padding(horizontal = 8.dp)
            .testTag("reader_top_bar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back navigation
        Box(
            modifier = Modifier
                .size(OpusDimensions.MinTouchTarget)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                )
                .testTag("reader_back_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reader_back),
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Table of Contents
        Box(
            modifier = Modifier
                .size(OpusDimensions.MinTouchTarget)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onContentsClick
                )
                .testTag("reader_contents_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reader_toc),
                contentDescription = "Contents",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Search
        Box(
            modifier = Modifier
                .size(OpusDimensions.MinTouchTarget)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSearchClick
                )
                .testTag("reader_search_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reader_search),
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Bookmark
        Box(
            modifier = Modifier
                .size(OpusDimensions.MinTouchTarget)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBookmarkClick
                )
                .testTag("reader_bookmark_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isBookmarked) "Remove Bookmark" else "Add Bookmark",
                tint = if (isBookmarked) OpusColors.BookmarkRed else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Display Settings ("Aa")
        Box(
            modifier = Modifier
                .size(OpusDimensions.MinTouchTarget)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDisplaySettingsClick
                )
                .testTag("reader_display_settings_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reader_aa),
                contentDescription = "Display Settings",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Overflow Menu
        Box(
            modifier = Modifier
                .size(OpusDimensions.MinTouchTarget)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOverflowClick
                )
                .testTag("reader_overflow_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reader_overflow),
                contentDescription = "More Options",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
