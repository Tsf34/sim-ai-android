package com.fabrice.simai.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stockage des paramètres chiffrés localement.
 *
 * La clé API OpenCode est stockée dans EncryptedSharedPreferences (Tink) :
 * elle ne transite jamais en clair et ne peut pas être lue par une application
 * malveillante sans le verrouillage du périphérique.
 */
class SecureStorage(context: Context) {

    companion object {
        const val KEY_API_KEY = "sim_ai_api_key"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    @Suppress("DEPRECATION")
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "simai_secure_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getApiKey(): String = prefs.getString(KEY_API_KEY, "").orEmpty()

    fun saveApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key.trim()).apply()
    }

    fun deleteApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
    }

    fun hasApiKey(): Boolean = prefs.contains(KEY_API_KEY)
}
