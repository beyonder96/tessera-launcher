package com.tessera.launcher.data.hud.adapters

import android.content.Context
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.hud.HudNotificationSnapshot
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType

/**
 * Interface comum para adaptadores de Live Activities no Tessera Launcher.
 * Cada adaptador é especialista em converter snapshots e notificações brutas
 * no modelo padronizado LiveActivityInfo com metadados ricos (criticalText, marcos, etc.).
 */
interface LiveActivityAdapter {
    val type: LiveActivityType
    fun matches(snapshot: HudNotificationSnapshot): Boolean
    fun parse(
        snapshot: HudNotificationSnapshot,
        sbn: StatusBarNotification,
        context: Context
    ): LiveActivityInfo?
}
