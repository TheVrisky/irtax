package tax.app.mehdighobadi.theme

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import tax.app.mehdighobadi.R

enum class AppTheme(val displayName: String) {
    CLASSIC("کلاسیک"), MIDNIGHT("نیمه‌شب"), OCEAN("اقیانوسی"), FOREST("جنگلی"),
    ROYAL("سلطنتی"), SUNSET("غروب"), EMERALD("زمردی"), LAVENDER("اسطوخودوسی")
}

data class ThemePalette(val accent: Int, val background: Int, val surface: Int, val text: Int, val soft: Int, val article: Int)

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    var currentTheme: AppTheme
        get() = runCatching { AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.CLASSIC.name) ?: AppTheme.CLASSIC.name) }.getOrDefault(AppTheme.CLASSIC)
        set(value) { prefs.edit { putString(KEY_THEME, value.name) } }
    var fontSize: Int
        get() = prefs.getInt(KEY_FONT_SIZE, 16).coerceIn(12, 24)
        set(value) { prefs.edit { putInt(KEY_FONT_SIZE, value.coerceIn(12, 24)) } }
    var compactMode: Boolean
        get() = prefs.getBoolean(KEY_COMPACT_MODE, false)
        set(value) { prefs.edit { putBoolean(KEY_COMPACT_MODE, value) } }
    var reduceMotion: Boolean
        get() = prefs.getBoolean(KEY_REDUCE_MOTION, false)
        set(value) { prefs.edit { putBoolean(KEY_REDUCE_MOTION, value) } }

    fun palette(context: Context): ThemePalette {
        val ids = when (currentTheme) {
            AppTheme.CLASSIC -> intArrayOf(R.color.classic_accent,R.color.classic_bg,R.color.classic_surface,R.color.classic_ink,R.color.classic_soft,R.color.classic_article)
            AppTheme.MIDNIGHT -> intArrayOf(R.color.midnight_accent,R.color.midnight_bg,R.color.midnight_surface,R.color.midnight_ink,R.color.midnight_soft,R.color.midnight_article)
            AppTheme.OCEAN -> intArrayOf(R.color.ocean_accent,R.color.ocean_bg,R.color.ocean_surface,R.color.ocean_ink,R.color.ocean_soft,R.color.ocean_article)
            AppTheme.FOREST -> intArrayOf(R.color.forest_accent,R.color.forest_bg,R.color.forest_surface,R.color.forest_ink,R.color.forest_soft,R.color.forest_article)
            AppTheme.ROYAL -> intArrayOf(R.color.royal_accent,R.color.royal_bg,R.color.royal_surface,R.color.royal_ink,R.color.royal_soft,R.color.royal_article)
            AppTheme.SUNSET -> intArrayOf(R.color.sunset_accent,R.color.sunset_bg,R.color.sunset_surface,R.color.sunset_ink,R.color.sunset_soft,R.color.sunset_article)
            AppTheme.EMERALD -> intArrayOf(R.color.emerald_accent,R.color.emerald_bg,R.color.emerald_surface,R.color.emerald_ink,R.color.emerald_soft,R.color.emerald_article)
            AppTheme.LAVENDER -> intArrayOf(R.color.lavender_accent,R.color.lavender_bg,R.color.lavender_surface,R.color.lavender_ink,R.color.lavender_soft,R.color.lavender_article)
        }
        val c = ids.map { ContextCompat.getColor(context, it) }
        return ThemePalette(c[0], c[1], c[2], c[3], c[4], c[5])
    }

    fun applyTo(activity: Activity, root: View, toolbar: Toolbar? = null) {
        val p = palette(activity)
        root.setBackgroundColor(p.background)
        toolbar?.setBackgroundColor(p.accent)
        activity.window.statusBarColor = p.accent
        activity.window.navigationBarColor = p.surface
    }

    fun resetToDefaults() {
        prefs.edit {
            putString(KEY_THEME, AppTheme.CLASSIC.name)
            putInt(KEY_FONT_SIZE, 16)
            putBoolean(KEY_COMPACT_MODE, false)
            putBoolean(KEY_REDUCE_MOTION, false)
        }
    }

    companion object {
        private const val PREFS_NAME = "irtax_settings"
        private const val KEY_THEME = "theme"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_COMPACT_MODE = "compact_mode"
        private const val KEY_REDUCE_MOTION = "reduce_motion"
    }
}
