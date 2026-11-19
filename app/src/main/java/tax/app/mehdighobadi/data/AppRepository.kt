package tax.app.mehdighobadi.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Offline study state. Existing chapter:index preferences are migrated once to
 * stable article UUID keys when an article has an ID. Articles without IDs keep
 * a legacy positional key until the bundled content supplies a stable ID.
 */
class AppRepository private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init { migrateLegacyState() }

    fun isFavorite(chapterId: String, articleIndex: Int): Boolean =
        favSet().contains(articleKey(chapterId, articleIndex))

    fun toggleFavorite(chapterId: String, articleIndex: Int): Boolean {
        val set = favSet().toMutableSet()
        val key = articleKey(chapterId, articleIndex)
        val added = if (set.contains(key)) { set.remove(key); false } else { set.add(key); true }
        prefs.edit { putStringSet(KEY_FAVORITES, set) }
        return added
    }

    fun favoriteRefs(): List<ArticleRef> =
        favSet().mapNotNull(::resolveKey).filter { it.isValid() }.sortedBy { it.flatIndex() }

    fun isRead(chapterId: String, articleIndex: Int): Boolean =
        readSet().contains(articleKey(chapterId, articleIndex))

    fun markRead(chapterId: String, articleIndex: Int) {
        val key = articleKey(chapterId, articleIndex)
        val set = readSet().toMutableSet()
        if (set.add(key)) prefs.edit { putStringSet(KEY_READ, set) }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val days = prefs.getStringSet(KEY_DAYS, emptySet())?.toMutableSet() ?: mutableSetOf()
        days.add(today)
        prefs.edit { putStringSet(KEY_DAYS, days) }

        val article = TaxData.content.chapters.find { it.id == chapterId }?.articles?.getOrNull(articleIndex)
        prefs.edit {
            putString(KEY_LAST_CHAPTER, chapterId)
            putInt(KEY_LAST_INDEX, articleIndex)
            putString(KEY_LAST_ARTICLE_ID, article?.id)
        }
    }

    fun readCount(): Int = readSet().count { resolveKey(it)?.isValid() == true }

    fun chapterReadCount(chapterId: String): Int {
        val chapter = TaxData.content.chapters.find { it.id == chapterId } ?: return 0
        return chapter.articles.indices.count { isRead(chapterId, it) }
    }

    fun totalArticles(): Int = TaxData.content.chapters.sumOf { it.articles.size }

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

    fun studyDays(): Int = prefs.getStringSet(KEY_DAYS, emptySet())?.size ?: 0

    fun lastReadRef(): ArticleRef? {
        val stableId = prefs.getString(KEY_LAST_ARTICLE_ID, null)
        if (!stableId.isNullOrBlank()) {
            val stableRef = findByArticleId(stableId)
            if (stableRef != null) return stableRef
        }
        val chapterId = prefs.getString(KEY_LAST_CHAPTER, null) ?: return null
        val index = prefs.getInt(KEY_LAST_INDEX, -1)
        val ref = ArticleRef(chapterId, index)
        return ref.takeIf { it.isValid() }
    }

    fun smartSuggestion(): Chapter? {
        val chapters = TaxData.content.chapters
        val nextFromLast = lastReadRef()?.let { last ->
            val i = chapters.indexOfFirst { it.id == last.chapterId }
            chapters.getOrNull(i + 1)?.takeIf { chapterProgress(it.id) < 1f }
        }
        val candidates = (listOfNotNull(nextFromLast) + chapters).filter { chapterProgress(it.id) < 1f }
        return candidates.minByOrNull { chapterProgress(it.id) }
    }

    private fun articleKey(chapterId: String, index: Int): String {
        val article = TaxData.content.chapters.find { it.id == chapterId }?.articles?.getOrNull(index)
        return article?.id?.takeIf { it.isNotBlank() }?.let { "$STABLE_PREFIX$it" }
            ?: legacyKey(chapterId, index)
    }

    private fun migrateLegacyState() {
        val chapters = TaxData.content.chapters
        fun migrate(set: Set<String>): Set<String> = set.mapNotNull { old ->
            val parts = old.split(":")
            if (parts.size != 2) return@mapNotNull old.takeIf { it.startsWith(STABLE_PREFIX) }
            val chapter = chapters.find { it.id == parts[0] } ?: return@mapNotNull old
            val index = parts[1].toIntOrNull() ?: return@mapNotNull old
            val article = chapter.articles.getOrNull(index) ?: return@mapNotNull old
            article.id?.takeIf { it.isNotBlank() }?.let { "$STABLE_PREFIX$it" } ?: old
        }.toSet()

        val favorites = migrate(favSet())
        val reads = migrate(readSet())
        val lastChapter = prefs.getString(KEY_LAST_CHAPTER, null)
        val lastIndex = prefs.getInt(KEY_LAST_INDEX, -1)
        val lastArticleId = lastChapter?.let { chapterId ->
            chapters.find { it.id == chapterId }?.articles?.getOrNull(lastIndex)?.id
        }
        prefs.edit {
            putStringSet(KEY_FAVORITES, favorites)
            putStringSet(KEY_READ, reads)
            if (!lastArticleId.isNullOrBlank()) putString(KEY_LAST_ARTICLE_ID, lastArticleId)
            putBoolean(KEY_MIGRATION_DONE, true)
        }
    }

    /** Reconcile positional legacy keys after a remote document with stable IDs is applied. */
    fun reconcileStableIdentifiers() {
        val chapters = TaxData.content.chapters
        fun reconcile(set: Set<String>): Set<String> = set.mapNotNull { key ->
            if (key.startsWith(STABLE_PREFIX)) return@mapNotNull key
            val parts = key.split(":")
            if (parts.size != 2) return@mapNotNull key
            val chapter = chapters.find { it.id == parts[0] } ?: return@mapNotNull key
            val index = parts[1].toIntOrNull() ?: return@mapNotNull key
            val article = chapter.articles.getOrNull(index) ?: return@mapNotNull key
            article.id?.takeIf { it.isNotBlank() }?.let { "$STABLE_PREFIX$it" } ?: key
        }.toSet()
        prefs.edit {
            putStringSet(KEY_FAVORITES, reconcile(favSet()))
            putStringSet(KEY_READ, reconcile(readSet()))
            val chapterId = prefs.getString(KEY_LAST_CHAPTER, null)
            val index = prefs.getInt(KEY_LAST_INDEX, -1)
            val article = chapterId?.let { ch -> chapters.find { it.id == ch }?.articles?.getOrNull(index) }
            if (!article?.id.isNullOrBlank()) putString(KEY_LAST_ARTICLE_ID, article?.id)
        }
    }

    private fun resolveKey(key: String): ArticleRef? {
        if (key.startsWith(STABLE_PREFIX)) return findByArticleId(key.removePrefix(STABLE_PREFIX))
        val parts = key.split(":")
        if (parts.size != 2) return null
        return ArticleRef(parts[0], parts[1].toIntOrNull() ?: return null)
    }

    private fun findByArticleId(id: String): ArticleRef? {
        TaxData.content.chapters.forEach { chapter ->
            val index = chapter.articles.indexOfFirst { it.id == id }
            if (index >= 0) return ArticleRef(chapter.id, index)
        }
        return null
    }

    private fun legacyKey(chapterId: String, index: Int) = "$chapterId:$index"
    private fun favSet(): Set<String> = prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet() ?: emptySet()
    private fun readSet(): Set<String> = prefs.getStringSet(KEY_READ, emptySet())?.toSet() ?: emptySet()

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
        private const val KEY_LAST_ARTICLE_ID = "last_article_id"
        private const val KEY_MIGRATION_DONE = "stable_article_ids_migrated_v1"
        private const val STABLE_PREFIX = "article:"

        @Volatile private var instance: AppRepository? = null

        fun get(context: Context): AppRepository =
            instance ?: synchronized(this) {
                instance ?: AppRepository(context.applicationContext).also { instance = it }
            }
    }
}
