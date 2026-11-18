package tax.app.mehdighobadi.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tax.app.mehdighobadi.BuildConfig
import java.util.concurrent.TimeUnit

/**
 * Central API client. The base URL is supplied at build time through
 * -PIRTAX_API_BASE_URL=https://your-domain/
 *
 * No insecure HTTP fallback is provided.
 */
object ApiClient {
    @Volatile
    private var bearerToken: String? = null

    private val authInterceptor = Interceptor { chain ->
        val original: Request = chain.request()
        val request = original.newBuilder().apply {
            bearerToken?.takeIf { it.isNotBlank() }?.let {
                header("Authorization", "Bearer $it")
            }
            header("Accept", "application/json")
            header("X-Client", "irtax-android")
        }.build()
        chain.proceed(request)
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val service: ApiService by lazy {
        val baseUrl = BuildConfig.API_BASE_URL.trim()
        require(
            baseUrl.startsWith("https://") &&
                !baseUrl.contains("example.invalid") &&
                baseUrl.endsWith("/")
        ) {
            "IRTAX API URL is not configured. Set IRTAX_API_BASE_URL to the HTTPS API base URL."
        }

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /** Set after a successful login; pass null to sign out. */
    fun setBearerToken(token: String?) {
        bearerToken = token?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun clearSession() {
        bearerToken = null
    }
}
