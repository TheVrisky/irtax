package tax.app.mehdighobadi.ui2.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- Palette (Indigo / Turquoise accents on soft greys) ----------
val Indigo = Color(0xFF5B6CFF)
val IndigoDeep = Color(0xFF3A47C9)
val Turquoise = Color(0xFF2DD4BF)
val TurquoiseDeep = Color(0xFF0FA793)

// Light neutrals
val GreyBg = Color(0xFFF6F7FB)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceTintLight = Color(0xFFEEF1FF)
val InkLight = Color(0xFF1E2230)
val InkSoftLight = Color(0xFF5B6072)
val LineLight = Color(0xFFE4E7F2)

// Dark neutrals
val GreyBgDark = Color(0xFF0F1220)
val SurfaceDark = Color(0xFF181C2C)
val SurfaceTintDark = Color(0xFF20263C)
val InkDark = Color(0xFFE9ECF7)
val InkSoftDark = Color(0xFF9AA1BC)
val LineDark = Color(0xFF2A3149)

val Success = Color(0xFF22C55E)
val SuccessSoft = Color(0xFFBBF7D0)
val Danger = Color(0xFFEF4444)
val DangerSoft = Color(0xFFFECACA)
val Warning = Color(0xFFF59E0B)

// ---------- Semantic tokens exposed to UI ----------
data class MagPalette(
    val bg: Color,
    val surface: Color,
    val surfaceTint: Color,
    val ink: Color,
    val inkSoft: Color,
    val line: Color,
    val primary: Color,
    val secondary: Color,
    val onPrimary: Color,
    val heroGradient: Brush,
    val glassOverlay: Color,
    val isDark: Boolean
)

val LocalMagPalette = staticCompositionLocalOf {
    MagPalette(
        bg = GreyBg,
        surface = SurfaceLight,
        surfaceTint = SurfaceTintLight,
        ink = InkLight,
        inkSoft = InkSoftLight,
        line = LineLight,
        primary = Indigo,
        secondary = Turquoise,
        onPrimary = Color.White,
        heroGradient = Brush.linearGradient(listOf(Indigo, Turquoise)),
        glassOverlay = Color(0xCCFFFFFF),
        isDark = false
    )
}

fun lightPalette() = MagPalette(
    bg = GreyBg,
    surface = SurfaceLight,
    surfaceTint = SurfaceTintLight,
    ink = InkLight,
    inkSoft = InkSoftLight,
    line = LineLight,
    primary = Indigo,
    secondary = Turquoise,
    onPrimary = Color.White,
    heroGradient = Brush.linearGradient(listOf(Indigo, IndigoDeep)),
    glassOverlay = Color(0xE6FFFFFF),
    isDark = false
)

fun darkPalette() = MagPalette(
    bg = GreyBgDark,
    surface = SurfaceDark,
    surfaceTint = SurfaceTintDark,
    ink = InkDark,
    inkSoft = InkSoftDark,
    line = LineDark,
    primary = Color(0xFF8B97FF),
    secondary = Turquoise,
    onPrimary = Color(0xFF0B0E18),
    heroGradient = Brush.linearGradient(listOf(Color(0xFF4A57E0), TurquoiseDeep)),
    glassOverlay = Color(0xCC181C2C),
    isDark = true
)

// ---------- Typography (Vazirmatn-style hierarchy; uses system fallback offline) ----------
private val VazirFamily: FontFamily = FontFamily.Default

val MagTypography = Typography(
    displaySmall = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 38.sp),
    headlineSmall = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 26.sp),
    bodyLarge = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 24.sp),
    bodySmall = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Light, fontSize = 12.5f.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp)
)

@Composable
fun MagTheme(dark: Boolean, content: @Composable () -> Unit) {
    val palette = if (dark) darkPalette() else lightPalette()
    val scheme = if (dark) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            secondary = palette.secondary,
            background = palette.bg,
            surface = palette.surface,
            onSurface = palette.ink,
            outline = palette.line
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            secondary = palette.secondary,
            background = palette.bg,
            surface = palette.surface,
            onSurface = palette.ink,
            outline = palette.line
        )
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalMagPalette provides palette) {
        MaterialTheme(colorScheme = scheme, typography = MagTypography, content = content)
    }
}
