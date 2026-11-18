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
        val chapters = document.chapters.sortedBy { it.sortOrder }.map { chapter ->
            Chapter(
                id = chapter.clientKey,
                title = chapter.title,
                articles = chapter.articles.sortedBy { it.sortOrder }.map { article ->
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
