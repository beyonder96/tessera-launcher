package com.tessera.launcher.data.model

import android.app.PendingIntent
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
 * Ação rápida associada à notificação de Live Activity (ex: Chamar motorista, Mensagem, Cancelar).
 */
data class LiveActivityAction(
    val title: String,
    val intent: PendingIntent? = null
)

/**
 * Etapas da corrida para Uber / 99 / apps de mobilidade urbana.
 */
enum class RideStage {
    SEARCHING,    // Procurando motorista / confirmando
    ARRIVING,     // Motorista a caminho do local de embarque
    ARRIVED,      // Motorista chegou ao ponto de partida
    IN_TRIP,      // Em rota para o destino
    COMPLETED     // Finalizado
}

/**
 * Detalhes estruturados de corridas de transporte (Uber, 99, Lyft, etc.).
 */
data class RideActivityDetails(
    val appName: String,
    val stage: RideStage = RideStage.ARRIVING,
    val etaMinutes: Int? = null,
    val etaText: String? = null,
    val statusMessage: String? = null,
    val driverName: String? = null,
    val driverRating: String? = null,
    val licensePlate: String? = null,
    val vehicleModel: String? = null,
    val driverAvatar: Bitmap? = null,
    val vehicleImage: Bitmap? = null,
    val progressPercent: Float = 0.5f,
    val actions: List<LiveActivityAction> = emptyList()
)

/**
 * Etapas de entrega de pedidos (iFood, Rappi, Zé Delivery, Uber Eats).
 */
enum class DeliveryStage {
    CONFIRMED,    // Pedido confirmado pelo restaurante
    PREPARING,    // Na cozinha / sendo preparado
    ON_THE_WAY,   // Saiu para entrega / entregador a caminho
    DELIVERED     // Pedido entregue
}

/**
 * Detalhes estruturados de delivery de refeições e compras.
 */
data class DeliveryActivityDetails(
    val appName: String,
    val storeName: String,
    val stage: DeliveryStage = DeliveryStage.ON_THE_WAY,
    val etaMinutes: Int? = null,
    val etaText: String? = null,
    val statusMessage: String? = null,
    val confirmationPin: String? = null,
    val orderSummary: String? = null,
    val storeLogo: Bitmap? = null,
    val progressPercent: Float = 0.5f,
    val actions: List<LiveActivityAction> = emptyList()
)

/**
 * Tipos de manobras de navegação GPS (Google Maps, Waze).
 */
enum class ManeuverType {
    TURN_LEFT,
    TURN_RIGHT,
    TURN_SLIGHT_LEFT,
    TURN_SLIGHT_RIGHT,
    TURN_SHARP_LEFT,
    TURN_SHARP_RIGHT,
    STRAIGHT,
    U_TURN,
    ROUNDABOUT,
    FORK,
    MERGE,
    DESTINATION,
    UNKNOWN
}

/**
 * Detalhes estruturados de navegação GPS curva a curva (Google Maps, Waze).
 */
data class NavigationActivityDetails(
    val appName: String,
    val maneuver: ManeuverType = ManeuverType.STRAIGHT,
    val maneuverText: String? = null,
    val distanceToManeuver: String? = null,
    val nextStreetName: String? = null,
    val etaRemaining: String? = null,
    val distanceRemaining: String? = null,
    val estimatedArrival: String? = null,
    val progressPercent: Float = 0.5f,
    val actions: List<LiveActivityAction> = emptyList()
)

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
    val timestamp: Long = System.currentTimeMillis(),
    val rideDetails: RideActivityDetails? = null,
    val deliveryDetails: DeliveryActivityDetails? = null,
    val navigationDetails: NavigationActivityDetails? = null,
    val actions: List<LiveActivityAction> = emptyList()
)
