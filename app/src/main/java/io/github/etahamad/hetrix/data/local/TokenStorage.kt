package io.github.etahamad.hetrix.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Interface for securely storing and retrieving HetrixTools API credentials.
 */
interface TokenStorage {
    val tokenFlow: StateFlow<String?>

    fun getToken(): String?

    fun hasToken(): Boolean

    suspend fun saveToken(token: String)

    suspend fun clearToken()
}

/**
 * Production implementation using AndroidX Security [EncryptedSharedPreferences]
 * with AES-256 GCM encryption.
 */
class EncryptedTokenStorage(context: Context) : TokenStorage {

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to standard private preferences if Keystore hardware is corrupted or unsupported
            context.getSharedPreferences(PREFS_FALLBACK_FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    private val _tokenFlow = MutableStateFlow<String?>(null)
    override val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

    init {
        _tokenFlow.value = getToken()
    }

    override fun getToken(): String? {
        return sharedPreferences.getString(KEY_API_TOKEN, null)?.takeIf { it.isNotBlank() }
    }

    override fun hasToken(): Boolean {
        return !getToken().isNullOrBlank()
    }

    override suspend fun saveToken(token: String) {
        val cleanToken = token.trim()
        sharedPreferences.edit()
            .putString(KEY_API_TOKEN, cleanToken)
            .apply()
        _tokenFlow.value = cleanToken
    }

    override suspend fun clearToken() {
        sharedPreferences.edit()
            .remove(KEY_API_TOKEN)
            .apply()
        _tokenFlow.value = null
    }

    companion object {
        private const val PREFS_FILE_NAME = "hetrix_secure_prefs"
        private const val PREFS_FALLBACK_FILE_NAME = "hetrix_unencrypted_prefs"
        private const val KEY_API_TOKEN = "hetrix_api_bearer_token"
    }
}
