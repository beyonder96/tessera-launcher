package com.tessera.launcher.data.hud

import android.app.Notification
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.Immutable

/**
 * Snapshot desacoplado de uma notificação do sistema Android.
 * Contém todos os metadados brutos necessários para a classificação (Live vs Peek vs Ignorar)
 * e parsing pelos adapters especializados, permitindo execução pura fora da thread principal
 * e testes unitários em JVM sem mocks complexos do framework Android.
 */
@Immutable
data class HudNotificationSnapshot(
    val key: String,
    val packageName: String,
    val appName: String,
    val flags: Int = 0,
    val category: String? = null,
    val template: String? = null,
    val isOngoing: Boolean = false,
    val isGroupSummary: Boolean = false,
    val isForegroundService: Boolean = false,
    val channelImportance: Int = 3, // NotificationManager.IMPORTANCE_DEFAULT = 3
    val isAmbient: Boolean = false,
    val matchesInterruptionFilter: Boolean = true,
    val visibility: Int = Notification.VISIBILITY_PUBLIC,
    val hasChronometer: Boolean = false,
    val chronometerBaseMs: Long? = null,
    val isChronometerCountDown: Boolean = false,
    val progress: Int? = null,
    val progressMax: Int = 0,
    val isProgressIndeterminate: Boolean = false,
    val requestPromotedOngoing: Boolean = false,
    val title: String = "",
    val text: String = "",
    val subText: String = "",
    val bigText: String = "",
    val shortCriticalText: String? = null,
    val color: Int? = null,
    val postTime: Long = System.currentTimeMillis(),
    val actionsCount: Int = 0,
    val hasLargeIcon: Boolean = false
) {
    val fullText: String by lazy {
        buildString {
            if (title.isNotBlank()) append(title)
            if (text.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append(text)
            }
            if (subText.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append(subText)
            }
            if (bigText.isNotBlank() && bigText != text) {
                if (isNotEmpty()) append(" • ")
                append(bigText)
            }
        }
    }

    companion object {
        private const val EXTRA_TEMPLATE = "android.template"
        private const val EXTRA_REQUEST_PROMOTED_ONGOING = "android.requestPromotedOngoing"
        private const val EXTRA_SHORT_CRITICAL_TEXT = "android.shortCriticalText"
        private const val FLAG_PROMOTED_ONGOING = 0x00000020 // Android 16

        fun from(
            sbn: StatusBarNotification,
            ranking: NotificationListenerService.Ranking?,
            context: Context
        ): HudNotificationSnapshot {
            val notification = sbn.notification
            val extras = notification?.extras
            val pkg = sbn.packageName ?: ""

            val appName = runCatching {
                val pm = context.packageManager
                val info = pm.getApplicationInfo(pkg, 0)
                pm.getApplicationLabel(info).toString()
            }.getOrDefault(pkg)

            val flags = notification?.flags ?: 0
            val isOngoing = sbn.isOngoing || ((flags and Notification.FLAG_ONGOING_EVENT) != 0)
            val isForegroundService = (flags and Notification.FLAG_FOREGROUND_SERVICE) != 0
            val isGroupSummary = (flags and Notification.FLAG_GROUP_SUMMARY) != 0

            val rawTitle = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
                ?: extras?.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()?.trim()
                ?: ""

            val rawText = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
            val rawBigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim() ?: ""
            val rawSubText = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim() ?: ""

            val template = extras?.getString(EXTRA_TEMPLATE)

            val showChronometer = extras?.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER, false) ?: false
            val chronometerBase = if (showChronometer) {
                val base = extras?.getLong("android.chronometerBase", 0L) ?: 0L
                if (base > 0) base else notification?.`when`
            } else null

            val isCountDown = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                extras?.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN, false) ?: false
            } else false

            val maxProgress = extras?.getInt(Notification.EXTRA_PROGRESS_MAX, 0) ?: 0
            val curProgress = extras?.getInt(Notification.EXTRA_PROGRESS, 0) ?: 0
            val indeterminate = extras?.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false) ?: false

            val progressPercent = if (maxProgress > 0 && !indeterminate) {
                ((curProgress.toFloat() / maxProgress) * 100).toInt().coerceIn(0, 100)
            } else null

            val promotedOngoing = (flags and FLAG_PROMOTED_ONGOING) != 0 ||
                    (extras?.getBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, false) ?: false)

            val criticalText = extras?.getCharSequence(EXTRA_SHORT_CRITICAL_TEXT)?.toString()?.trim()

            val notifColor = notification?.color?.takeIf { it != Notification.COLOR_DEFAULT && it != 0 }

            val channelImportance = ranking?.importance ?: 3
            val isAmbient = ranking?.isAmbient ?: false
            val matchesFilter = ranking?.matchesInterruptionFilter() ?: true

            val visibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification?.visibility ?: Notification.VISIBILITY_PUBLIC
            } else Notification.VISIBILITY_PUBLIC

            val actionsCount = notification?.actions?.size ?: 0
            val hasLargeIcon = notification?.getLargeIcon() != null

            return HudNotificationSnapshot(
                key = sbn.key ?: "${pkg}_${sbn.id}",
                packageName = pkg,
                appName = appName,
                flags = flags,
                category = notification?.category,
                template = template,
                isOngoing = isOngoing,
                isGroupSummary = isGroupSummary,
                isForegroundService = isForegroundService,
                channelImportance = channelImportance,
                isAmbient = isAmbient,
                matchesInterruptionFilter = matchesFilter,
                visibility = visibility,
                hasChronometer = showChronometer,
                chronometerBaseMs = chronometerBase,
                isChronometerCountDown = isCountDown,
                progress = progressPercent,
                progressMax = maxProgress,
                isProgressIndeterminate = indeterminate,
                requestPromotedOngoing = promotedOngoing,
                title = rawTitle,
                text = rawText,
                subText = rawSubText,
                bigText = rawBigText,
                shortCriticalText = criticalText,
                color = notifColor,
                postTime = if (sbn.postTime > 0) sbn.postTime else System.currentTimeMillis(),
                actionsCount = actionsCount,
                hasLargeIcon = hasLargeIcon
            )
        }
    }
}
