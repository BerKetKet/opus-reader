package app.opusreader.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OpusColors.BottomBarDark)
            .testTag("opus_bottom_nav_bar")
    ) {
        HorizontalDivider(
            thickness = OpusDimensions.DividerThickness,
            color = OpusColors.DividerDark
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OpusDimensions.BottomNavHeight),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.id == selectedId
                val tint = if (isSelected) OpusColors.AccentBlue else OpusColors.TextSecondaryDark

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
                        Text(
                            text = item.label,
                            style = OpusTypography.labelMedium,
                            color = tint
                        )
                    }
                }
            }
        }
    }
}
