package com.tessera.launcher

import com.tessera.launcher.data.helper.RideNotificationParser
import com.tessera.launcher.data.model.RideStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RideNotificationParserTest {

    @Test
    fun parseEtaMinutes_extractsCorrectValue() {
        val text1 = "UberX • 1 min until pickup"
        assertEquals(1, RideNotificationParser.extractEtaMinutes(text1))

        val text2 = "Motorista chegando em 3 min • Anderson"
        assertEquals(3, RideNotificationParser.extractEtaMinutes(text2))

        val text3 = "99Pop • Chega em 5 minutos • Honda Civic"
        assertEquals(5, RideNotificationParser.extractEtaMinutes(text3))
    }

    @Test
    fun parseLicensePlate_supportsVariousFormats() {
        // Internacional como na imagem de referência do usuário
        val intlText = "Anderson is arriving soon • 7NLR004 • Silver Honda Civic"
        assertEquals("7NLR004", RideNotificationParser.extractLicensePlate(intlText))

        // Mercosul
        val mercosulText = "João está a caminho • BRA2E19 • Chevrolet Onix Branco"
        assertEquals("BRA2E19", RideNotificationParser.extractLicensePlate(mercosulText))

        // Padrão tradicional brasileiro
        val traditionalText = "Carlos • ABC-1234 • Fiat Argo Prata"
        assertEquals("ABC-1234", RideNotificationParser.extractLicensePlate(traditionalText))
    }

    @Test
    fun determineRideStage_identifiesCorrectStages() {
        assertEquals(
            RideStage.SEARCHING,
            RideNotificationParser.determineRideStage("Procurando motoristas próximos para sua viagem...")
        )

        assertEquals(
            RideStage.ARRIVED,
            RideNotificationParser.determineRideStage("Seu motorista chegou no local de embarque!")
        )

        assertEquals(
            RideStage.IN_TRIP,
            RideNotificationParser.determineRideStage("Viagem em andamento até Av. Paulista, 1000")
        )

        assertEquals(
            RideStage.ARRIVING,
            RideNotificationParser.determineRideStage("Anderson está a caminho • 1 min")
        )
    }

    @Test
    fun extractDriverName_parsesArrivingPattern() {
        val text = "Anderson is arriving soon"
        assertEquals("Anderson", RideNotificationParser.extractDriverName(text, "Uber", text))

        val textPt = "Anderson está a caminho"
        assertEquals("Anderson", RideNotificationParser.extractDriverName(textPt, "Uber", textPt))
    }

    @Test
    fun extractVehicleModel_parsesColorAndModel() {
        val text = "7NLR004 • Silver Honda Civic"
        val model = RideNotificationParser.extractVehicleModel(text, "7NLR004", "Anderson")
        assertNotNull(model)
        assertEquals("Silver Honda Civic", model)

        val textPt = "BRA2E19 • Honda Civic Prata • Anderson"
        val modelPt = RideNotificationParser.extractVehicleModel(textPt, "BRA2E19", "Anderson")
        assertNotNull(modelPt)
        assertEquals("Honda Civic Prata", modelPt)
    }
}
