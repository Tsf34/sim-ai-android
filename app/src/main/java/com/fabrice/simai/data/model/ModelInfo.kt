package com.fabrice.simai.data.model

/** Un modèle disponible via l'API OpenCode (GET /api/model). */
data class ModelInfo(
    val id: String,
    val name: String? = null,
    val providerId: String? = null,
    val type: String? = null,
    val status: String? = null,
)
