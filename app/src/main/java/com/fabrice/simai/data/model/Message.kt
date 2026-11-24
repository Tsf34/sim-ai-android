package com.fabrice.simai.data.model

/** Un message échangé (utilisateur ou assistant). */
data class Message(
    val role: String,       // "user" ou "assistant"
    val content: String,
    val timestamp: Long,
)
