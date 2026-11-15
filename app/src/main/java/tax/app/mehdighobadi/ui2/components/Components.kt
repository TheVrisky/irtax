package tax.app.mehdighobadi.ui2.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import tax.app.mehdighobadi.ui2.theme.LocalMagPalette

val CardShape: Shape = RoundedCornerShape(24.dp)
val SmallShape: Shape = RoundedCornerShape(18.dp)
val PillShape: Shape = RoundedCornerShape(50)

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    tint: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val p = LocalMagPalette.current
    Surface(
        modifier = modifier,
        shape = shape,
        color = tint ?: p.surface.copy(alpha = if (p.isDark) 0.72f else 0.86f),
        tonalElevation = 0.dp,
        shadowElevation = if (p.isDark) 0.dp else 10.dp,
        border = BorderStroke(1.dp, p.line.copy(alpha = if (p.isDark) 0.5f else 0.7f)),
        content = content
    )
}

@Composable
fun GlassOverlayBackdrop(modifier: Modifier = Modifier) {
    val p = LocalMagPalette.current
    Box(
        modifier = modifier
            .clip(CardShape)
            .background(p.glassOverlay)
    )
}

// Fancy gradient hero background decoration
@Composable
fun HeroGradientBox(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val p = LocalMagPalette.current
    Box(
        modifier = modifier
            .clip(CardShape)
            .background(p.heroGradient),
        content = content
    )
}

// Animated count-up number
@Composable
fun AnimatedCount(target: Int, durationMs: Int = 900): Int {
    var started by remember { mutableStateOf(false) }
    val value by animateFloatAsState(
        targetValue = if (started) target.toFloat() else 0f,
        animationSpec = tween(durationMs),
        label = "count"
    )
    LaunchedEffect(target) { started = true }
    return value.toInt()
}
