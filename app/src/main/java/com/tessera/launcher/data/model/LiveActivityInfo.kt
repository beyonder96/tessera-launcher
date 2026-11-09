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
    MARKET,      // Modo Mercado (Tessera Core)
    TIME_CLOCK,  // Ponto & Jornada CLT (Tessera Core)
    FOCUS,       // Sessão Pomodoro / Chronos (Tessera Core)
    PACKAGE,     // Encomendas e e-commerce (Mercado Livre, Shopee, Amazon, Correios, etc.)
    PROGRESS,    // Downloads, uploads, processos longos (Play Store, Chrome, etc.)
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
    val actions: List<LiveActivityAction> = emptyList(),
    val appName: String = "",
    val brandColor: Int? = null,
    val criticalText: String? = null,
    val milestones: List<HudMilestone> = emptyList(),
    val isSticky: Boolean = false,
    val expiresAt: Long? = null,
    val source: HudSource = HudSource.HEURISTIC,
    val packageDetails: PackageActivityDetails? = null
)

/**
 * Origem / motor de identificação da Live Activity.
 */
enum class HudSource {
    NATIVE_LIVE_UPDATE, // ProgressStyle / CallStyle / requestPromotedOngoing nativo (Android 16+)
    ADAPTER,            // Parser especializado do Tessera (Uber, iFood, Maps, ML, etc.)
    HEURISTIC,          // Heurística de notificação contínua aprovada pelo classificador
    TESSERA_CORE        // Ecossistema nativo do Tessera Launcher
}

/**
 * Marco individual em uma jornada de progresso por etapas (ex: Comprado -> Enviado -> Em rota -> Entregue).
 */
data class HudMilestone(
    val label: String,
    val reached: Boolean
)

/**
 * Estágios de encomendas e rastreio de e-commerce.
 */
enum class PackageStage {
    ORDERED,           // Pedido realizado / pagamento aprovado
    SHIPPED,           // Enviado pelo vendedor / postado
    IN_TRANSIT,        // Em trânsito entre centros de distribuição
    OUT_FOR_DELIVERY,  // Saiu para entrega ao destinatário (hoje)
    PICKUP_READY,      // Pronto para retirada na agência / locker
    DELIVERED,         // Entregue
    ISSUE              // Tentativa sem sucesso / aguardando ação / taxa alfandegária
}

/**
 * Detalhes estruturados de encomendas e compras (Mercado Livre, Shopee, Amazon, Correios, Loggi, etc.).
 */
data class PackageActivityDetails(
    val appName: String,
    val stage: PackageStage = PackageStage.OUT_FOR_DELIVERY,
    val headline: String,
    val etaText: String? = null,
    val trackingCode: String? = null,
    val itemSummary: String? = null,
    val packageLogo: Bitmap? = null,
    val milestones: List<HudMilestone> = emptyList(),
    val actions: List<LiveActivityAction> = emptyList()
)
