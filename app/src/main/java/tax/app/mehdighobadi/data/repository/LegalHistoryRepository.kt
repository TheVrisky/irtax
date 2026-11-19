package tax.app.mehdighobadi.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tax.app.mehdighobadi.data.LegalAmendment
import tax.app.mehdighobadi.data.LegalDocumentVersion
import tax.app.mehdighobadi.data.LegalVersionStatus
import tax.app.mehdighobadi.data.remote.ApiClient
import tax.app.mehdighobadi.data.remote.LegalAmendmentDto
import tax.app.mehdighobadi.data.remote.LegalDocumentVersionDto
import java.io.IOException

sealed interface LegalHistoryResult<out T> {
    data class Success<T>(val items: List<T>) : LegalHistoryResult<T>
    data class Failure(val message: String) : LegalHistoryResult<Nothing>
}

/**
 * Read-only access to immutable legal publication history and amendment records.
 * This repository never changes the active document or local reading state.
 */
class LegalHistoryRepository {

    suspend fun loadVersions(documentId: String): LegalHistoryResult<LegalDocumentVersion> =
        withContext(Dispatchers.IO) {
            if (documentId.isBlank()) {
                return@withContext LegalHistoryResult.Failure("شناسه سند قانونی معتبر نیست.")
            }
            try {
                val response = ApiClient.service.getLegalDocumentVersions(documentId)
                if (!response.isSuccessful) {
                    return@withContext LegalHistoryResult.Failure(messageFor(response.code()))
                }
                val items = response.body()?.data
                    ?: return@withContext LegalHistoryResult.Failure("فهرست نسخه‌های قانون خالی است.")
                LegalHistoryResult.Success(items.map { it.toDomain() })
            } catch (_: IOException) {
                LegalHistoryResult.Failure("برای دریافت تاریخچه نسخه‌ها اتصال اینترنت برقرار نیست.")
            } catch (_: IllegalArgumentException) {
                LegalHistoryResult.Failure("نشانی HTTPS سرور برای این نسخه تنظیم نشده است.")
            } catch (_: Exception) {
                LegalHistoryResult.Failure("دریافت تاریخچه نسخه‌های قانون ممکن نشد.")
            }
        }

    suspend fun loadAmendments(documentId: String): LegalHistoryResult<LegalAmendment> =
        withContext(Dispatchers.IO) {
            if (documentId.isBlank()) {
                return@withContext LegalHistoryResult.Failure("شناسه سند قانونی معتبر نیست.")
            }
            try {
                val response = ApiClient.service.getLegalAmendments(documentId)
                if (!response.isSuccessful) {
                    return@withContext LegalHistoryResult.Failure(messageFor(response.code()))
                }
                val items = response.body()?.data
                    ?: return@withContext LegalHistoryResult.Failure("فهرست اصلاحیه‌های قانون خالی است.")
                LegalHistoryResult.Success(items.map { it.toDomain() })
            } catch (_: IOException) {
                LegalHistoryResult.Failure("برای دریافت اصلاحیه‌ها اتصال اینترنت برقرار نیست.")
            } catch (_: IllegalArgumentException) {
                LegalHistoryResult.Failure("نشانی HTTPS سرور برای این نسخه تنظیم نشده است.")
            } catch (_: Exception) {
                LegalHistoryResult.Failure("دریافت اصلاحیه‌های قانون ممکن نشد.")
            }
        }

    suspend fun loadArticleHistory(articleId: String): LegalHistoryResult<LegalAmendment> =
        withContext(Dispatchers.IO) {
            if (articleId.isBlank()) {
                return@withContext LegalHistoryResult.Failure("شناسه ماده قانونی معتبر نیست.")
            }
            try {
                val response = ApiClient.service.getArticleHistory(articleId)
                if (!response.isSuccessful) {
                    return@withContext LegalHistoryResult.Failure(messageFor(response.code()))
                }
                val items = response.body()?.data
                    ?: return@withContext LegalHistoryResult.Failure("تاریخچه این ماده خالی است.")
                LegalHistoryResult.Success(items.map { it.toDomain() })
            } catch (_: IOException) {
                LegalHistoryResult.Failure("برای دریافت تاریخچه ماده اتصال اینترنت برقرار نیست.")
            } catch (_: IllegalArgumentException) {
                LegalHistoryResult.Failure("نشانی HTTPS سرور برای این نسخه تنظیم نشده است.")
            } catch (_: Exception) {
                LegalHistoryResult.Failure("دریافت تاریخچه ماده ممکن نشد.")
            }
        }

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
