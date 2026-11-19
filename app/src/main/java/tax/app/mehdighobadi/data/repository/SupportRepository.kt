package tax.app.mehdighobadi.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import tax.app.mehdighobadi.data.remote.ApiClient
import tax.app.mehdighobadi.data.remote.ConversationDto
import tax.app.mehdighobadi.data.remote.CreateConversationRequest
import tax.app.mehdighobadi.data.remote.MessageDto
import tax.app.mehdighobadi.data.remote.SendMessageRequest
import java.io.IOException

sealed interface SupportResult<out T> {
    data class Success<T>(val value: T) : SupportResult<T>
    data class Failure(val message: String) : SupportResult<Nothing>
}

class SupportRepository {
    suspend fun conversations(): SupportResult<List<ConversationDto>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getConversations()
            if (!response.isSuccessful) return@withContext SupportResult.Failure(httpMessage(response.code()))
            val data = response.body()?.data ?: return@withContext SupportResult.Failure("پاسخ فهرست گفت‌وگوها معتبر نیست.")
            SupportResult.Success(data)
        } catch (_: IOException) { SupportResult.Failure("اتصال به اینترنت برقرار نشد.") }
        catch (_: HttpException) { SupportResult.Failure("پاسخ سرور قابل پردازش نیست.") }
        catch (_: IllegalArgumentException) { SupportResult.Failure("نشانی امن API تنظیم نشده است.") }
        catch (_: Exception) { SupportResult.Failure("دریافت گفت‌وگوها با خطای غیرمنتظره روبه‌رو شد.") }
    }

    suspend fun messages(id: String): SupportResult<List<MessageDto>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getMessages(id)
            if (!response.isSuccessful) return@withContext SupportResult.Failure(httpMessage(response.code()))
            val data = response.body()?.data?.items ?: return@withContext SupportResult.Failure("پاسخ پیام‌ها معتبر نیست.")
            SupportResult.Success(data)
        } catch (_: IOException) { SupportResult.Failure("اتصال به اینترنت برقرار نشد.") }
        catch (_: Exception) { SupportResult.Failure("دریافت پیام‌ها انجام نشد.") }
    }

    suspend fun create(subject: String, body: String): SupportResult<ConversationDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.createConversation(CreateConversationRequest(subject.trim(), body.trim()))
            if (!response.isSuccessful) return@withContext SupportResult.Failure(httpMessage(response.code()))
            val data = response.body()?.data ?: return@withContext SupportResult.Failure("پاسخ ایجاد گفت‌وگو معتبر نیست.")
            SupportResult.Success(data)
        } catch (_: IOException) { SupportResult.Failure("اتصال به اینترنت برقرار نشد.") }
        catch (_: Exception) { SupportResult.Failure("ایجاد گفت‌وگو انجام نشد.") }
    }

    suspend fun send(id: String, body: String): SupportResult<MessageDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.sendMessage(id, SendMessageRequest(body.trim()))
            if (!response.isSuccessful) return@withContext SupportResult.Failure(httpMessage(response.code()))
            val data = response.body()?.data ?: return@withContext SupportResult.Failure("پاسخ ارسال پیام معتبر نیست.")
            SupportResult.Success(data)
        } catch (_: IOException) { SupportResult.Failure("اتصال به اینترنت برقرار نشد.") }
        catch (_: Exception) { SupportResult.Failure("ارسال پیام انجام نشد.") }
    }

    private fun httpMessage(code: Int): String = when (code) {
        401 -> "نشست کاربری معتبر نیست. دوباره وارد حساب شوید."
        403 -> "اجازه دسترسی به این گفت‌وگو را ندارید."
        404 -> "گفت‌وگو یا پیام موردنظر پیدا نشد."
        422 -> "اطلاعات ارسالی معتبر نیست."
        429 -> "درخواست‌ها بیش از حد مجاز است. کمی بعد تلاش کنید."
        in 500..599 -> "سرور موقتاً در دسترس نیست."
        else -> "ارتباط با سرور ناموفق بود. کد خطا: " + code
    }
}
