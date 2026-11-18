package tax.app.mehdighobadi.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tax.app.mehdighobadi.data.cache.ArticleCache
import tax.app.mehdighobadi.data.remote.ApiClient
import tax.app.mehdighobadi.data.remote.ArticleDto
import java.io.IOException

enum class ArticleSource { REMOTE, CACHE, EMPTY }

data class ArticleLoadResult(
    val articles: List<ArticleDto>,
    val source: ArticleSource,
    val message: String? = null
)

/**
 * Offline-first repository for server-managed articles.
 * It does not replace TaxData; UI migration can happen screen by screen.
 */
class ArticleRepository(context: Context) {
    private val cache = ArticleCache(context.applicationContext)

    suspend fun loadArticles(): ArticleLoadResult = withContext(Dispatchers.IO) {
        try {
            val allArticles = mutableListOf<ArticleDto>()
            var page = 1
            var lastPage: Int? = null

            do {
                val response = ApiClient.service.getArticles(page = page, perPage = 100)
                if (!response.isSuccessful) {
                    return@withContext cachedOrEmpty("دریافت محتوا از سرور موفق نبود.")
                }
                val payload = response.body()?.data
                    ?: return@withContext cachedOrEmpty("پاسخ محتوای سرور خالی است.")
                val items = payload.items
                if (items.isEmpty()) break
                allArticles += items
                lastPage = payload.lastPage
                page++
            } while (lastPage != null && page <= lastPage && page <= MAX_PAGES)

            if (allArticles.isNotEmpty()) cache.upsertAll(allArticles)
            val cached = cache.getAll()
            ArticleLoadResult(
                articles = if (cached.isNotEmpty()) cached else allArticles,
                source = ArticleSource.REMOTE
            )
        } catch (_: IOException) {
            cachedOrEmpty("اتصال برقرار نیست؛ محتوای ذخیره‌شده نمایش داده می‌شود.")
        } catch (_: Exception) {
            cachedOrEmpty("دریافت آنلاین ممکن نشد؛ محتوای ذخیره‌شده نمایش داده می‌شود.")
        }
    }

    suspend fun getCachedArticle(id: String): ArticleDto? =
        withContext(Dispatchers.IO) { cache.getById(id) }

    private fun cachedOrEmpty(message: String): ArticleLoadResult {
        val cached = runCatching { cache.getAll() }.getOrDefault(emptyList())
        return ArticleLoadResult(
            articles = cached,
            source = if (cached.isEmpty()) ArticleSource.EMPTY else ArticleSource.CACHE,
            message = message
        )
    }

    companion object {
        private const val MAX_PAGES = 20
    }
}
