package tax.app.mehdighobadi.data.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores the Sanctum bearer token encrypted with Android Keystore-backed keys.
 * The encrypted preferences file is excluded from cloud/device backups because
 * the Keystore key is device-bound and cannot be restored on another device.
 */
class SessionStore private constructor(context: Context) {
    private val preferences = run {
        val appContext = context.applicationContext
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun readToken(): String? =
        preferences.getString(KEY_TOKEN, null)?.takeIf { it.isNotBlank() }

    fun saveToken(token: String) {
        require(token.isNotBlank()) { "Authentication token cannot be blank." }
        preferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "irtax_secure_session"
        private const val KEY_TOKEN = "sanctum_bearer_token"

        @Volatile
        private var instance: SessionStore? = null

        fun get(context: Context): SessionStore =
            instance ?: synchronized(this) {
                instance ?: SessionStore(context.applicationContext).also { instance = it }
            }
    }
}
