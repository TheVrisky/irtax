package tax.app.mehdighobadi.theme

import android.app.Activity
import android.content.Context\nimport android.content.res.Configuration
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import tax.app.mehdighobadi.R

/** حالت نمایش: روشن / تاریک / هماهنگ با سیستم */
enum class DarkMode(val key: String) {
    LIGHT("light"), DARK("dark"), SYSTEM("system");

    companion object {
        fun fromKey(key: String?): DarkMode = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

/** رنگ اصلی برنامه: نیلی (پیش‌فرض) / فیروزه‌ای / بنفش */
enum class Accent(val key: String, val displayName: String, val swatchColorRes: Int) {
    INDIGO("indigo", "نیلی", R.color.primary),
    TEAL("teal", "فیروزه‌ای", R.color.primary_teal),
    VIOLET("violet", "بنفش", R.color.primary_violet);

    val styleRes: Int
        get() = when (this) {
            INDIGO -> R.style.ThemeOverlay_irtax_AccentIndigo
            TEAL -> R.style.ThemeOverlay_irtax_AccentTeal
            VIOLET -> R.style.ThemeOverlay_irtax_AccentViolet
        }

    companion object {
        fun fromKey(key: String?): Accent = entries.firstOrNull { it.key == key } ?: INDIGO
    }
}

/**
 * مدیریت تم نسخه‌ی بازطراحی‌شده:
 * حالت روشن/تاریک، رنگ اصلی، اندازه‌ی فونت و کاهش انیمیشن.
 */
class ThemeManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var darkMode: DarkMode
        get() = DarkMode.fromKey(prefs.getString(KEY_DARK_MODE, DarkMode.SYSTEM.key))
        set(value) = prefs.edit { putString(KEY_DARK_MODE, value.key) }

    var accent: Accent
        get() = Accent.fromKey(prefs.getString(KEY_ACCENT, Accent.INDIGO.key))
        set(value) = prefs.edit { putString(KEY_ACCENT, value.key) }

    var fontSize: Int
        get() = prefs.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE).coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        set(value) = prefs.edit { putInt(KEY_FONT_SIZE, value.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)) }

    var reduceMotion: Boolean
        get() = prefs.getBoolean(KEY_REDUCE_MOTION, false)
        set(value) = prefs.edit { putBoolean(KEY_REDUCE_MOTION, value) }

    /** قبل از super.onCreate هر Activity صدا زده شود */
    fun applyNightMode() {
        AppCompatDelegate.setDefaultNightMode(
            when (darkMode) {
                DarkMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                DarkMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                DarkMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    /** قبل از setContentView صدا زده شود */
    fun applyAccent(activity: Activity) {
        activity.theme.applyStyle(accent.styleRes, true)
    }

    fun resetToDefaults() {
        prefs.edit {
            putString(KEY_DARK_MODE, DarkMode.SYSTEM.key)
            putString(KEY_ACCENT, Accent.INDIGO.key)
            putInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE)
            putBoolean(KEY_REDUCE_MOTION, false)
        }
    }

    companion object {
        private const val PREFS_NAME = "irtax_prefs_v2"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_ACCENT = "accent"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_REDUCE_MOTION = "reduce_motion"

        const val MIN_FONT_SIZE = 13
        const val MAX_FONT_SIZE = 22
        const val DEFAULT_FONT_SIZE = 16
    }
}
