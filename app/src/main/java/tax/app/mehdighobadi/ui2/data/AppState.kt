package tax.app.mehdighobadi.ui2.data

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import tax.app.mehdighobadi.data.TaxData

object AppState {
    var readArticles = mutableStateMapOf<String, Boolean>()
    var favorites = mutableStateMapOf<String, Boolean>()
    var studyMinutes = mutableStateOf(0)
    var darkMode = mutableStateOf(false)
    var reduceMotion = mutableStateOf(false)

    fun load(ctx: Context) {
        val prefs = ctx.getSharedPreferences("mag_state", Context.MODE_PRIVATE)
        readArticles.clear()
        favorites.clear()
        prefs.getStringSet("read", emptySet())?.forEach { readArticles[it] = true }
        prefs.getStringSet("fav", emptySet())?.forEach { favorites[it] = true }
        studyMinutes.value = prefs.getInt("minutes", 0)
        darkMode.value = prefs.getBoolean("dark", false)
        reduceMotion.value = prefs.getBoolean("reduce", false)
    }

    fun persist(ctx: Context) {
        val prefs = ctx.getSharedPreferences("mag_state", Context.MODE_PRIVATE)
        prefs.edit()
            .putStringSet("read", readArticles.keys.toSet())
            .putStringSet("fav", favorites.keys.toSet())
            .putInt("minutes", studyMinutes.value)
            .putBoolean("dark", darkMode.value)
            .putBoolean("reduce", reduceMotion.value)
            .apply()
    }

    fun toggleRead(ctx: Context, key: String) {
        readArticles[key] = !(readArticles[key] ?: false)
        persist(ctx)
    }

    fun toggleFav(ctx: Context, key: String) {
        favorites[key] = !(favorites[key] ?: false)
        persist(ctx)
    }

    fun addMinutes(ctx: Context, m: Int) {
        studyMinutes.value += m
        persist(ctx)
    }

    fun totalArticles(): Int = TaxData.content.chapters.sumOf { it.articles.size }
    fun readCount(): Int = readArticles.count { it.value }
    fun favCount(): Int = favorites.count { it.value }
    fun progressPct(): Int = if (totalArticles() == 0) 0 else (readCount() * 100 / totalArticles())
}
