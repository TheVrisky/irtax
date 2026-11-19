package tax.app.mehdighobadi.data.repository

import android.content.Context
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tax.app.mehdighobadi.data.LegalAmendment
import tax.app.mehdighobadi.data.LegalDocumentVersion
import tax.app.mehdighobadi.data.LegalVersionStatus
import tax.app.mehdighobadi.data.cache.LegalHistoryCache
import tax.app.mehdighobadi.data.remote.ApiClient
import tax.app.mehdighobadi.data.remote.ApiEnvelope
import tax.app.mehdighobadi.data.remote.LegalAmendmentDto
import tax.app.mehdighobadi.data.remote.LegalDocumentVersionDto
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type

sealed interface LegalHistoryResult<out T> {
    data class Success<T>(val items: List<T>, val fromCache: Boolean = false) : LegalHistoryResult<T>
    data class Failure(val message: String) : LegalHistoryResult<Nothing>
}

/**
 * Read-only access to immutable legal publication history and amendment records.
 * Successful responses are persisted locally and reused when the API is unavailable.
 */
class LegalHistoryRepository(context: Context) {
    private val cache = LegalHistoryCache(context)

    suspend fun loadVersions(documentId: String): LegalHistoryResult<LegalDocumentVersion> =
        withContext(Dispatchers.IO) {
            if (documentId.isBlank()) return@withContext invalidId()
            fetchOrCached(
                key = LegalHistoryCache.versionsKey(documentId),
                offlineMessage = "برای دریافت تاریخچه نسخه‌ها اتصال اینترنت برقرار نیست.",
                domainClass = LegalDocumentVersion::class.java,
                request = { ApiClient.service.getLegalDocumentVersions(documentId) },
                map = { rows -> rows.map { it.toDomain() } }
            )
        }

    suspend fun loadAmendments(documentId: String): LegalHistoryResult<LegalAmendment> =
        withContext(Dispatchers.IO) {
            if (documentId.isBlank()) return@withContext invalidId()
            fetchOrCached(
                key = LegalHistoryCache.amendmentsKey(documentId),
                offlineMessage = "برای دریافت اصلاحیه‌ها اتصال اینترنت برقرار نیست.",
                domainClass = LegalAmendment::class.java,
                request = { ApiClient.service.getLegalAmendments(documentId) },
                map = { rows -> rows.map { it.toDomain() } }
            )
        }

    suspend fun loadArticleHistory(articleId: String): LegalHistoryResult<LegalAmendment> =
        withContext(Dispatchers.IO) {
            if (articleId.isBlank()) return@withContext invalidId()
            fetchOrCached(
                key = LegalHistoryCache.articleHistoryKey(articleId),
                offlineMessage = "برای دریافت تاریخچه ماده اتصال اینترنت برقرار نیست.",
                domainClass = LegalAmendment::class.java,
                request = { ApiClient.service.getArticleHistory(articleId) },
                map = { rows -> rows.map { it.toDomain() } }
            )
        }

    private suspend fun <Dto, Domain : Any> fetchOrCached(
        key: String,
        offlineMessage: String,
        domainClass: Class<Domain>,
        request: suspend () -> Response<ApiEnvelope<List<Dto>>>,
        map: (List<Dto>) -> List<Domain>
    ): LegalHistoryResult<Domain> {
        fun cached(): List<Domain>? {
            val type: Type = TypeToken.getParameterized(List::class.java, domainClass).type
            return cache.load(key, type)
        }

        try {
            val response = request()
            if (response.isSuccessful) {
                val dtoItems = response.body()?.data
                if (dtoItems != null) {
                    val items = map(dtoItems)
                    cache.save(key, items)
                    return LegalHistoryResult.Success(items)
                }
            }
            cached()?.let { return LegalHistoryResult.Success(it, fromCache = true) }
            return LegalHistoryResult.Failure(messageFor(response.code()))
        } catch (_: IOException) {
            cached()?.let { return LegalHistoryResult.Success(it, fromCache = true) }
            return LegalHistoryResult.Failure(offlineMessage)
        } catch (_: IllegalArgumentException) {
            cached()?.let { return LegalHistoryResult.Success(it, fromCache = true) }
            return LegalHistoryResult.Failure("نشانی HTTPS سرور برای این نسخه تنظیم نشده است.")
        } catch (_: Exception) {
            cached()?.let { return LegalHistoryResult.Success(it, fromCache = true) }
            return LegalHistoryResult.Failure("دریافت تاریخچه از سرور یا حافظه محلی ممکن نشد.")
        }
    }

    private fun invalidId() =
        LegalHistoryResult.Failure("شناسه سند یا ماده قانونی معتبر نیست.")

    private fun messageFor(code: Int): String = when (code) {
        401 -> "برای مشاهده تاریخچه باید وارد حساب کاربری شوی."
        403 -> "اجازه مشاهده این تاریخچه را نداری."
        404 -> "سند یا سابقه درخواستی پیدا نشد."
        429 -> "درخواست‌ها بیش از حد مجاز است؛ کمی بعد تلاش کن."
        in 500..599 -> "سرور موقتاً در دسترس نیست."
        else -> "دریافت تاریخچه ناموفق بود. کد: $code"
    }

    private fun LegalDocumentVersionDto.toDomain() = LegalDocumentVersion(
        documentId = documentId,
        versionId = id,
        versionNumber = versionNumber,
        publicationDate = publicationDate,
        effectiveDate = effectiveDate,
        expirationDate = expirationDate,
        amendmentDate = amendmentDate,
        checksum = checksum,
        status = LegalVersionStatus.fromApi(status)
    )

    private fun LegalAmendmentDto.toDomain() = LegalAmendment(
        id = id,
        documentId = documentId,
        articleId = articleId,
        amendmentType = amendmentType,
        previousText = previousText,
        newText = newText,
        publicationDate = publicationDate,
        effectiveDate = effectiveDate,
        sourceReference = sourceReference
    )
}
