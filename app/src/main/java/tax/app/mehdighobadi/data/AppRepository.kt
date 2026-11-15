package tax.app.mehdighobadi.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * لایه‌ی داده‌ی محلی (کاملاً آفلاین):
 * - نشان‌گذاری مواد (علاقه‌مندی‌ها)
 * - آمار مطالعه (مواد خوانده‌شده، روزهای فعالیت، آخرین مطالعه)
 * - موتور پیشنهاد هوشمند ساده بر اساس روند مطالعه
 */
class AppRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---------- نشان‌گذاری‌ها ----------

    fun isFavorite(chapterId: String, articleIndex: Int): Boolean =
        favSet().contains(articleKey(chapterId, articleIndex))

    /** وضعیت جدید نشان را برمی‌گرداند (true = نشان‌شده) */
    fun toggleFavorite(chapterId: String, articleIndex: Int): Boolean {
        val set = favSet().toMutableSet()
        val key = articleKey(chapterId, articleIndex)
        val added = if (set.contains(key)) { set.remove(key); false } else { set.add(key); true }
        prefs.edit { putStringSet(KEY_FAVORITES, set) }
        return added
    }

    /** همه‌ی نشان‌شده‌ها به ترتیب فصل و ماده (مواد حذف‌شده از داده نادیده گرفته می‌شوند) */
    fun favoriteRefs(): List<ArticleRef> {
        val refs = favSet().mapNotNull { key ->
            val parts = key.split(":")
            if (parts.size != 2) return@mapNotNull null
            val chapterId = parts[0]
            val index = parts[1].toIntOrNull() ?: return@mapNotNull null
            ArticleRef(chapterId, index)
        }.filter { it.isValid() }.sortedBy { it.flatIndex() }
        return refs
    }

    // ---------- آمار مطالعه ----------

    fun isRead(chapterId: String, articleIndex: Int): Boolean =
        readSet().contains(articleKey(chapterId, articleIndex))

    fun markRead(chapterId: String, articleIndex: Int) {
        val key = articleKey(chapterId, articleIndex)
        val set = readSet().toMutableSet()
        if (!set.contains(key)) {
            set.add(key)
            prefs.edit { putStringSet(KEY_READ, set) }
        }
        // ثبت روز فعالیت برای «روزهای مطالعه»
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val days = prefs.getStringSet(KEY_DAYS, emptySet())?.toMutableSet() ?: mutableSetOf()
        days.add(today)
        prefs.edit { putStringSet(KEY_DAYS, days) }
        // آخرین مطالعه
        prefs.edit {
            putString(KEY_LAST_CHAPTER, chapterId)
            putInt(KEY_LAST_INDEX, articleIndex)
        }
    }

    fun readCount(): Int = readSet().count { key ->
        val parts = key.split(":")
        parts.size == 2 &&
            TaxData.content.chapters.any { ch ->
                ch.id == parts[0] && (parts[1].toIntOrNull() ?: -1) in ch.articles.indices
            }
    }

    fun chapterReadCount(chapterId: String): Int {
        val chapter = TaxData.content.chapters.find { it.id == chapterId } ?: return 0
        val prefix = "$chapterId:"
        return readSet().count { it.startsWith(prefix) && (it.removePrefix(prefix).toIntOrNull() ?: -1) in chapter.articles.indices }
    }

    fun totalArticles(): Int = TaxData.content.chapters.sumOf { it.articles.size }

    /** پیشرفت کل ۰ تا ۱ */
    fun overallProgress(): Float {
        val total = totalArticles()
        if (total == 0) return 0f
        return readCount().toFloat() / total
    }

    fun chapterProgress(chapterId: String): Float {
        val chapter = TaxData.content.chapters.find { it.id == chapterId } ?: return 0f
        if (chapter.articles.isEmpty()) return 0f
        return chapterReadCount(chapterId).toFloat() / chapter.articles.size
    }

    /** تعداد روزهای متفاوتی که مطالعه ثبت شده */
    fun studyDays(): Int = prefs.getStringSet(KEY_DAYS, emptySet())?.size ?: 0

    /** آخرین ماده‌ی مطالعه‌شده برای «ادامه مطالعه» */
    fun lastReadRef(): ArticleRef? {
        val chapterId = prefs.getString(KEY_LAST_CHAPTER, null) ?: return null
        val index = prefs.getInt(KEY_LAST_INDEX, -1)
        val ref = ArticleRef(chapterId, index)
        return if (ref.isValid()) ref else null
    }

    // ---------- پیشنهاد هوشمند ----------

    /**
     * فصلی با کمترین پیشرفت (اولویت با فصل بعدیِ خوانده‌نشده نسبت به آخرین مطالعه).
     * اگر همه کامل شده باشد null برمی‌گردد.
     */
    fun smartSuggestion(): Chapter? {
        val chapters = TaxData.content.chapters
        val nextFromLast = lastReadRef()?.let { last ->
            val i = chapters.indexOfFirst { it.id == last.chapterId }
            chapters.getOrNull(i + 1)?.takeIf { chapterProgress(it.id) < 1f }
        }
        val candidates = (listOfNotNull(nextFromLast) + chapters).filter { chapterProgress(it.id) < 1f }
        return candidates.minByOrNull { chapterProgress(it.id) }
    }

    // ---------- Helpers ----------

    private fun articleKey(chapterId: String, index: Int) = "$chapterId:$index"

    private fun favSet(): Set<String> = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    private fun readSet(): Set<String> = prefs.getStringSet(KEY_READ, emptySet()) ?: emptySet()

    data class ArticleRef(val chapterId: String, val articleIndex: Int) {
        fun isValid(): Boolean {
            val chapter = TaxData.content.chapters.find { it.id == chapterId } ?: return false
            return articleIndex in chapter.articles.indices
        }
        fun chapter(): Chapter? = TaxData.content.chapters.find { it.id == chapterId }
        fun article(): Article? = chapter()?.articles?.getOrNull(articleIndex)
        fun flatIndex(): Int {
            var flat = 0
            for (ch in TaxData.content.chapters) {
                if (ch.id == chapterId) return flat + articleIndex
                flat += ch.articles.size
            }
            return flat
        }
    }

    companion object {
        private const val PREFS_NAME = "irtax_app_state"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_READ = "read_articles"
        private const val KEY_DAYS = "study_days"
        private const val KEY_LAST_CHAPTER = "last_chapter"
        private const val KEY_LAST_INDEX = "last_index"

        @Volatile
        private var instance: AppRepository? = null

        fun get(context: Context): AppRepository =
            instance ?: synchronized(this) {
                instance ?: AppRepository(context.applicationContext).also { instance = it }
            }
    }
}
