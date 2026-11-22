package com.tessera.launcher

import com.tessera.launcher.data.license.LicenseManager
import com.tessera.launcher.data.license.LicenseStatus
import com.tessera.launcher.data.license.LicenseStorage
import com.tessera.launcher.data.license.LicenseTier
import com.tessera.launcher.data.license.TimeProvider
import com.tessera.launcher.data.license.VaultSnapshot
import com.tessera.launcher.ui.state.LauncherUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingAndThemeStateTest {

    private class FakeTimeProvider(var nowMs: Long = 1_700_000_000_000L) : TimeProvider {
        override fun currentTimeMillis(): Long = nowMs
        override fun elapsedRealtimeMillis(): Long = 10_000L
    }

    private class FakeLicenseStorage : LicenseStorage {
        var snapshot: VaultSnapshot? = null
        override fun readSnapshot(): VaultSnapshot? = snapshot
        override fun writeSnapshot(snapshot: VaultSnapshot) {
            this.snapshot = snapshot
        }
        override fun isSealValid(): Boolean = true
    }

    @Test
    fun `AUTO theme resolves to LIGHT during day and AMOLED during night`() {
        val dayState = LauncherUiState(themeMode = "AUTO", isAutoNightNow = false)
        assertEquals("LIGHT", dayState.effectiveThemeMode)
        assertTrue(dayState.isLightMode)

        val nightState = LauncherUiState(themeMode = "AUTO", isAutoNightNow = true)
        assertEquals("AMOLED", nightState.effectiveThemeMode)
        assertFalse(nightState.isLightMode)
    }

    @Test
    fun `explicit themes ignore isAutoNightNow`() {
        val lightAtNight = LauncherUiState(themeMode = "LIGHT", isAutoNightNow = true)
        assertEquals("LIGHT", lightAtNight.effectiveThemeMode)
        assertTrue(lightAtNight.isLightMode)

        val amoledAtDay = LauncherUiState(themeMode = "AMOLED", isAutoNightNow = false)
        assertEquals("AMOLED", amoledAtDay.effectiveThemeMode)
        assertFalse(amoledAtDay.isLightMode)
    }

    @Test
    fun `subscriptionTagLabel shows TESTE on trial, PRO on free, and null for active subscribers`() {
        val trialState = LauncherUiState(
            licenseStatus = LicenseStatus(tier = LicenseTier.TRIAL, hasProAccess = true)
        )
        assertEquals("TESTE", trialState.subscriptionTagLabel)

        val freeState = LauncherUiState(
            licenseStatus = LicenseStatus(tier = LicenseTier.FREE, hasProAccess = false)
        )
        assertEquals("PRO", freeState.subscriptionTagLabel)

        val paidTiers = listOf(
            LicenseTier.PRO_GIFT,
            LicenseTier.PRO_MONTHLY,
            LicenseTier.PRO_SEMESTRAL,
            LicenseTier.PRO_ANNUAL,
            LicenseTier.PRO_LIFETIME,
            LicenseTier.MASTER
        )
        paidTiers.forEach { tier ->
            val state = LauncherUiState(
                licenseStatus = LicenseStatus(
                    tier = tier,
                    hasProAccess = true,
                    isMasterAccess = tier == LicenseTier.MASTER
                )
            )
            assertNull("Expected null tag for tier $tier", state.subscriptionTagLabel)
        }
    }

    @Test
    fun `hasExistingVault is false before first status read and true after initialization`() {
        val storage = FakeLicenseStorage()
        val manager = LicenseManager(storage, FakeTimeProvider())

        assertFalse(manager.hasExistingVault())
        manager.getLicenseStatus()
        assertTrue(manager.hasExistingVault())
    }
}
