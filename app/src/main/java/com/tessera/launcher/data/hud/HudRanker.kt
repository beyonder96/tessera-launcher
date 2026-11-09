package com.tessera.launcher.data.hud

import com.tessera.launcher.data.model.DeliveryStage
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType
import com.tessera.launcher.data.model.PackageStage
import com.tessera.launcher.data.model.RideStage

/**
 * Algoritmo dinâmico de ranqueamento e prioridade de Live Activities.
 * Calcula pontuações baseadas em tipo, urgência e recência para selecionar
 * a atividade primária (centro da ilha) e a secundária (bolha split).
 */
object HudRanker {

    fun calculateScore(activity: LiveActivityInfo, nowMs: Long = System.currentTimeMillis()): Int {
        val baseScore = when (activity.type) {
            LiveActivityType.CALL -> 100
            LiveActivityType.TIMER -> 90
            LiveActivityType.FOCUS -> 90
            LiveActivityType.RIDE -> 85
            LiveActivityType.MARKET -> 85
            LiveActivityType.NAVIGATION -> 80
            LiveActivityType.DELIVERY -> 75
            LiveActivityType.PACKAGE -> {
                val stage = activity.packageDetails?.stage
                if (stage == PackageStage.OUT_FOR_DELIVERY || stage == PackageStage.PICKUP_READY) 72 else 45
            }
            LiveActivityType.TIME_CLOCK -> 65
            LiveActivityType.FITNESS -> 60
            LiveActivityType.PROGRESS -> 55
            LiveActivityType.OTHER -> 30
        }

        var urgencyBonus = 0

        // Bônus para motorista chegando / embarque iminente
        val ride = activity.rideDetails
        if (ride != null) {
            if (ride.stage == RideStage.ARRIVED || (ride.etaMinutes != null && ride.etaMinutes <= 2)) {
                urgencyBonus += 20
            }
        }

        // Bônus para entregador a caminho ou com PIN de confirmação
        val delivery = activity.deliveryDetails
        if (delivery != null) {
            if (delivery.confirmationPin != null) {
                urgencyBonus += 15
            } else if (delivery.stage == DeliveryStage.ON_THE_WAY) {
                urgencyBonus += 10
            }
        }

        // Bônus para timer em contagem regressiva nos momentos finais
        if (activity.type == LiveActivityType.TIMER && activity.isChronometerCountDown) {
            val base = activity.chronometerBaseMs ?: 0L
            val remainingSec = (base - nowMs) / 1000L
            if (remainingSec in 1..60) {
                urgencyBonus += 15
            }
        }

        // Bônus de recência: atividades atualizadas nos últimos 10 minutos
        val elapsedMinutes = ((nowMs - activity.timestamp).coerceAtLeast(0L) / 60_000L).toInt()
        val recencyBonus = (10 - elapsedMinutes).coerceIn(0, 10)

        return baseScore + urgencyBonus + recencyBonus
    }

    data class RankedActivities(
        val primary: LiveActivityInfo? = null,
        val secondary: LiveActivityInfo? = null,
        val others: List<LiveActivityInfo> = emptyList()
    )

    fun rank(activities: Collection<LiveActivityInfo>, nowMs: Long = System.currentTimeMillis()): RankedActivities {
        if (activities.isEmpty()) return RankedActivities()

        val sorted = activities.sortedByDescending { calculateScore(it, nowMs) }
        val primary = sorted.firstOrNull()
        val secondary = sorted.getOrNull(1)
        val others = if (sorted.size > 2) sorted.drop(2) else emptyList()

        return RankedActivities(primary = primary, secondary = secondary, others = others)
    }
}
