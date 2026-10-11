package com.tessera.launcher.data.model

import android.graphics.Bitmap

/**
 * Categorias de Live Activities e Ongoing Notifications suportadas no Tessera Launcher.
 */
enum class LiveActivityType {
    RIDE,        // Uber, 99, Didi, Bolt, Lyft
    DELIVERY,    // iFood, Rappi, Uber Eats, Zé Delivery
    TIMER,       // Cronômetro ou Timer em contagem regressiva
    NAVIGATION,  // Google Maps, Waze
    CALL,        // Ligação ativa (Telefone / WhatsApp)
    FITNESS,     // Strava, Nike Run Club
    OTHER        // Outros serviços em primeiro plano contínuos
}

/**
 * Modelo de dados unificado para Live Activities de aplicativos de terceiros.
 */
data class LiveActivityInfo(
    val id: String,
    val packageName: String,
    val type: LiveActivityType,
    val title: String,
    val subtitle: String? = null,
    val extraInfo: String? = null,
    val progress: Int? = null,
    val chronometerBaseMs: Long? = null,
    val isChronometerCountDown: Boolean = false,
    val artwork: Bitmap? = null,
    val priorityScore: Int = 10,
    val timestamp: Long = System.currentTimeMillis()
)
