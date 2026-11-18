package tax.app.mehdighobadi.data.repository

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tax.app.mehdighobadi.data.Chapter
import tax.app.mehdighobadi.data.Note
import tax.app.mehdighobadi.data.Section
import tax.app.mehdighobadi.data.TaxContent
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.data.cache.ArticleCache
import tax.app.mehdighobadi.data.remote.ApiClient
import tax.app.mehdighobadi.data.remote.LegalDocumentDto
import java.io.IOException

enum class LegalContentSource { REMOTE, CACHE, LOCAL }
data class LegalContentResult(
    val content: TaxContent,
    val source: LegalContentSource,
    val documentId: String? = null,
    val version: String? = null,
    val message: String? = null
)

/**
 * Loads a complete, reviewed legal-document tree. The existing local document
 * remains the final fallback. Remote chapter clientKey values must match
 * existing local chapter IDs so bookmarks and reading progress remain stable.
 */
class LegalDocumentRepository(context: Context) {
    private val cache = ArticleCache(context.applicationContext)
    private val gson = Gson()

    suspend fun loadActiveDocument(): LegalContentResult = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getActiveLegalDocument()
            if (!response.isSuccessful) return@withContext cachedOrLocal("دریافت نسخه منتشرشده قانون ممکن نشد.")
            val document = response.body()?.data
                ?: return@withContext cachedOrLocal("پاسخ سند قانونی سرور خالی است.")
            val mapped = mapDocument(document)
                ?: return@withContext cachedOrLocal("ساختار سند قانونی سرور کامل یا معتبر نیست.")
            cache.saveLegalDocument(document.id, document.version, gson.toJson(document))
            LegalContentResult(mapped, LegalContentSource.REMOTE, document.id, document.version)
        } catch (_: IOException) {
            cachedOrLocal("اینترنت در دسترس نیست؛ آخرین نسخه ذخیره‌شده نمایش داده می‌شود.")
        } catch (_: IllegalArgumentException) {
            cachedOrLocal("نشانی HTTPS سرور تنظیم نشده است.")
        } catch (_: Exception) {
            cachedOrLocal("دریافت آنلاین قانون ممکن نشد؛ نسخه ذخیره‌شده بررسی می‌شود.")
        }
    }

    private fun cachedOrLocal(message: String): LegalContentResult {
        val json = runCatching { cache.getLatestLegalDocument() }.getOrNull()
        if (!json.isNullOrBlank()) {
            val cached = runCatching { gson.fromJson(json, LegalDocumentDto::class.java) }.getOrNull()
            val mapped = cached?.let { mapDocument(it) }
            if (mapped != null) return LegalContentResult(
                mapped, LegalContentSource.CACHE, cached.id, cached.version, message
            )
        }
        return LegalContentResult(TaxData.content, LegalContentSource.LOCAL, message = message)
    }

    private fun mapDocument(document: LegalDocumentDto): TaxContent? {
        if (document.id.isBlank() || document.chapters.isEmpty()) return null
        val keys = document.chapters.map { it.clientKey }
        if (keys.any { it.isBlank() } || keys.distinct().size != keys.size) return null
        if (document.chapters.map { it.sortOrder }.distinct().size != document.chapters.size) return null
        val allArticleIds = document.chapters.flatMap { it.articles }.map { it.id }
        if (allArticleIds.any { it.isBlank() } || allArticleIds.distinct().size != allArticleIds.size) return null
        val allNestedIds = document.chapters.flatMap { chapter ->
            chapter.articles.flatMap { article ->
                article.sections.map { it.id } + article.notes.map { it.id }
            }
        }
        if (allNestedIds.any { it.isBlank() } || allNestedIds.distinct().size != allNestedIds.size) return null

        val orderedRemoteChapters = document.chapters.sortedBy { it.sortOrder }
        val bundledChapters = TaxData.content.chapters.associateBy { it.id }

        // AppRepository currently stores bookmarks and reading positions as
        // chapterKey + articleIndex. Until those preferences are migrated to
        // stable article IDs, every bundled chapter/article must remain at the
        // same position in the remote document. New chapters/articles may be
        // appended, but existing entries may not be removed, inserted before,
        // or reordered.
        val remoteByKey = orderedRemoteChapters.associateBy { it.clientKey }
        for ((chapterKey, bundledChapter) in bundledChapters) {
            val remoteChapter = remoteByKey[chapterKey] ?: return null
            val remoteArticles = remoteChapter.articles.sortedBy { it.sortOrder }
            if (remoteArticles.size < bundledChapter.articles.size) return null
            val existingOrderMatches = bundledChapter.articles.indices.all { index ->
                remoteArticles[index].number == bundledChapter.articles[index].number
            }
            if (!existingOrderMatches) return null
        }

        val chapters = orderedRemoteChapters.map { chapter ->
            if (chapter.title.isBlank()) return null
            val orderedArticles = chapter.articles.sortedBy { it.sortOrder }
            if (orderedArticles.any { it.number.isBlank() || it.content.isBlank() }) return null
            if (orderedArticles.map { it.sortOrder }.distinct().size != orderedArticles.size) return null
            Chapter(
                id = chapter.clientKey,
                title = chapter.title,
                articles = orderedArticles.map { article ->
                    tax.app.mehdighobadi.data.Article(
                        number = article.number,
                        content = article.content,
                        sections = article.sections.sortedBy { it.sortOrder }.map {
                            Section(title = it.title, content = it.content, id = it.id)
                        },
                        notes = article.notes.sortedBy { it.sortOrder }.map {
                            Note(label = it.label, content = it.content, id = it.id)
                        },
                        id = article.id
                    )
                }
            )
        }
        if (chapters.any { it.articles.isEmpty() }) return null
        return TaxContent(chapters = chapters)
    }
}
