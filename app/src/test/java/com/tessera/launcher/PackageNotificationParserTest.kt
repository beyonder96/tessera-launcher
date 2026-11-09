package com.tessera.launcher

import com.tessera.launcher.data.helper.PackageNotificationParser
import com.tessera.launcher.data.model.PackageStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageNotificationParserTest {

    @Test
    fun testDetermineStageOutForDelivery() {
        assertEquals(
            PackageStage.OUT_FOR_DELIVERY,
            PackageNotificationParser.determineStage("Seu pedido do Mercado Livre saiu para entrega e chega hoje!")
        )
        assertEquals(
            PackageStage.OUT_FOR_DELIVERY,
            PackageNotificationParser.determineStage("Shopee: O motorista está a caminho do seu endereço")
        )
    }

    @Test
    fun testDetermineStageInTransitAndShipped() {
        assertEquals(
            PackageStage.IN_TRANSIT,
            PackageNotificationParser.determineStage("Objeto em trânsito entre Unidade de Tratamento e Distribuição")
        )
        assertEquals(
            PackageStage.SHIPPED,
            PackageNotificationParser.determineStage("Seu pedido foi postado pelo vendedor na transportadora")
        )
    }

    @Test
    fun testDetermineStagePickupAndDelivered() {
        assertEquals(
            PackageStage.PICKUP_READY,
            PackageNotificationParser.determineStage("Objeto disponível para retirada na agência dos Correios")
        )
        assertEquals(
            PackageStage.DELIVERED,
            PackageNotificationParser.determineStage("Entrega concluída com sucesso! Pacote entregue ao destinatário")
        )
    }

    @Test
    fun testDetermineStageIssue() {
        assertEquals(
            PackageStage.ISSUE,
            PackageNotificationParser.determineStage("Tentativa de entrega não realizada - destinatário ausente")
        )
    }

    @Test
    fun testExtractTrackingCode() {
        assertEquals(
            "QB123456789BR",
            PackageNotificationParser.extractTrackingCode("Seu código de rastreamento é QB123456789BR. Acompanhe a entrega.")
        )
        assertEquals(
            "NL987654321BR",
            PackageNotificationParser.extractTrackingCode("Objeto NL987654321BR saiu para entrega")
        )
        assertNull(
            PackageNotificationParser.extractTrackingCode("Nenhum código presente no texto de compra")
        )
    }

    @Test
    fun testExtractEtaText() {
        val etaToday = PackageNotificationParser.extractEtaText(
            "Chega hoje até às 18h no seu endereço",
            PackageStage.OUT_FOR_DELIVERY
        )
        assertNotNull(etaToday)
        assertTrue(etaToday!!.contains("Chega hoje"))

        val etaTomorrow = PackageNotificationParser.extractEtaText(
            "Previsão de entrega: chega amanhã",
            PackageStage.OUT_FOR_DELIVERY
        )
        assertEquals("Chega amanhã", etaTomorrow)

        val etaDelivered = PackageNotificationParser.extractEtaText(
            "Pedido entregue às 14:30",
            PackageStage.DELIVERED
        )
        assertEquals("Pedido entregue", etaDelivered)
    }
}
