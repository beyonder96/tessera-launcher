package com.tessera.launcher.data.model

data class PinnedShortcutInfo(
    val id: String,
    val packageName: String,
    val label: String,
    val iconFileName: String? = null,
    val intentUri: String? = null,
    val isPwa: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
