package com.tessera.launcher

import com.tessera.launcher.data.hud.HudRanker
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType
import com.tessera.launcher.data.model.PackageActivityDetails
import com.tessera.launcher.data.model.PackageStage
import com.tessera.launcher.data.model.RideActivityDetails
import com.tessera.launcher.data.model.RideStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HudRankerTest {

    @Test
    fun testCallRanksHigherThanRide() {
        val call = LiveActivityInfo(
            id = "call_1",
            packageName = "com.whatsapp",
            type = LiveActivityType.CALL,
            title = "Chamada"
        )
        val ride = LiveActivityInfo(
            id = "ride_1",
            packageName = "com.ubercab",
            type = LiveActivityType.RIDE,
            title = "Uber"
        )

        val ranked = HudRanker.rank(listOf(ride, call))
        assertEquals("call_1", ranked.primary?.id)
        assertEquals("ride_1", ranked.secondary?.id)
    }

    @Test
    fun testRideUrgencyBonusWhenArrived() {
        val normalRide = LiveActivityInfo(
            id = "ride_normal",
            packageName = "com.ubercab",
            type = LiveActivityType.RIDE,
            title = "Uber",
            rideDetails = RideActivityDetails(
                appName = "Uber",
                stage = RideStage.ARRIVING,
                etaMinutes = 15
            )
        )

        val urgentRide = LiveActivityInfo(
            id = "ride_urgent",
            packageName = "com.taxis99",
            type = LiveActivityType.RIDE,
            title = "99",
            rideDetails = RideActivityDetails(
                appName = "99",
                stage = RideStage.ARRIVED,
                etaMinutes = 1
            )
        )

        val scoreNormal = HudRanker.calculateScore(normalRide)
        val scoreUrgent = HudRanker.calculateScore(urgentRide)

        assertTrue(scoreUrgent > scoreNormal)
    }

    @Test
    fun testPackageOutForDeliveryRanksAboveGeneralProgress() {
        val pkg = LiveActivityInfo(
            id = "pkg_1",
            packageName = "com.mercadolibre",
            type = LiveActivityType.PACKAGE,
            title = "Mercado Livre",
            packageDetails = PackageActivityDetails(
                appName = "Mercado Livre",
                stage = PackageStage.OUT_FOR_DELIVERY,
                headline = "Saiu para entrega"
            )
        )

        val download = LiveActivityInfo(
            id = "download_1",
            packageName = "com.android.vending",
            type = LiveActivityType.PROGRESS,
            title = "Download",
            progress = 45
        )

        val ranked = HudRanker.rank(listOf(download, pkg))
        assertEquals("pkg_1", ranked.primary?.id)
        assertEquals("download_1", ranked.secondary?.id)
    }
}
