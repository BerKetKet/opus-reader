package app.opusreader.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

data class OpusNavItem(
    val id: Int,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun OpusBottomNavBar(
    items: List<OpusNavItem>,
    selectedId: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = OpusColors.BackgroundDark,
    hasLeadingResumeSlot: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(OpusDimensions.BottomNavHeight)
            .background(backgroundColor)
            .testTag("opus_bottom_nav_bar")
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = OpusColors.DividerDark
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasLeadingResumeSlot) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
            items.forEach { item ->
                val isSelected = item.id == selectedId
                val tint = if (isSelected) OpusColors.NavSelectedBlue else OpusColors.NavUnselectedGray

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onItemSelected(item.id) }
                        .testTag(item.tag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = tint,
                            modifier = Modifier.size(OpusDimensions.BottomNavIconSize)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            style = OpusTypography.labelMedium.copy(fontSize = 10.sp, lineHeight = 13.sp),
                            color = tint
                        )
                    }
                }
            }
        }
    }
}
