package com.fabrice.simai.data.api

import com.fabrice.simai.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Client HTTP vers l'API OpenCode (schema documenté sur https://opencode.ai/docs/api).
 *
 * Endpoints utilisés :
 *  - GET  /api/model                                   -> modèles disponibles
 *  - POST /api/session                                 -> créer une session (conversation)
 *  - POST /api/session/{id}/prompt                     -> envoyer un message
 *  - GET  /api/session/{id}/message?order=asc          -> lire les messages
 *  - DELETE /api/session/{id}                          -> supprimer une session
 */
class OpenCodeApiClient(private val settings: SettingsRepository) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val jsonMediaType = "application/json".toMediaType()

    // ------------------------------------------------------------------ utils

    private fun baseUrl(): String = settings.getServerUrl().trimEnd('/')

    private fun requestBuilder(path: String, method: String, body: String?): Request.Builder {
        val builder = Request.Builder()
            .url(baseUrl() + path)
            .header("Accept", "application/json")
        val key = settings.getApiKey().trim()
        if (key.isNotEmpty()) {
            builder.header("Authorization", "Bearer $key")
        }
        if (body != null) {
            builder.method(method, body.toRequestBody(jsonMediaType))
        } else {
            builder.method(method, null)
        }
        return builder
    }

    private suspend fun <T> execute(
        path: String,
        method: String = "GET",
        body: String? = null,
        parse: (String) -> T
    ): T = withContext(Dispatchers.IO) {
        val request = requestBuilder(path, method, body).build()
        httpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val detail = responseBody.trim()
                val code = response.code()
                throw IOException(if (detail.isEmpty()) "HTTP $code (${response.message})" else "HTTP $code : $detail")
            }
            parse(responseBody)
        }
    }

    @Serializable
    private data class ApiResponse<T>(@SerialName("data") val data: List<T>?)

    /** Wrapper pour les endpoints dont le champ "data" est un seul objet (POST /api/session). */
    @Serializable
    private data class ApiResponseData<T>(@SerialName("data") val data: T?)

    @Serializable
    data class ModelInfoApi(
        @SerialName("id") val id: String = "",
        @SerialName("name") val name: String? = null,
        @SerialName("providerID") val providerId: String? = null,
        @SerialName("type") val type: String? = null,
        @SerialName("status") val status: String? = null,
    )

    @Serializable
    data class SessionInfo(
        @SerialName("id") val id: String,
        @SerialName("title") val title: String? = null,
    )

    @Serializable
    data class OpenCodeMessage(
        @SerialName("id") val id: String? = null,
        @SerialName("type") val type: String? = null,
        @SerialName("text") val text: String? = null,
        @SerialName("time") val time: MessageTime? = null,
        @SerialName("content") val content: List<ContentPart>? = null,
    ) {
        @Serializable
        data class MessageTime(
            @SerialName("created") val created: Long? = null,
            @SerialName("completed") val completed: Long? = null,
        )

        @Serializable
        data class ContentPart(
            @SerialName("type") val type: String? = null,
            @SerialName("text") val text: String? = null,
        )

        /** Texte concaténé des parties de type "text" de la réponse de l'assistant. */
        fun textContent(): String =
            content
                ?.filter { it.type == "text" && !it.text.isNullOrBlank() }
                ?.joinToString("") { it.text.orEmpty() }
                ?: text.orEmpty()
    }

    @Serializable
    private data class PromptInput(@SerialName("text") val text: String)

    // ------------------------------------------------------------------- API

    /** GET /api/model : liste des modèles disponibles. */
    suspend fun listModels(): List<com.fabrice.simai.data.model.ModelInfo> =
        execute("/api/model") { body -> json.decodeFromString<ApiResponse<ModelInfoApi>>(body).data.orEmpty().map { it.toModelInfo() } }

    /** POST /api/session : crée une nouvelle session OpenCode. */
    suspend fun createSession(): SessionInfo =
        execute("/api/session", "POST", "{}") { body ->
            json.decodeFromString<ApiResponseData<SessionInfo>>(body).data!!
        }

    /**
     * POST /api/session/{id}/prompt : envoie le message.
     * Retourne dès que le message est enfilé — on utilise [sendAndWaitReply] pour lire la réponse.
     */
    suspend fun sendMessage(sessionId: String, text: String) {
        execute(
            "/api/session/$sessionId/prompt",
            "POST",
            json.encodeToString(PromptInput(text))
        ) { }
    }

    /** GET /api/session/{id}/message?order=asc : liste des messages de la session. */
    suspend fun listMessages(sessionId: String): List<OpenCodeMessage> =
        execute("/api/session/$sessionId/message?order=asc") { body ->
            json.decodeFromString<ApiResponse<OpenCodeMessage>>(body).data.orEmpty()
        }

    /** DELETE /api/session/{id} : supprime une session (meilleur effort, pas d'erreur si déjà supprimée). */
    suspend fun deleteSession(sessionId: String) {
        execute("/api/session/$sessionId", "DELETE") { }
    }

    /**
     * Envoie un message et attend la réponse de l'assistant en sondant
     * [listMessages] jusqu'à ce que le message de type "assistant" soit complété.
     */
    suspend fun sendAndWaitReply(
        sessionId: String,
        text: String,
        timeoutMs: Long = 240_000
    ): String {
        sendMessage(sessionId, text)
        val sentAt = System.currentTimeMillis()
        val pollMs = 1_000
        var waited = 0L
        while (waited < timeoutMs) {
            delay(pollMs.toLong())
            waited += pollMs
            val reply = listMessages(sessionId)
                .lastOrNull { m -> m.type == "assistant" && m.time?.created != null && m.time!!.created >= sentAt }
            if (reply != null && reply.time?.completed != null) {
                val content = reply.textContent()
                if (content.isNotBlank()) return content
            }
        }
        throw IOException("Délai dépassé en attente de la réponse de Sim AI")
    }

    private fun ModelInfoApi.toModelInfo(): com.fabrice.simai.data.model.ModelInfo =
        com.fabrice.simai.data.model.ModelInfo(
            id = id,
            name = name,
            providerId = providerId,
            type = type,
            status = status
        )
}
