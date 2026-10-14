package com.tessera.launcher

import com.tessera.launcher.data.helper.DeliveryNotificationParser
import com.tessera.launcher.data.model.DeliveryStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DeliveryNotificationParserTest {

    @Test
    fun parsePin_extractsPinCodes() {
        val text1 = "Seu pedido do McDonald's está a caminho • PIN: 4821"
        assertEquals("4821", DeliveryNotificationParser.extractPin(text1))

        val text2 = "Código de entrega: 9012 • Informe ao entregador"
        assertEquals("9012", DeliveryNotificationParser.extractPin(text2))

        val text3 = "iFood • Código 3344"
        assertEquals("3344", DeliveryNotificationParser.extractPin(text3))
    }

    @Test
    fun determineDeliveryStage_identifiesStages() {
        assertEquals(
            DeliveryStage.CONFIRMED,
            DeliveryNotificationParser.determineDeliveryStage("Pedido confirmado pelo restaurante!")
        )

        assertEquals(
            DeliveryStage.PREPARING,
            DeliveryNotificationParser.determineDeliveryStage("O restaurante está preparando seu pedido na cozinha.")
        )

        assertEquals(
            DeliveryStage.ON_THE_WAY,
            DeliveryNotificationParser.determineDeliveryStage("O entregador saiu para entrega e está a caminho.")
        )

        assertEquals(
            DeliveryStage.DELIVERED,
            DeliveryNotificationParser.determineDeliveryStage("Pedido entregue! Aproveite seu almoço.")
        )
    }

    @Test
    fun extractEtaMinutes_parsesTimeWindow() {
        val text = "Previsão de entrega: 35 min"
        assertEquals(35, DeliveryNotificationParser.extractEtaMinutes(text))

        val textRange = "Chega em 20-30 min"
        assertEquals(20, DeliveryNotificationParser.extractEtaMinutes(textRange))
    }

    @Test
    fun extractStoreName_cleansAppName() {
        val store1 = DeliveryNotificationParser.extractStoreName("iFood • Burger King", "Pedido a caminho", null, "iFood")
        assertEquals("Burger King", store1)

        val store2 = DeliveryNotificationParser.extractStoreName("Habib's", "Preparando pedido", null, "iFood")
        assertEquals("Habib's", store2)
    }
}
