package com.courseflow.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Persists the auth token encrypted (AES-256, master key in the Android Keystore).
 * If the Keystore is unavailable the token is kept in memory only: the user simply logs in again.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences? = try {
        val appContext = context.applicationContext
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            "session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        null
    }

    @Volatile private var memoryToken: String? = null

    fun saveToken(token: String) {
        memoryToken = token
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun getToken(): String? = memoryToken ?: prefs?.getString(KEY_TOKEN, null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()

    fun clear() {
        memoryToken = null
        prefs?.edit()?.clear()?.apply()
    }

    private companion object {
        const val KEY_TOKEN = "auth_token"
    }
}
