package com.tessera.launcher.data.hud.adapters

import android.content.Context
import android.graphics.Color
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.helper.DeliveryNotificationParser
import com.tessera.launcher.data.hud.HudNotificationSnapshot
import com.tessera.launcher.data.hud.HudSignalClassifier
import com.tessera.launcher.data.model.DeliveryStage
import com.tessera.launcher.data.model.HudMilestone
import com.tessera.launcher.data.model.HudSource
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType

class DeliveryAdapter : LiveActivityAdapter {

    override val type: LiveActivityType = LiveActivityType.DELIVERY

    override fun matches(snapshot: HudNotificationSnapshot): Boolean {
        return snapshot.packageName in HudSignalClassifier.DELIVERY_PACKAGES
    }

    override fun parse(
        snapshot: HudNotificationSnapshot,
        sbn: StatusBarNotification,
        context: Context
    ): LiveActivityInfo {
        val details = DeliveryNotificationParser.parse(
            sbn = sbn,
            notification = sbn.notification,
            context = context,
            title = snapshot.title,
            text = snapshot.text,
            subText = snapshot.subText
        )

        val brandColor = snapshot.color ?: when {
            details.appName.contains("ifood", ignoreCase = true) -> Color.rgb(234, 29, 44)
            details.appName.contains("rappi", ignoreCase = true) -> Color.rgb(255, 68, 31)
            details.appName.contains("ze", ignoreCase = true) -> Color.rgb(255, 179, 0)
            else -> Color.rgb(234, 29, 44)
        }

        val criticalText = details.confirmationPin?.let { "PIN $it" }
            ?: details.etaMinutes?.let { "$it min" }
            ?: details.etaText
            ?: details.statusMessage
            ?: details.appName

        val milestones = listOf(
            HudMilestone("Confirmado", true),
            HudMilestone("Cozinha", details.stage in listOf(DeliveryStage.PREPARING, DeliveryStage.ON_THE_WAY, DeliveryStage.DELIVERED)),
            HudMilestone("A caminho", details.stage in listOf(DeliveryStage.ON_THE_WAY, DeliveryStage.DELIVERED)),
            HudMilestone("Entregue", details.stage == DeliveryStage.DELIVERED)
        )

        return LiveActivityInfo(
            id = sbn.key ?: "${snapshot.packageName}_${sbn.id}",
            packageName = snapshot.packageName,
            type = LiveActivityType.DELIVERY,
            title = details.appName,
            subtitle = details.statusMessage ?: details.storeName,
            extraInfo = details.confirmationPin?.let { "PIN: $it" } ?: details.orderSummary,
            progress = (details.progressPercent * 100).toInt(),
            artwork = details.storeLogo,
            priorityScore = 75,
            timestamp = snapshot.postTime,
            appName = details.appName,
            brandColor = brandColor,
            criticalText = criticalText,
            milestones = milestones,
            source = HudSource.ADAPTER,
            deliveryDetails = details,
            actions = details.actions
        )
    }
}
