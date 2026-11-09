package com.tessera.launcher.data.hud.adapters

import android.app.Notification
import android.content.Context
import android.graphics.Color
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.hud.HudNotificationSnapshot
import com.tessera.launcher.data.hud.HudSignalClassifier
import com.tessera.launcher.data.model.HudSource
import com.tessera.launcher.data.model.LiveActivityAction
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType

class TimerAdapter : LiveActivityAdapter {

    override val type: LiveActivityType = LiveActivityType.TIMER

    override fun matches(snapshot: HudNotificationSnapshot): Boolean {
        val isStopwatch = snapshot.category == Notification.CATEGORY_STOPWATCH || snapshot.category == "stopwatch"
        return snapshot.packageName in HudSignalClassifier.TIMER_PACKAGES || isStopwatch || snapshot.hasChronometer
    }

    override fun parse(
        snapshot: HudNotificationSnapshot,
        sbn: StatusBarNotification,
        context: Context
    ): LiveActivityInfo {
        val brandColor = snapshot.color ?: Color.rgb(255, 145, 0) // Laranja timer

        val actions = sbn.notification?.actions?.mapNotNull { act ->
            val actTitle = act.title?.toString()?.trim() ?: return@mapNotNull null
            LiveActivityAction(actTitle, act.actionIntent)
        } ?: emptyList()

        val criticalText = when {
            snapshot.text.isNotBlank() -> snapshot.text
            snapshot.isChronometerCountDown -> "Timer"
            else -> "Cronômetro"
        }

        return LiveActivityInfo(
            id = sbn.key ?: "${snapshot.packageName}_${sbn.id}",
            packageName = snapshot.packageName,
            type = LiveActivityType.TIMER,
            title = snapshot.title.ifBlank { snapshot.appName },
            subtitle = snapshot.text.ifBlank { "Contagem ativa" },
            chronometerBaseMs = snapshot.chronometerBaseMs,
            isChronometerCountDown = snapshot.isChronometerCountDown,
            priorityScore = 90,
            timestamp = snapshot.postTime,
            appName = snapshot.appName,
            brandColor = brandColor,
            criticalText = criticalText,
            source = HudSource.ADAPTER,
            actions = actions
        )
    }
}
