package com.tessera.launcher.data.hud.adapters

import android.app.Notification
import android.content.Context
import android.graphics.Color
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.helper.NavigationNotificationParser
import com.tessera.launcher.data.hud.HudNotificationSnapshot
import com.tessera.launcher.data.hud.HudSignalClassifier
import com.tessera.launcher.data.model.HudSource
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType

class NavigationAdapter : LiveActivityAdapter {

    override val type: LiveActivityType = LiveActivityType.NAVIGATION

    override fun matches(snapshot: HudNotificationSnapshot): Boolean {
        return snapshot.packageName in HudSignalClassifier.NAVIGATION_PACKAGES ||
                snapshot.category == Notification.CATEGORY_NAVIGATION
    }

    override fun parse(
        snapshot: HudNotificationSnapshot,
        sbn: StatusBarNotification,
        context: Context
    ): LiveActivityInfo {
        val details = NavigationNotificationParser.parse(
            sbn = sbn,
            notification = sbn.notification,
            context = context,
            title = snapshot.title,
            text = snapshot.text,
            subText = snapshot.subText
        )

        val brandColor = snapshot.color ?: if (details.appName.contains("waze", ignoreCase = true)) {
            Color.rgb(51, 204, 255) // Azul Waze
        } else {
            Color.rgb(16, 185, 129) // Verde Google Maps
        }

        val criticalText = details.distanceToManeuver?.let { dist ->
            details.maneuverText?.let { "$dist • $it" } ?: dist
        } ?: details.etaRemaining ?: details.nextStreetName ?: details.appName

        return LiveActivityInfo(
            id = sbn.key ?: "${snapshot.packageName}_${sbn.id}",
            packageName = snapshot.packageName,
            type = LiveActivityType.NAVIGATION,
            title = details.appName,
            subtitle = details.maneuverText ?: details.nextStreetName ?: snapshot.title,
            extraInfo = details.etaRemaining?.let { eta ->
                details.distanceRemaining?.let { "$eta • $it" } ?: eta
            },
            progress = (details.progressPercent * 100).toInt(),
            priorityScore = 70,
            timestamp = snapshot.postTime,
            appName = details.appName,
            brandColor = brandColor,
            criticalText = criticalText,
            source = HudSource.ADAPTER,
            navigationDetails = details,
            actions = details.actions
        )
    }
}
