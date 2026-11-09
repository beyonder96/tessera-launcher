package com.tessera.launcher.data.hud.adapters

import android.content.Context
import android.graphics.Color
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.bridge.TesseraBridgeContract
import com.tessera.launcher.data.hud.HudNotificationSnapshot
import com.tessera.launcher.data.model.HudSource
import com.tessera.launcher.data.model.LiveActivityAction
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType

class TesseraCoreAdapter : LiveActivityAdapter {

    override val type: LiveActivityType = LiveActivityType.OTHER

    override fun matches(snapshot: HudNotificationSnapshot): Boolean {
        return snapshot.packageName == TesseraBridgeContract.CORE_PACKAGE
    }

    override fun parse(
        snapshot: HudNotificationSnapshot,
        sbn: StatusBarNotification,
        context: Context
    ): LiveActivityInfo {
        val extras = sbn.notification?.extras
        val ecosystemKind = extras?.getString("com.tessera.live.kind")

        val targetType = when (ecosystemKind?.uppercase()) {
            "MARKET" -> LiveActivityType.MARKET
            "TIME_CLOCK", "PONTO" -> LiveActivityType.TIME_CLOCK
            "FOCUS", "CHRONOS" -> LiveActivityType.FOCUS
            "TRANSIT" -> LiveActivityType.NAVIGATION
            else -> LiveActivityType.OTHER
        }

        val brandColor = when (targetType) {
            LiveActivityType.MARKET -> Color.rgb(0, 230, 118)
            LiveActivityType.TIME_CLOCK -> Color.rgb(61, 90, 254)
            LiveActivityType.FOCUS -> Color.rgb(255, 171, 0)
            else -> Color.rgb(0, 229, 255)
        }

        val actions = sbn.notification?.actions?.mapNotNull { act ->
            val actTitle = act.title?.toString()?.trim() ?: return@mapNotNull null
            LiveActivityAction(actTitle, act.actionIntent)
        } ?: emptyList()

        return LiveActivityInfo(
            id = sbn.key ?: "${snapshot.packageName}_${sbn.id}",
            packageName = snapshot.packageName,
            type = targetType,
            title = snapshot.title.ifBlank { "Tessera Core" },
            subtitle = snapshot.text,
            extraInfo = snapshot.subText,
            progress = snapshot.progress,
            chronometerBaseMs = snapshot.chronometerBaseMs,
            isChronometerCountDown = snapshot.isChronometerCountDown,
            priorityScore = 90,
            timestamp = snapshot.postTime,
            appName = "Tessera Core",
            brandColor = brandColor,
            criticalText = snapshot.text.takeIf { it.isNotBlank() } ?: snapshot.title,
            source = HudSource.TESSERA_CORE,
            actions = actions
        )
    }
}
