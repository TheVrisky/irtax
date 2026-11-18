package tax.app.mehdighobadi.data.auth

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import tax.app.mehdighobadi.data.remote.ApiClient
import tax.app.mehdighobadi.data.remote.LoginRequest
import tax.app.mehdighobadi.data.remote.UserDto
import java.io.IOException

sealed interface AuthResult {
    data class Success(val user: UserDto?) : AuthResult
    data class Failure(val message: String) : AuthResult
}

/**
 * Authentication orchestration. UI must call this from a coroutine and render
 * AuthResult; raw passwords and tokens must never be logged.
 */
class AuthRepository(context: Context) {
    private val sessionStore = SessionStore.get(context.applicationContext)

    suspend fun login(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            if (email.isBlank() || password.isBlank()) {
                return@withContext AuthResult.Failure("ایمیل و گذرواژه را وارد کنید.")
            }

            try {
                val response = ApiClient.service.login(LoginRequest(email.trim(), password))
                if (!response.isSuccessful) {
                    return@withContext AuthResult.Failure(
                        when (response.code()) {
                            401, 422 -> "اطلاعات ورود معتبر نیست."
                            429 -> "تعداد درخواست‌ها زیاد است. کمی بعد دوباره تلاش کنید."
                            in 500..599 -> "سرور موقتاً در دسترس نیست."
                            else -> "ورود انجام نشد. کد خطا: ${response.code()}"
                        }
                    )
                }

                val payload = response.body()?.data
                val token = payload?.token?.takeIf { it.isNotBlank() }
                    ?: return@withContext AuthResult.Failure("پاسخ ورود سرور معتبر نیست.")

                sessionStore.saveToken(token)
                ApiClient.setBearerToken(token)
                AuthResult.Success(payload.user)
            } catch (_: IOException) {
                AuthResult.Failure("اتصال به اینترنت برقرار نشد.")
            } catch (_: HttpException) {
                AuthResult.Failure("پاسخ سرور قابل پردازش نیست.")
            } catch (_: IllegalStateException) {
                AuthResult.Failure("نشانی امن API تنظیم نشده است.")
            } catch (_: Exception) {
                AuthResult.Failure("خطای غیرمنتظره هنگام ورود رخ داد.")
            }
        }

    suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        val hadSession = sessionStore.readToken() != null
        return@withContext try {
            if (hadSession) runCatching { ApiClient.service.logout() }
            true
        } finally {
            sessionStore.clear()
            ApiClient.clearSession()
        }
    }

    fun isSignedIn(): Boolean = sessionStore.readToken() != null
}
