package com.tessera.launcher.data.hud.adapters

import android.content.Context
import android.graphics.Color
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.helper.PackageNotificationParser
import com.tessera.launcher.data.hud.HudNotificationSnapshot
import com.tessera.launcher.data.hud.HudSignalClassifier
import com.tessera.launcher.data.model.HudMilestone
import com.tessera.launcher.data.model.HudSource
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType
import com.tessera.launcher.data.model.PackageStage

class PackageAdapter : LiveActivityAdapter {

    override val type: LiveActivityType = LiveActivityType.PACKAGE

    override fun matches(snapshot: HudNotificationSnapshot): Boolean {
        val fullTextLower = snapshot.fullText.lowercase()
        return snapshot.packageName in HudSignalClassifier.PACKAGE_TRACKING_PACKAGES ||
                fullTextLower.contains("rastre") ||
                Regex("""\b[a-zA-Z]{2}\d{9}[a-zA-Z]{2}\b""").containsMatchIn(snapshot.fullText)
    }

    override fun parse(
        snapshot: HudNotificationSnapshot,
        sbn: StatusBarNotification,
        context: Context
    ): LiveActivityInfo {
        val details = PackageNotificationParser.parse(snapshot, sbn, context)

        val brandColor = snapshot.color ?: when {
            snapshot.packageName.contains("mercadolibre") -> Color.rgb(255, 230, 0)
            snapshot.packageName.contains("shopee") -> Color.rgb(238, 77, 45)
            snapshot.packageName.contains("amazon") -> Color.rgb(255, 153, 0)
            snapshot.packageName.contains("aliexpress") -> Color.rgb(255, 71, 0)
            snapshot.packageName.contains("correios") -> Color.rgb(0, 114, 206)
            snapshot.packageName.contains("loggi") -> Color.rgb(0, 194, 255)
            else -> null
        }

        val criticalText = when (details.stage) {
            PackageStage.OUT_FOR_DELIVERY -> details.etaText ?: "Saiu p/ entrega"
            PackageStage.DELIVERED -> "Entregue"
            PackageStage.PICKUP_READY -> "Retirar na agência"
            PackageStage.ISSUE -> "Verificar entrega"
            PackageStage.IN_TRANSIT -> "Em trânsito"
            PackageStage.SHIPPED -> "Enviado"
            PackageStage.ORDERED -> "Confirmado"
        }

        val isSticky = details.stage in listOf(
            PackageStage.OUT_FOR_DELIVERY,
            PackageStage.PICKUP_READY,
            PackageStage.ISSUE
        )

        val expiresAt = if (details.stage == PackageStage.DELIVERED) {
            System.currentTimeMillis() + 2 * 3600_000L // 2 horas após entregue
        } else {
            System.currentTimeMillis() + 24 * 3600_000L // 24 horas no máximo sem update
        }

        val progressPercent = when (details.stage) {
            PackageStage.ORDERED -> 15
            PackageStage.SHIPPED -> 40
            PackageStage.IN_TRANSIT -> 65
            PackageStage.OUT_FOR_DELIVERY -> 85
            PackageStage.PICKUP_READY -> 90
            PackageStage.DELIVERED -> 100
            PackageStage.ISSUE -> 75
        }

        return LiveActivityInfo(
            id = sbn.key ?: "${snapshot.packageName}_${sbn.id}",
            packageName = snapshot.packageName,
            type = LiveActivityType.PACKAGE,
            title = details.appName,
            subtitle = details.headline,
            extraInfo = details.etaText ?: details.trackingCode,
            progress = progressPercent,
            artwork = details.packageLogo,
            priorityScore = if (details.stage == PackageStage.OUT_FOR_DELIVERY) 72 else 45,
            timestamp = snapshot.postTime,
            appName = details.appName,
            brandColor = brandColor,
            criticalText = criticalText,
            milestones = details.milestones,
            isSticky = isSticky,
            expiresAt = expiresAt,
            source = HudSource.ADAPTER,
            packageDetails = details,
            actions = details.actions
        )
    }
}
