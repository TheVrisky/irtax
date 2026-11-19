package tax.app.mehdighobadi.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tax.app.mehdighobadi.data.remote.ApiClient
import tax.app.mehdighobadi.data.remote.ExamDto
import tax.app.mehdighobadi.data.remote.ExamResultDto
import tax.app.mehdighobadi.data.remote.ExamSubmissionRequest
import java.io.IOException

sealed interface ExamLoadResult {
    data class Success(val exams: List<ExamDto>) : ExamLoadResult
    data class Failure(val message: String) : ExamLoadResult
}

sealed interface ExamDetailResult {
    data class Success(val exam: ExamDto) : ExamDetailResult
    data class Failure(val message: String) : ExamDetailResult
}

sealed interface ExamSubmitResult {
    data class Success(val result: ExamResultDto) : ExamSubmitResult
    data class Failure(val message: String) : ExamSubmitResult
}

/** Server is authoritative for published exams and grading. */
class ExamRepository {
    suspend fun loadPublishedExams(): ExamLoadResult = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getExams(page = 1, perPage = 100)
            if (!response.isSuccessful) return@withContext ExamLoadResult.Failure(messageFor(response.code()))
            val items = response.body()?.data?.items
                ?: return@withContext ExamLoadResult.Failure("پاسخ فهرست آزمون‌ها معتبر نیست.")
            ExamLoadResult.Success(items)
        } catch (_: IOException) {
            ExamLoadResult.Failure("اتصال اینترنت برقرار نیست.")
        } catch (_: IllegalArgumentException) {
            ExamLoadResult.Failure("نشانی HTTPS سرور هنوز برای این نسخه تنظیم نشده است.")
        } catch (_: Exception) {
            ExamLoadResult.Failure("دریافت آزمون‌ها ممکن نشد.")
        }
    }

    suspend fun loadExam(id: String): ExamDetailResult = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getExam(id)
            if (!response.isSuccessful) return@withContext ExamDetailResult.Failure(messageFor(response.code()))
            val exam = response.body()?.data
                ?: return@withContext ExamDetailResult.Failure("جزئیات آزمون خالی است.")
            if (exam.questions.isEmpty() || exam.questions.any { it.options.size < 2 }) {
                return@withContext ExamDetailResult.Failure("ساختار سؤال‌های این آزمون کامل نیست.")
            }
            ExamDetailResult.Success(exam)
        } catch (_: IOException) {
            ExamDetailResult.Failure("اتصال اینترنت برقرار نیست.")
        } catch (_: IllegalArgumentException) {
            ExamDetailResult.Failure("نشانی HTTPS سرور تنظیم نشده است.")
        } catch (_: Exception) {
            ExamDetailResult.Failure("دریافت سؤال‌های آزمون ممکن نشد.")
        }
    }

    suspend fun submit(id: String, answers: Map<String, String>): ExamSubmitResult =
        withContext(Dispatchers.IO) {
            if (answers.isEmpty()) return@withContext ExamSubmitResult.Failure("هنوز پاسخی ثبت نشده است.")
            try {
                val response = ApiClient.service.submitExam(id, ExamSubmissionRequest(answers))
                if (!response.isSuccessful) return@withContext ExamSubmitResult.Failure(messageFor(response.code()))
                val result = response.body()?.data
                    ?: return@withContext ExamSubmitResult.Failure("نتیجه‌ای از سرور دریافت نشد.")
                ExamSubmitResult.Success(result)
            } catch (_: IOException) {
                ExamSubmitResult.Failure("ارسال پاسخ‌ها انجام نشد؛ اتصال اینترنت را بررسی کنید.")
            } catch (_: Exception) {
                ExamSubmitResult.Failure("ثبت پاسخ‌ها با خطا روبه‌رو شد.")
            }
        }

    private fun messageFor(code: Int): String = when (code) {
        401 -> "برای شرکت در آزمون باید وارد حساب کاربری شوی."
        403 -> "اجازه شرکت در این آزمون را نداری."
        404 -> "آزمون پیدا نشد یا دیگر منتشرشده نیست."
        422 -> "پاسخ‌های ارسالی معتبر نیستند."
        429 -> "درخواست‌ها بیش از حد مجاز است؛ کمی بعد تلاش کن."
        in 500..599 -> "سرور موقتاً در دسترس نیست."
        else -> "ارتباط با سرور ناموفق بود. کد: $code"
    }
}
