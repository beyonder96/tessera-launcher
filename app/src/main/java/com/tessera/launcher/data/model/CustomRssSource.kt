package com.tessera.launcher.data.model

import java.util.UUID

/**
 * Fonte de notícias RSS/Atom cadastrada manualmente pelo usuário.
 */
data class CustomRssSource(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val url: String,
    val isEnabled: Boolean = true
)
