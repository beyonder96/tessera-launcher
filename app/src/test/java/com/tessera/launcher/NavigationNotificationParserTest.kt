package com.tessera.launcher

import com.tessera.launcher.data.helper.NavigationNotificationParser
import com.tessera.launcher.data.model.ManeuverType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NavigationNotificationParserTest {

    @Test
    fun determineManeuver_identifiesManeuvers() {
        assertEquals(
            ManeuverType.TURN_RIGHT,
            NavigationNotificationParser.determineManeuver("Vire à direita na Av. Paulista")
        )

        assertEquals(
            ManeuverType.TURN_LEFT,
            NavigationNotificationParser.determineManeuver("Curva à esquerda em 50 metros")
        )

        assertEquals(
            ManeuverType.ROUNDABOUT,
            NavigationNotificationParser.determineManeuver("Na rotatória, pegue a 2ª saída")
        )

        assertEquals(
            ManeuverType.U_TURN,
            NavigationNotificationParser.determineManeuver("Faça o retorno quando possível")
        )

        assertEquals(
            ManeuverType.DESTINATION,
            NavigationNotificationParser.determineManeuver("Seu destino está à direita")
        )
    }

    @Test
    fun extractDistanceToManeuver_parsesDistances() {
        val dist1 = NavigationNotificationParser.extractDistanceToManeuver("Em 200 m vire à esquerda", "Em 200 m")
        assertEquals("em 200 m", dist1)

        val dist2 = NavigationNotificationParser.extractDistanceToManeuver("1.5 km - Siga em frente", "1.5 km")
        assertEquals("em 1.5 km", dist2)
    }

    @Test
    fun extractNextStreet_parsesStreetNames() {
        val street1 = NavigationNotificationParser.extractNextStreet("Em 200 m vire na Av. Paulista", "Vire à direita", "na Av. Paulista")
        assertNotNull(street1)
        assertEquals("Av. Paulista", street1)

        val street2 = NavigationNotificationParser.extractNextStreet("Continue na R. Augusta • 15 min", "Siga", "na R. Augusta")
        assertNotNull(street2)
        assertEquals("R. Augusta", street2)
    }

    @Test
    fun extractMetrics_parsesEtaDistanceAndArrival() {
        val fullText = "18 min • 15 km • 14:35"
        assertEquals("18 min", NavigationNotificationParser.extractEta(fullText))
        assertEquals("15 km", NavigationNotificationParser.extractDistanceRemaining(fullText))
        assertEquals("14:35", NavigationNotificationParser.extractArrivalTime(fullText))
    }
}
