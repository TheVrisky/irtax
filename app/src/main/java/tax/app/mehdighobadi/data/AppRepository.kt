package tax.app.mehdighobadi.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/** Persistent study state keyed by stable article identities. */
class AppRepository private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    init { migrateLegacyState() }

    fun isFavorite(chapterId: String, articleIndex: Int) = favSet().contains(articleKey(chapterId, articleIndex))
    fun toggleFavorite(chapterId: String, articleIndex: Int): Boolean {
        val set = favSet().toMutableSet()
        val key = articleKey(chapterId, articleIndex)
        val added = if (set.contains(key)) { set.remove(key); false } else { set.add(key); true }
        prefs.edit { putStringSet(KEY_FAVORITES, set) }
        return added
    }
    fun favoriteRefs() = favSet().mapNotNull(::resolveKey).filter { it.isValid() }.sortedBy { it.flatIndex() }
    fun isRead(chapterId: String, articleIndex: Int) = readSet().contains(articleKey(chapterId, articleIndex))

    fun markRead(chapterId: String, articleIndex: Int) {
        val set = readSet().toMutableSet()
        set.add(articleKey(chapterId, articleIndex))
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val days = prefs.getStringSet(KEY_DAYS, emptySet())?.toMutableSet() ?: mutableSetOf()
        days.add(today)
        val article = TaxData.content.chapters.find { it.id == chapterId }?.articles?.getOrNull(articleIndex)
        prefs.edit {
            putStringSet(KEY_READ, set)
            putStringSet(KEY_DAYS, days)
            putString(KEY_LAST_CHAPTER, chapterId)
            putInt(KEY_LAST_INDEX, articleIndex)
            putString(KEY_LAST_ARTICLE_ID, article?.let { stableId(chapterId, it) })
            putString(KEY_LAST_DOCUMENT_VERSION, TaxData.documentVersion)
        }
    }

    fun readCount() = readSet().count { resolveKey(it)?.isValid() == true }
    fun chapterReadCount(chapterId: String): Int {
        val chapter = TaxData.content.chapters.find { it.id == chapterId } ?: return 0
        return chapter.articles.indices.count { isRead(chapterId, it) }
    }
    fun totalArticles() = TaxData.content.chapters.sumOf { it.articles.size }
    fun overallProgress() = totalArticles().takeIf { it > 0 }?.let { readCount().toFloat() / it } ?: 0f
    fun chapterProgress(chapterId: String): Float {
        val chapter = TaxData.content.chapters.find { it.id == chapterId } ?: return 0f
        return chapter.articles.size.takeIf { it > 0 }?.let { chapterReadCount(chapterId).toFloat() / it } ?: 0f
    }
    fun studyDays() = prefs.getStringSet(KEY_DAYS, emptySet())?.size ?: 0
    fun lastReadDocumentVersion() = prefs.getString(KEY_LAST_DOCUMENT_VERSION, null)

    fun lastReadRef(): ArticleRef? {
        prefs.getString(KEY_LAST_ARTICLE_ID, null)?.takeIf { it.isNotBlank() }?.let { findByArticleId(it)?.let { ref -> return ref } }
        val chapterId = prefs.getString(KEY_LAST_CHAPTER, null) ?: return null
        return ArticleRef(chapterId, prefs.getInt(KEY_LAST_INDEX, -1)).takeIf { it.isValid() }
    }

    fun smartSuggestion(): Chapter? {
        val chapters = TaxData.content.chapters
        val next = lastReadRef()?.let { ref ->
            val i = chapters.indexOfFirst { it.id == ref.chapterId }
            chapters.getOrNull(i + 1)?.takeIf { chapterProgress(it.id) < 1f }
        }
        return (listOfNotNull(next) + chapters).filter { chapterProgress(it.id) < 1f }.minByOrNull { chapterProgress(it.id) }
    }

    private fun articleKey(chapterId: String, index: Int): String {
        val article = TaxData.content.chapters.find { it.id == chapterId }?.articles?.getOrNull(index)
            ?: return "$chapterId:$index"
        return "$STABLE_PREFIX${stableId(chapterId, article)}"
    }
    private fun stableId(chapterId: String, article: Article): String =
        article.id?.takeIf { it.isNotBlank() }
            ?: UUID.nameUUIDFromBytes("$DOCUMENT_ID:$chapterId:${article.number}".toByteArray(Charsets.UTF_8)).toString()

    private fun migrateLegacyState() {
        if (prefs.getBoolean(KEY_MIGRATION_DONE, false)) return
        val chapters = TaxData.content.chapters
        fun migrate(set: Set<String>) = set.map { old ->
            val parts = old.split(":")
            if (parts.size != 2 || old.startsWith(STABLE_PREFIX)) return@map old
            val chapter = chapters.find { it.id == parts[0] } ?: return@map old
            val index = parts[1].toIntOrNull() ?: return@map old
            val article = chapter.articles.getOrNull(index) ?: return@map old
            "$STABLE_PREFIX${stableId(chapter.id, article)}"
        }.toSet()
        val lastChapter = prefs.getString(KEY_LAST_CHAPTER, null)
        val lastIndex = prefs.getInt(KEY_LAST_INDEX, -1)
        val lastArticle = lastChapter?.let { ch -> chapters.find { it.id == ch }?.articles?.getOrNull(lastIndex) }
        prefs.edit {
            putStringSet(KEY_FAVORITES, migrate(favSet()))
            putStringSet(KEY_READ, migrate(readSet()))
            if (lastChapter != null && lastArticle != null) putString(KEY_LAST_ARTICLE_ID, stableId(lastChapter, lastArticle))
            putString(KEY_LAST_DOCUMENT_VERSION, TaxData.documentVersion)
            putBoolean(KEY_MIGRATION_DONE, true)
        }
    }

    /** Reconcile legacy positional refs after replacing the active legal document. */
    fun reconcileStableIdentifiers() {
        val chapters = TaxData.content.chapters
        val aliases = buildMap<String, String> {
            chapters.forEach { chapter ->
                chapter.articles.forEach { article ->
                    val bundledId = UUID.nameUUIDFromBytes("$DOCUMENT_ID:${chapter.id}:${article.number}".toByteArray(Charsets.UTF_8)).toString()
                    put(bundledId, stableId(chapter.id, article))
                }
            }
        }
        fun reconcile(set: Set<String>): Set<String> = set.map { key ->
            if (!key.startsWith(STABLE_PREFIX)) {
                val parts = key.split(":")
                if (parts.size == 2) {
                    val chapter = chapters.find { it.id == parts[0] }
                    val article = chapter?.articles?.getOrNull(parts[1].toIntOrNull() ?: -1)
                    if (chapter != null && article != null) return@map "$STABLE_PREFIX${stableId(chapter.id, article)}"
                }
                return@map key
            }
            val oldId = key.removePrefix(STABLE_PREFIX)
            "$STABLE_PREFIX${aliases[oldId] ?: oldId}"
        }.toSet()
        val chapterId = prefs.getString(KEY_LAST_CHAPTER, null)
        val index = prefs.getInt(KEY_LAST_INDEX, -1)
        val article = chapterId?.let { ch -> chapters.find { it.id == ch }?.articles?.getOrNull(index) }
        prefs.edit {
            putStringSet(KEY_FAVORITES, reconcile(favSet()))
            putStringSet(KEY_READ, reconcile(readSet()))
            if (chapterId != null && article != null) putString(KEY_LAST_ARTICLE_ID, stableId(chapterId, article))
            putString(KEY_LAST_DOCUMENT_VERSION, TaxData.documentVersion)
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
            val index = chapter.articles.indexOfFirst { stableId(chapter.id, it) == id }
            if (index >= 0) return ArticleRef(chapter.id, index)
        }
        return null
    }
    private fun favSet() = prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet() ?: emptySet()
    private fun readSet() = prefs.getStringSet(KEY_READ, emptySet())?.toSet() ?: emptySet()

    data class ArticleRef(val chapterId: String, val articleIndex: Int) {
        fun isValid(): Boolean = TaxData.content.chapters.find { it.id == chapterId }?.articles?.indices?.contains(articleIndex) == true
        fun chapter() = TaxData.content.chapters.find { it.id == chapterId }
        fun article() = chapter()?.articles?.getOrNull(articleIndex)
        fun flatIndex(): Int {
            var count = 0
            for (chapter in TaxData.content.chapters) {
                if (chapter.id == chapterId) return count + articleIndex
                count += chapter.articles.size
            }
            return count
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
        private const val KEY_LAST_DOCUMENT_VERSION = "last_document_version"
        private const val KEY_MIGRATION_DONE = "stable_article_ids_migrated_v2"
        private const val STABLE_PREFIX = "article:"
        private const val DOCUMENT_ID = "law-tax-invoice"
        @Volatile private var instance: AppRepository? = null
        fun get(context: Context): AppRepository = instance ?: synchronized(this) {
            instance ?: AppRepository(context.applicationContext).also { instance = it }
        }
    }
}
