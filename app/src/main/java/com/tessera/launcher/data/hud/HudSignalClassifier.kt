package com.tessera.launcher.data.hud

import android.app.Notification
import com.tessera.launcher.data.model.HudAppRule
import com.tessera.launcher.data.model.HudClassification
import com.tessera.launcher.data.model.HudConfig
import com.tessera.launcher.data.model.HudDecision
import com.tessera.launcher.data.model.HudReason
import com.tessera.launcher.data.model.LiveActivityType

/**
 * Motor central de classificação semântica do Live HUD.
 * Determina se uma notificação deve:
 * 1. LIVE: se transformar em Live Activity dinâmica persistente na ilha;
 * 2. PEEK: disparar um alerta temporário (~4s) em forma de pílula expandida;
 * 3. IGNORE: ser descartada (serviços em segundo plano, ruído, etc.).
 *
 * Totalmente desacoplado do Android Framework para testes unitários em JVM.
 */
object HudSignalClassifier {

    val RIDE_PACKAGES = setOf(
        "com.ubercab",
        "com.taxis99",
        "com.didiglobal.passenger",
        "ee.mtakso.client",
        "me.lyft.android",
        "com.indriver"
    )

    val DELIVERY_PACKAGES = setOf(
        "br.com.brainweb.ifood",
        "com.rappi",
        "com.deliveryhero",
        "com.cerveceriamodelo.zedelivery",
        "com.ubercab.eats",
        "com.dd.doordash",
        "com.keeta.consumer",
        "com.glovo"
    )

    val NAVIGATION_PACKAGES = setOf(
        "com.google.android.apps.maps",
        "com.waze",
        "com.citymapper.app.release",
        "com.tranzmate" // Moovit
    )

    val PACKAGE_TRACKING_PACKAGES = setOf(
        "com.mercadolibre",
        "com.shopee.br",
        "com.amazon.mShop.android.shopping",
        "com.alibaba.aliexpresshd",
        "br.com.correios.srocorreios",
        "br.com.correios.precodoprazo",
        "com.loggi.client",
        "com.luizalabs.mlapp",
        "com.zzkko", // Shein
        "com.einnovation.temu"
    )

    val TIMER_PACKAGES = setOf(
        "com.google.android.deskclock",
        "com.sec.android.app.clockpackage",
        "com.coloros.alarmclock",
        "com.xiaomi.calendar",
        "com.oneplus.deskclock"
    )

    val FITNESS_PACKAGES = setOf(
        "com.strava",
        "com.nike.plusgps",
        "com.samsung.android.wear.shealth",
        "com.sec.android.app.shealth",
        "com.google.android.apps.fitness",
        "com.adidas.runningapp"
    )

    val CALL_PACKAGES = setOf(
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.whatsapp",
        "com.whatsapp.w4b",
        "org.telegram.messenger",
        "com.discord",
        "com.google.android.apps.tachyon", // Meet
        "us.zoom.videomeetings",
        "com.microsoft.teams"
    )

    val KNOWN_MUSIC_PACKAGES = setOf(
        "com.spotify.music",
        "com.google.android.apps.youtube.music",
        "com.deezer.android.app",
        "com.apple.android.music",
        "com.amazon.mp3",
        "com.soundcloud.android",
        "com.audiomack",
        "com.maxmpz.audioplayer",
        "com.tidal.mobile"
    )

    private val BACKGROUND_SERVICE_PHRASES = listOf(
        "em execução em segundo plano",
        "running in the background",
        "executando em segundo plano",
        "toque para mais opções",
        "tap for more options",
        "vpn ativada",
        "vpn is active",
        "conectado à vpn",
        "connected to vpn",
        "depuração usb",
        "usb debugging",
        "carregamento rápido ativado",
        "transferindo arquivos via usb",
        "serviço em primeiro plano",
        "syncing in background",
        "sincronizando dados"
    )

    private val OUT_FOR_DELIVERY_PHRASES = listOf(
        "saiu para entrega",
        "chega hoje",
        "a caminho do seu endereço",
        "motorista está a caminho",
        "entregador a caminho",
        "out for delivery",
        "en camino a tu domicilio"
    )

    private val PACKAGE_UPDATE_PHRASES = listOf(
        "compra aprovada",
        "pedido enviado",
        "objeto postado",
        "em trânsito",
        "disponível para retirada",
        "aguardando retirada",
        "objeto entregue",
        "pedido entregue"
    )

    private val HIGH_PRIORITY_PEEK_KEYWORDS = listOf(
        "código de verificação",
        "código pix",
        "pix recebido",
        "transferência recebida",
        "seu código é",
        "verification code",
        "chamada de voz perdida",
        "chamada de vídeo perdida",
        "missed call"
    )

    /**
     * Classifica o snapshot da notificação de acordo com as regras ativas e configurações do usuário.
     */
    fun classify(
        snapshot: HudNotificationSnapshot,
        config: HudConfig
    ): HudClassification {
        val pkg = snapshot.packageName

        // 1. Regra explícita do usuário para este app
        val userRule = config.appRules[pkg] ?: HudAppRule.AUTO
        if (userRule == HudAppRule.OFF) {
            return HudClassification(HudDecision.IGNORE, 100, HudReason.USER_RULE)
        }

        // 2. Filtros de ruído absolutos do sistema
        if (pkg == "com.tessera.launcher") {
            return HudClassification(HudDecision.IGNORE, 100, HudReason.SYSTEM)
        }
        if (pkg == "android" || pkg == "com.android.systemui") {
            return HudClassification(HudDecision.IGNORE, 95, HudReason.SYSTEM)
        }
        if (snapshot.isGroupSummary && snapshot.text.isBlank() && snapshot.bigText.isBlank()) {
            return HudClassification(HudDecision.IGNORE, 90, HudReason.GROUP_SUMMARY)
        }

        // Sessões de mídia são orquestradas diretamente pelo MediaSessionManager
        val isMediaStyle = snapshot.template?.contains("MediaStyle", ignoreCase = true) == true
        if (isMediaStyle || pkg in KNOWN_MUSIC_PACKAGES) {
            return HudClassification(HudDecision.IGNORE, 95, HudReason.MEDIA_SESSION)
        }

        // 3. Live Updates nativas do Android 16 (ProgressStyle / requestPromotedOngoing)
        val isProgressStyle = snapshot.template?.contains("ProgressStyle", ignoreCase = true) == true
        if (snapshot.requestPromotedOngoing || isProgressStyle) {
            val decision = applyUserRuleFilter(HudDecision.LIVE, userRule)
            return HudClassification(decision, 95, HudReason.NATIVE_LIVE_UPDATE)
        }

        // 4. Chamadas ativas (CallStyle ou CATEGORY_CALL + ongoing)
        val isCallStyle = snapshot.template?.contains("CallStyle", ignoreCase = true) == true
        val isCallCategory = snapshot.category == Notification.CATEGORY_CALL || snapshot.category == "call"
        if (isCallStyle || (isCallCategory && snapshot.isOngoing)) {
            val decision = applyUserRuleFilter(HudDecision.LIVE, userRule)
            return HudClassification(decision, 95, HudReason.CALL_STYLE)
        }

        // 5. Adapters dedicados de tempo real
        val fullTextLower = snapshot.fullText.lowercase()

        // 5.1 Mobilidade Urbana (Uber, 99, etc.)
        if (pkg in RIDE_PACKAGES || snapshot.category == Notification.CATEGORY_TRANSPORT) {
            if (snapshot.isOngoing || snapshot.actionsCount > 0) {
                val decision = applyUserRuleFilter(HudDecision.LIVE, userRule)
                return HudClassification(decision, 90, HudReason.KNOWN_ADAPTER)
            }
        }

        // 5.2 Delivery de Comida (iFood, Rappi, etc.)
        if (pkg in DELIVERY_PACKAGES) {
            if (snapshot.isOngoing || OUT_FOR_DELIVERY_PHRASES.any { fullTextLower.contains(it) }) {
                val decision = applyUserRuleFilter(HudDecision.LIVE, userRule)
                return HudClassification(decision, 88, HudReason.KNOWN_ADAPTER)
            }
        }

        // 5.3 Navegação GPS curva-a-curva
        if (pkg in NAVIGATION_PACKAGES || snapshot.category == Notification.CATEGORY_NAVIGATION) {
            if (snapshot.isOngoing) {
                val decision = applyUserRuleFilter(HudDecision.LIVE, userRule)
                return HudClassification(decision, 85, HudReason.KNOWN_ADAPTER)
            }
        }

        // 5.4 Encomendas / E-commerce em rota ("Saiu para entrega" -> LIVE sticky)
        val isPackageApp = pkg in PACKAGE_TRACKING_PACKAGES ||
                fullTextLower.contains("rastre") ||
                Regex("""\b[a-zA-Z]{2}\d{9}[a-zA-Z]{2}\b""").containsMatchIn(snapshot.fullText)

        if (isPackageApp) {
            val isOutForDelivery = OUT_FOR_DELIVERY_PHRASES.any { fullTextLower.contains(it) }
            if (isOutForDelivery) {
                val decision = applyUserRuleFilter(HudDecision.LIVE, userRule)
                return HudClassification(decision, 85, HudReason.PACKAGE_TRACKING)
            }
            val isPackageProgress = PACKAGE_UPDATE_PHRASES.any { fullTextLower.contains(it) }
            if (isPackageProgress && config.peekEnabled) {
                val decision = applyUserRuleFilter(HudDecision.PEEK, userRule)
                return HudClassification(decision, 80, HudReason.PACKAGE_TRACKING)
            }
        }

        // 5.5 Cronômetro / Timer do sistema
        val isStopwatchCategory = snapshot.category == Notification.CATEGORY_STOPWATCH || snapshot.category == "stopwatch"
        if (pkg in TIMER_PACKAGES || isStopwatchCategory || snapshot.hasChronometer) {
            if (snapshot.isOngoing) {
                val decision = applyUserRuleFilter(HudDecision.LIVE, userRule)
                return HudClassification(decision, 85, HudReason.CHRONOMETER)
            }
        }

        // 5.6 Fitness (Strava, etc.)
        val isWorkoutCategory = snapshot.category == Notification.CATEGORY_WORKOUT || snapshot.category == "workout"
        if (pkg in FITNESS_PACKAGES || isWorkoutCategory) {
            if (snapshot.isOngoing) {
                val decision = applyUserRuleFilter(HudDecision.LIVE, userRule)
                return HudClassification(decision, 80, HudReason.KNOWN_ADAPTER)
            }
        }

        // 5.7 Ecossistema Tessera Core
        if (pkg == "com.tessera.core") {
            val decision = applyUserRuleFilter(HudDecision.LIVE, userRule)
            return HudClassification(decision, 90, HudReason.KNOWN_ADAPTER)
        }

        // 6. Notificações contínuas gerais (isOngoing)
        if (snapshot.isOngoing) {
            // Detecção de serviço de segundo plano passivo / ruído de sistema
            val isBackgroundService = snapshot.isForegroundService && snapshot.channelImportance <= 2 // LOW ou MIN
            val containsNoisePhrase = BACKGROUND_SERVICE_PHRASES.any { fullTextLower.contains(it) }

            if (isBackgroundService || containsNoisePhrase) {
                // Usuário pode forçar com LIVE_ONLY ou LIVE_AND_PEEK
                if (userRule == HudAppRule.LIVE_ONLY || userRule == HudAppRule.LIVE_AND_PEEK) {
                    return HudClassification(HudDecision.LIVE, 60, HudReason.USER_RULE)
                }
                return HudClassification(HudDecision.IGNORE, 85, HudReason.BACKGROUND_SERVICE)
            }

            // Barra de progresso determinada ativa (ex: download / upload / backup)
            if (snapshot.progress != null && !snapshot.isProgressIndeterminate) {
                val decision = applyUserRuleFilter(HudDecision.LIVE, userRule)
                return HudClassification(decision, 75, HudReason.PROGRESS)
            }

            // Categorias de sistema elegíveis
            if (snapshot.category in listOf(
                    Notification.CATEGORY_PROGRESS,
                    Notification.CATEGORY_STATUS
                ) && snapshot.channelImportance >= 3
            ) {
                val decision = applyUserRuleFilter(HudDecision.LIVE, userRule)
                return HudClassification(decision, 65, HudReason.CATEGORY)
            }

            // Se for ongoing desconhecido sem progresso nem adaptador:
            // Por padrão do novo motor, ruído ongoing passivo não entra na ilha
            if (userRule == HudAppRule.LIVE_ONLY || userRule == HudAppRule.LIVE_AND_PEEK) {
                return HudClassification(HudDecision.LIVE, 60, HudReason.USER_RULE)
            }
            return HudClassification(HudDecision.IGNORE, 70, HudReason.BACKGROUND_SERVICE)
        }

        // 7. Notificações transitórias para PEEK temporário
        if (!config.peekEnabled) {
            return HudClassification(HudDecision.IGNORE, 90, HudReason.LOW_IMPORTANCE)
        }

        // Respeita modo Não Perturbe (DND) do Android
        if (!snapshot.matchesInterruptionFilter) {
            return HudClassification(HudDecision.IGNORE, 95, HudReason.DND)
        }

        // Notificações de mensagens, e-mails importantes ou chamadas perdidas
        val isMessagingStyle = snapshot.template?.contains("MessagingStyle", ignoreCase = true) == true
        val isMessageCategory = snapshot.category in listOf(
            Notification.CATEGORY_MESSAGE,
            Notification.CATEGORY_EMAIL,
            Notification.CATEGORY_MISSED_CALL,
            "msg",
            "email"
        )
        val hasHighPriorityKeyword = HIGH_PRIORITY_PEEK_KEYWORDS.any { fullTextLower.contains(it) }

        if (isMessagingStyle || isMessageCategory || hasHighPriorityKeyword) {
            if (snapshot.channelImportance >= 3) {
                val decision = applyUserRuleFilter(HudDecision.PEEK, userRule)
                return HudClassification(decision, 85, HudReason.MESSAGING)
            }
        }

        // Se o usuário configurou explicitamente PEEK_ONLY ou LIVE_AND_PEEK para este app
        if (userRule == HudAppRule.PEEK_ONLY || userRule == HudAppRule.LIVE_AND_PEEK) {
            if (snapshot.channelImportance >= 3 && snapshot.title.isNotBlank()) {
                return HudClassification(HudDecision.PEEK, 75, HudReason.USER_RULE)
            }
        }

        // Resto: ignorado para manter a ilha limpa e minimalista
        return HudClassification(HudDecision.IGNORE, 65, HudReason.LOW_IMPORTANCE)
    }

    private fun applyUserRuleFilter(
        naturalDecision: HudDecision,
        rule: HudAppRule
    ): HudDecision {
        return when (rule) {
            HudAppRule.OFF -> HudDecision.IGNORE
            HudAppRule.LIVE_ONLY -> if (naturalDecision == HudDecision.PEEK) HudDecision.IGNORE else naturalDecision
            HudAppRule.PEEK_ONLY -> if (naturalDecision == HudDecision.LIVE) HudDecision.PEEK else naturalDecision
            HudAppRule.LIVE_AND_PEEK -> naturalDecision
            HudAppRule.AUTO -> naturalDecision
        }
    }
}
