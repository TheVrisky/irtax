package tax.app.mehdighobadi.ui2.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tax.app.mehdighobadi.ui2.theme.LocalMagPalette

enum class Dest(val label: String, val icon: ImageVector) {
    Home("خانه", Icons.Outlined.Home),
    Library("بانک سوال", Icons.Outlined.LibraryBooks),
    Favorites("نشان‌ها", Icons.Outlined.Bookmarks),
    Stats("پیشرفت", Icons.Outlined.BarChart),
    Settings("تنظیمات", Icons.Outlined.Settings)
}

@Composable
fun BottomPillNav(
    current: Dest,
    onSelect: (Dest) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalMagPalette.current
    val items = Dest.values()
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(p.glassOverlay)
            .padding(6.dp)
    ) {
        val itemWidth = (maxWidth / items.size)
        val activeIndex = items.indexOf(current)
        val offsetPx = with(density) { (itemWidth * activeIndex).toDp() }
        val animatedOffset by animateDpAsState(
            targetValue = offsetPx,
            animationSpec = tween(320),
            label = "pill"
        )

        // Animated pill indicator
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(itemWidth)
                .height(52.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(p.surfaceTint)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.forEach { dest ->
                val selected = dest == current
                val color by animateColorAsState(
                    targetValue = if (selected) p.primary else p.inkSoft,
                    animationSpec = tween(250), label = "c"
                )
                Column(
                    modifier = Modifier
                        .width(itemWidth)
                        .height(52.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(dest) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(dest.icon, contentDescription = dest.label, tint = color, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(
                        dest.label,
                        color = color,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
