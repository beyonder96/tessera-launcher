package com.tessera.launcher.theme

import com.tessera.launcher.ui.theme.apple.AppleTint
import com.tessera.launcher.ui.theme.apple.appleSpring
import com.tessera.launcher.ui.theme.apple.interTracking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.pow

class AppleFoundationTest {

    @Test
    fun testAppleSpringConversion() {
        val response = 0.5f
        val bounce = 0.15f

        val expectedStiffness = (2f * PI.toFloat() / response).pow(2)
        val expectedDamping = 1f - bounce

        val springSpec = appleSpring<Float>(response = response, bounce = bounce)

        assertEquals(expectedStiffness, springSpec.stiffness, 0.01f)
        assertEquals(expectedDamping, springSpec.dampingRatio, 0.01f)
    }

    @Test
    fun testInterTrackingDynamicScale() {
        // Para tamanhos pequenos (ex: 11sp), o tracking deve ser ligeiramente positivo ou próximo de 0
        val trackingSmall = interTracking(11f).value
        assertTrue("Tracking para 11sp deve ser > -0.01em", trackingSmall > -0.01f)

        // Para tamanhos médios (ex: 17sp), o tracking é levemente negativo (-0.01em a -0.015em)
        val trackingBody = interTracking(17f).value
        assertTrue("Tracking para 17sp deve ser negativo", trackingBody < 0f)

        // Para display large (ex: 34sp), o tracking é acentuadamente negativo (~ -0.022em)
        val trackingDisplay = interTracking(34f).value
        assertTrue("Tracking para 34sp deve ser mais negativo que para 17sp", trackingDisplay < trackingBody)

        // Para Hero Clock (112sp), o tracking atinge a assíntota de cerca de -0.0223em
        val trackingHero = interTracking(112f).value
        assertEquals(-0.0223f, trackingHero, 0.001f)
    }

    @Test
    fun testAppleTintResolution() {
        val blue = AppleTint.BLUE
        assertEquals("Azul", blue.displayName)
        assertTrue(blue.dark.value != 0UL)
        assertTrue(blue.light.value != 0UL)

        val fromId = AppleTint.fromId("NOTHING_RED")
        // Como NOTHING_RED não existe diretamente no enum, faz fallback seguro para BLUE
        assertEquals(AppleTint.BLUE, fromId)

        val red = AppleTint.fromId("RED")
        assertEquals(AppleTint.RED, red)
    }
}
