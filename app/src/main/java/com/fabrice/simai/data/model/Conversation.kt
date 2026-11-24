package com.fabrice.simai.data.model

/** Conversation locale (un chat) — liée à une session OpenCode. */
data class Conversation(
    val id: Long,
    val opencodeSessionId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
)
