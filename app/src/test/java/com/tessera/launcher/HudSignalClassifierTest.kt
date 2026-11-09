package com.tessera.launcher

import android.app.Notification
import com.tessera.launcher.data.hud.HudNotificationSnapshot
import com.tessera.launcher.data.hud.HudSignalClassifier
import com.tessera.launcher.data.model.HudAppRule
import com.tessera.launcher.data.model.HudConfig
import com.tessera.launcher.data.model.HudDecision
import com.tessera.launcher.data.model.HudReason
import org.junit.Assert.assertEquals
import org.junit.Test

class HudSignalClassifierTest {

    private val defaultConfig = HudConfig()

    @Test
    fun testNativeLiveUpdatePromotedOngoingClassifiedAsLive() {
        val snapshot = HudNotificationSnapshot(
            key = "key_uber_live",
            packageName = "com.ubercab",
            appName = "Uber",
            isOngoing = true,
            requestPromotedOngoing = true,
            template = "android.app.Notification\$ProgressStyle",
            title = "UberX a caminho",
            text = "Chega em 3 min"
        )

        val result = HudSignalClassifier.classify(snapshot, defaultConfig)
        assertEquals(HudDecision.LIVE, result.decision)
        assertEquals(HudReason.NATIVE_LIVE_UPDATE, result.reason)
    }

    @Test
    fun testCallStyleClassifiedAsLive() {
        val snapshot = HudNotificationSnapshot(
            key = "key_call",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            isOngoing = true,
            template = "android.app.Notification\$CallStyle",
            title = "Chamada em andamento",
            text = "02:15"
        )

        val result = HudSignalClassifier.classify(snapshot, defaultConfig)
        assertEquals(HudDecision.LIVE, result.decision)
        assertEquals(HudReason.CALL_STYLE, result.reason)
    }

    @Test
    fun testRideNotificationClassifiedAsLive() {
        val snapshot = HudNotificationSnapshot(
            key = "key_99",
            packageName = "com.taxis99",
            appName = "99",
            isOngoing = true,
            title = "Motorista chegando",
            text = "Carro Honda Civic • 2 min",
            actionsCount = 2
        )

        val result = HudSignalClassifier.classify(snapshot, defaultConfig)
        assertEquals(HudDecision.LIVE, result.decision)
        assertEquals(HudReason.KNOWN_ADAPTER, result.reason)
    }

    @Test
    fun testDeliveryNotificationClassifiedAsLive() {
        val snapshot = HudNotificationSnapshot(
            key = "key_ifood",
            packageName = "br.com.brainweb.ifood",
            appName = "iFood",
            isOngoing = true,
            title = "Pedido a caminho",
            text = "Entregador chegando em 8 minutos"
        )

        val result = HudSignalClassifier.classify(snapshot, defaultConfig)
        assertEquals(HudDecision.LIVE, result.decision)
        assertEquals(HudReason.KNOWN_ADAPTER, result.reason)
    }

    @Test
    fun testPackageOutForDeliveryClassifiedAsLiveSticky() {
        val snapshot = HudNotificationSnapshot(
            key = "key_ml",
            packageName = "com.mercadolibre",
            appName = "Mercado Livre",
            isOngoing = false,
            title = "Seu pacote saiu para entrega!",
            text = "Chegará hoje até às 18h no seu endereço"
        )

        val result = HudSignalClassifier.classify(snapshot, defaultConfig)
        assertEquals(HudDecision.LIVE, result.decision)
        assertEquals(HudReason.PACKAGE_TRACKING, result.reason)
    }

    @Test
    fun testPackageShippedClassifiedAsPeek() {
        val snapshot = HudNotificationSnapshot(
            key = "key_shopee",
            packageName = "com.shopee.br",
            appName = "Shopee",
            isOngoing = false,
            title = "Pedido enviado",
            text = "Seu pacote está em trânsito entre centros"
        )

        val result = HudSignalClassifier.classify(snapshot, defaultConfig)
        assertEquals(HudDecision.PEEK, result.decision)
        assertEquals(HudReason.PACKAGE_TRACKING, result.reason)
    }

    @Test
    fun testBackgroundServiceNoiseIgnored() {
        val snapshot = HudNotificationSnapshot(
            key = "key_vpn",
            packageName = "com.wireguard.android",
            appName = "WireGuard",
            isOngoing = true,
            isForegroundService = true,
            channelImportance = 2, // LOW
            title = "WireGuard",
            text = "Conectado à VPN • Em execução em segundo plano"
        )

        val result = HudSignalClassifier.classify(snapshot, defaultConfig)
        assertEquals(HudDecision.IGNORE, result.decision)
        assertEquals(HudReason.BACKGROUND_SERVICE, result.reason)
    }

    @Test
    fun testMessagingNotificationClassifiedAsPeek() {
        val snapshot = HudNotificationSnapshot(
            key = "key_msg",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            isOngoing = false,
            template = "android.app.Notification\$MessagingStyle",
            channelImportance = 4, // HIGH
            title = "Mariana",
            text = "Nos vemos às 19h no restaurante!"
        )

        val result = HudSignalClassifier.classify(snapshot, defaultConfig)
        assertEquals(HudDecision.PEEK, result.decision)
        assertEquals(HudReason.MESSAGING, result.reason)
    }

    @Test
    fun testDndBlocksPeek() {
        val snapshot = HudNotificationSnapshot(
            key = "key_msg_dnd",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            isOngoing = false,
            matchesInterruptionFilter = false, // Silenciado pelo Não Perturbe
            channelImportance = 4,
            title = "Mariana",
            text = "Oi"
        )

        val result = HudSignalClassifier.classify(snapshot, defaultConfig)
        assertEquals(HudDecision.IGNORE, result.decision)
        assertEquals(HudReason.DND, result.reason)
    }

    @Test
    fun testUserRuleOffOverridesEverything() {
        val config = HudConfig(
            appRules = mapOf("com.ubercab" to HudAppRule.OFF)
        )
        val snapshot = HudNotificationSnapshot(
            key = "key_uber",
            packageName = "com.ubercab",
            appName = "Uber",
            isOngoing = true,
            title = "UberX",
            text = "Chegando"
        )

        val result = HudSignalClassifier.classify(snapshot, config)
        assertEquals(HudDecision.IGNORE, result.decision)
        assertEquals(HudReason.USER_RULE, result.reason)
    }

    @Test
    fun testUserRuleLiveOnlySilencesPeek() {
        val config = HudConfig(
            appRules = mapOf("com.whatsapp" to HudAppRule.LIVE_ONLY)
        )
        val snapshot = HudNotificationSnapshot(
            key = "key_msg",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            isOngoing = false,
            template = "android.app.Notification\$MessagingStyle",
            title = "Mariana",
            text = "Oi"
        )

        val result = HudSignalClassifier.classify(snapshot, config)
        assertEquals(HudDecision.IGNORE, result.decision)
    }
}
