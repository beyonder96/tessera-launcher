package com.tessera.launcher.data.model

import android.app.PendingIntent
import android.graphics.Bitmap

/**
 * Modelo de notificação comum / recente para exibição no Feed / Hub de Notificações do Tessera.
 */
data class AppNotificationItem(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val largeIcon: Bitmap? = null,
    val contentIntent: PendingIntent? = null,
    val isOngoing: Boolean = false
)
