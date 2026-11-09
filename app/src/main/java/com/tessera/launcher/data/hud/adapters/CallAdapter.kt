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

class CallAdapter : LiveActivityAdapter {

    override val type: LiveActivityType = LiveActivityType.CALL

    override fun matches(snapshot: HudNotificationSnapshot): Boolean {
        val isCallStyle = snapshot.template?.contains("CallStyle", ignoreCase = true) == true
        val isCallCategory = snapshot.category == Notification.CATEGORY_CALL || snapshot.category == "call"
        return isCallStyle || (snapshot.packageName in HudSignalClassifier.CALL_PACKAGES && (isCallCategory || snapshot.isOngoing))
    }

    override fun parse(
        snapshot: HudNotificationSnapshot,
        sbn: StatusBarNotification,
        context: Context
    ): LiveActivityInfo {
        val brandColor = snapshot.color ?: Color.rgb(0, 230, 118) // Verde chamada

        val actions = sbn.notification?.actions?.mapNotNull { act ->
            val actTitle = act.title?.toString()?.trim() ?: return@mapNotNull null
            LiveActivityAction(actTitle, act.actionIntent)
        } ?: emptyList()

        val criticalText = when {
            snapshot.hasChronometer -> "Em chamada"
            snapshot.text.isNotBlank() -> snapshot.text
            else -> "Chamada ativa"
        }

        return LiveActivityInfo(
            id = sbn.key ?: "${snapshot.packageName}_${sbn.id}",
            packageName = snapshot.packageName,
            type = LiveActivityType.CALL,
            title = snapshot.title.ifBlank { snapshot.appName },
            subtitle = snapshot.text.ifBlank { "Chamada em andamento" },
            extraInfo = snapshot.subText.takeIf { it.isNotBlank() },
            chronometerBaseMs = snapshot.chronometerBaseMs,
            isChronometerCountDown = false,
            priorityScore = 100,
            timestamp = snapshot.postTime,
            appName = snapshot.appName,
            brandColor = brandColor,
            criticalText = criticalText,
            source = HudSource.ADAPTER,
            actions = actions
        )
    }
}
