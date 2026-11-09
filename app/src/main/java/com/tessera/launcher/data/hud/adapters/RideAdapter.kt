package com.tessera.launcher.data.hud.adapters

import android.app.Notification
import android.content.Context
import android.graphics.Color
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.helper.RideNotificationParser
import com.tessera.launcher.data.hud.HudNotificationSnapshot
import com.tessera.launcher.data.hud.HudSignalClassifier
import com.tessera.launcher.data.model.HudMilestone
import com.tessera.launcher.data.model.HudSource
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType
import com.tessera.launcher.data.model.RideStage

class RideAdapter : LiveActivityAdapter {

    override val type: LiveActivityType = LiveActivityType.RIDE

    override fun matches(snapshot: HudNotificationSnapshot): Boolean {
        return snapshot.packageName in HudSignalClassifier.RIDE_PACKAGES ||
                snapshot.category == Notification.CATEGORY_TRANSPORT
    }

    override fun parse(
        snapshot: HudNotificationSnapshot,
        sbn: StatusBarNotification,
        context: Context
    ): LiveActivityInfo {
        val details = RideNotificationParser.parse(
            sbn = sbn,
            notification = sbn.notification,
            context = context,
            title = snapshot.title,
            text = snapshot.text,
            subText = snapshot.subText
        )

        val brandColor = snapshot.color ?: if (details.appName.equals("99", ignoreCase = true)) {
            Color.rgb(255, 179, 0)
        } else {
            Color.rgb(37, 99, 235) // Azul Uber
        }

        val criticalText = details.etaMinutes?.let { "$it min" }
            ?: details.etaText
            ?: details.statusMessage
            ?: details.appName

        val milestones = listOf(
            HudMilestone("Buscando", true),
            HudMilestone("A caminho", details.stage in listOf(RideStage.ARRIVING, RideStage.ARRIVED, RideStage.IN_TRIP, RideStage.COMPLETED)),
            HudMilestone("Chegou", details.stage in listOf(RideStage.ARRIVED, RideStage.IN_TRIP, RideStage.COMPLETED)),
            HudMilestone("Em rota", details.stage in listOf(RideStage.IN_TRIP, RideStage.COMPLETED))
        )

        return LiveActivityInfo(
            id = sbn.key ?: "${snapshot.packageName}_${sbn.id}",
            packageName = snapshot.packageName,
            type = LiveActivityType.RIDE,
            title = details.appName,
            subtitle = details.statusMessage ?: details.driverName,
            extraInfo = details.licensePlate?.let { "$it • ${details.vehicleModel ?: ""}".trim() },
            progress = (details.progressPercent * 100).toInt(),
            artwork = details.vehicleImage ?: details.driverAvatar,
            priorityScore = 80,
            timestamp = snapshot.postTime,
            appName = details.appName,
            brandColor = brandColor,
            criticalText = criticalText,
            milestones = milestones,
            source = HudSource.ADAPTER,
            rideDetails = details,
            actions = details.actions
        )
    }
}
