package tax.app.mehdighobadi.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

enum class AppTheme(val displayName: String) {
    CLASSIC("Classic"),
    MIDNIGHT("Midnight"),
    OCEAN("Ocean"),
    FOREST("Forest"),
    ROYAL("Royal"),
    SUNSET("Sunset"),
    EMERALD("Emerald"),
    LAVENDER("Lavender")
}

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var currentTheme: AppTheme
        get() = AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.CLASSIC.name) ?: AppTheme.CLASSIC.name)
        set(value) = prefs.edit { putString(KEY_THEME, value.name) }

    var fontSize: Int
        get() = prefs.getInt(KEY_FONT_SIZE, 16)
        set(value) = prefs.edit { putInt(KEY_FONT_SIZE, value) }

    var compactMode: Boolean
        get() = prefs.getBoolean(KEY_COMPACT_MODE, false)
        set(value) = prefs.edit { putBoolean(KEY_COMPACT_MODE, value) }

    var reduceMotion: Boolean
        get() = prefs.getBoolean(KEY_REDUCE_MOTION, false)
        set(value) = prefs.edit { putBoolean(KEY_REDUCE_MOTION, value) }

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
