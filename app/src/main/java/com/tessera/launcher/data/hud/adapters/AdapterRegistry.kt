package com.tessera.launcher.data.hud.adapters

import android.content.Context
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.hud.HudNotificationSnapshot
import com.tessera.launcher.data.model.LiveActivityInfo

/**
 * Registro ordenado de adaptadores especializados de Live Activity.
 * O primeiro adaptador que corresponder ao snapshot processa a notificação.
 */
object AdapterRegistry {

    private val adapters: List<LiveActivityAdapter> = listOf(
        TesseraCoreAdapter(),
        NativeLiveUpdateAdapter(),
        CallAdapter(),
        TimerAdapter(),
        RideAdapter(),
        DeliveryAdapter(),
        NavigationAdapter(),
        PackageAdapter(),
        GenericAdapter() // Fallback final
    )

    fun parse(
        snapshot: HudNotificationSnapshot,
        sbn: StatusBarNotification,
        context: Context
    ): LiveActivityInfo? {
        val matchedAdapter = adapters.firstOrNull { it.matches(snapshot) } ?: return null
        return matchedAdapter.parse(snapshot, sbn, context)
    }
}
