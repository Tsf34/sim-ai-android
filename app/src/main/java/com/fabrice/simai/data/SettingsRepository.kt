package com.fabrice.simai.data

import android.content.Context
import android.content.SharedPreferences
import com.fabrice.simai.util.SecureStorage

/**
 * Repository des paramètres globaux (url du serveur, modèle sélectionné, clé API).
 * La clé API est stockée via [SecureStorage] (chiffrée).
 */
class SettingsRepository(context: Context) {

    private val secure = SecureStorage(context)
    private val plain: SharedPreferences =
        context.getSharedPreferences("simai_settings", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_SERVER_URL = "http://localhost:4096"
        const val KEY_SERVER_URL = "sim_ai_server_url"
        const val KEY_MODEL_ID = "sim_ai_model_id"
    }

    fun getServerUrl(): String = plain.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL).orEmpty()

    fun setServerUrl(url: String) {
        plain.edit().putString(KEY_SERVER_URL, url.trim().trimEnd('/')).apply()
    }

    fun getApiKey(): String = secure.getApiKey()

    fun saveApiKey(key: String) = secure.saveApiKey(key)

    fun deleteApiKey() = secure.deleteApiKey()

    fun hasApiKey(): Boolean = secure.hasApiKey()

    fun getModelId(): String = plain.getString(KEY_MODEL_ID, "").orEmpty()

    fun setModelId(modelId: String) {
        plain.edit().putString(KEY_MODEL_ID, modelId.trim()).apply()
    }
}
