package com.tessera.launcher

import com.tessera.launcher.data.license.BillingConfig
import com.tessera.launcher.data.license.LicenseActivationResult
import com.tessera.launcher.data.license.LicenseKeyType
import com.tessera.launcher.data.license.LicenseManager
import com.tessera.launcher.data.license.LicenseStorage
import com.tessera.launcher.data.license.LicenseTier
import com.tessera.launcher.data.license.TimeProvider
import com.tessera.launcher.data.license.VaultSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LicenseManagerTest {

    private class FakeTimeProvider(var nowMs: Long, var elapsedMs: Long = 10_000L) : TimeProvider {
        override fun currentTimeMillis(): Long = nowMs
        override fun elapsedRealtimeMillis(): Long = elapsedMs
    }

    private class FakeLicenseStorage : LicenseStorage {
        var snapshot: VaultSnapshot? = null
        var sealValid: Boolean = true
        override fun readSnapshot(): VaultSnapshot? = snapshot
        override fun writeSnapshot(snapshot: VaultSnapshot) {
            this.snapshot = snapshot
            this.sealValid = true
        }
        override fun isSealValid(): Boolean = sealValid
    }

    private val dayMs = 24L * 60L * 60L * 1000L

    @Test
    fun `fresh install initializes 3 day trial with pro access and without master AI`() {
        val time = FakeTimeProvider(nowMs = 1_700_000_000_000L)
        val storage = FakeLicenseStorage()
        val manager = LicenseManager(storage, time)

        val status = manager.getLicenseStatus()
        assertEquals(LicenseTier.TRIAL, status.tier)
        assertTrue(status.hasProAccess)
        assertFalse(status.isMasterAccess)
        assertEquals(3, status.daysRemaining)
        assertTrue(status.isTrialActive)
        assertFalse(status.isTrialExpired)
    }

    @Test
    fun `trial transitions to free soft lock after 72 hours`() {
        val time = FakeTimeProvider(nowMs = 1_700_000_000_000L)
        val storage = FakeLicenseStorage()
        val manager = LicenseManager(storage, time)

        manager.getLicenseStatus()
        time.nowMs += BillingConfig.TRIAL_DURATION_MS + 1_000L
        time.elapsedMs += BillingConfig.TRIAL_DURATION_MS + 1_000L

        val expired = manager.getLicenseStatus()
        assertEquals(LicenseTier.FREE, expired.tier)
        assertFalse(expired.hasProAccess)
        assertFalse(expired.isMasterAccess)
        assertTrue(expired.isTrialExpired)
    }

    @Test
    fun `gift keys strictly accept 3 to 7 days and reject out of bounds`() {
        val time = FakeTimeProvider(nowMs = 1_700_000_000_000L)
        val storage = FakeLicenseStorage()
        val manager = LicenseManager(storage, time)

        for (days in 3..7) {
            val key = LicenseManager.generateKey(LicenseKeyType.GIFT, giftDays = days)
            val result = manager.activateKey(key)
            assertTrue("Expected Success for $days days", result is LicenseActivationResult.Success)
            val status = manager.getLicenseStatus()
            assertEquals(LicenseTier.PRO_GIFT, status.tier)
            assertTrue(status.hasProAccess)
            assertFalse(status.isMasterAccess)
        }

        // Forging 2 or 8 days with valid HMAC should still be rejected cleanly by validation
        val forged2 = "2ABC"
        val forged2Sig = LicenseManager.computeSignature(LicenseKeyType.GIFT, forged2)
        assertTrue(manager.activateKey("TESS-GIFT-$forged2-$forged2Sig") is LicenseActivationResult.InvalidKey)

        val forged8 = "8ABC"
        val forged8Sig = LicenseManager.computeSignature(LicenseKeyType.GIFT, forged8)
        assertTrue(manager.activateKey("TESS-GIFT-$forged8-$forged8Sig") is LicenseActivationResult.InvalidKey)

        var threwLow = false
        try {
            LicenseManager.generateKey(LicenseKeyType.GIFT, giftDays = 2)
        } catch (_: IllegalArgumentException) {
            threwLow = true
        }
        assertTrue(threwLow)

        var threwHigh = false
        try {
            LicenseManager.generateKey(LicenseKeyType.GIFT, giftDays = 8)
        } catch (_: IllegalArgumentException) {
            threwHigh = true
        }
        assertTrue(threwHigh)
    }

    @Test
    fun `all paid tiers unlock pro without master and master unlocks AI`() {
        val time = FakeTimeProvider(nowMs = 1_700_000_000_000L)
        val manager = LicenseManager(FakeLicenseStorage(), time)

        val cases = listOf(
            Triple(LicenseKeyType.MENS, LicenseTier.PRO_MONTHLY, false),
            Triple(LicenseKeyType.SEMS, LicenseTier.PRO_SEMESTRAL, false),
            Triple(LicenseKeyType.ANUL, LicenseTier.PRO_ANNUAL, false),
            Triple(LicenseKeyType.LIFE, LicenseTier.PRO_LIFETIME, false),
            Triple(LicenseKeyType.MSTR, LicenseTier.MASTER, true)
        )

        for ((keyType, expectedTier, expectedMaster) in cases) {
            val key = LicenseManager.generateKey(keyType)
            val normalizedNoDashes = key.replace("-", "").lowercase()
            val result = manager.activateKey(normalizedNoDashes)
            assertTrue(result is LicenseActivationResult.Success)
            val status = manager.getLicenseStatus()
            assertEquals(expectedTier, status.tier)
            assertTrue(status.hasProAccess)
            assertEquals(expectedMaster, status.isMasterAccess)
        }
    }

    @Test
    fun `tampered signature is rejected`() {
        val manager = LicenseManager(FakeLicenseStorage(), FakeTimeProvider(1_700_000_000_000L))
        val validKey = LicenseManager.generateKey(LicenseKeyType.LIFE)
        val lastChar = validKey.last()
        val altChar = if (lastChar == 'A') 'B' else 'A'
        val badKey = validKey.dropLast(1) + altChar

        assertTrue(manager.activateKey(badKey) is LicenseActivationResult.InvalidKey)
    }

    @Test
    fun `reusing expired temporary key returns ExpiredKey`() {
        val time = FakeTimeProvider(nowMs = 1_700_000_000_000L)
        val manager = LicenseManager(FakeLicenseStorage(), time)

        val giftKey = LicenseManager.generateKey(LicenseKeyType.GIFT, giftDays = 3)
        assertTrue(manager.activateKey(giftKey) is LicenseActivationResult.Success)

        time.nowMs += 4L * dayMs
        time.elapsedMs += 4L * dayMs
        assertFalse(manager.getLicenseStatus().hasProAccess)

        assertTrue(manager.activateKey(giftKey) is LicenseActivationResult.ExpiredKey)
    }

    @Test
    fun `clock rollback revokes timed access and vault tamper resets to free`() {
        val time = FakeTimeProvider(nowMs = 1_700_000_000_000L)
        val storage = FakeLicenseStorage()
        val manager = LicenseManager(storage, time)

        // Advance 2 days
        manager.getLicenseStatus()
        time.nowMs += 2L * dayMs
        time.elapsedMs += 2L * dayMs
        assertTrue(manager.getLicenseStatus().hasProAccess)

        // Wind clock backward by 4 days
        time.nowMs -= 4L * dayMs
        val rolledBack = manager.getLicenseStatus()
        assertFalse(rolledBack.hasProAccess)
        assertTrue(rolledBack.isClockTampered)
        assertEquals(LicenseTier.FREE, rolledBack.tier)

        // Restore clock + trigger XML vault tamper
        time.nowMs += 4L * dayMs
        storage.sealValid = false
        val tampered = manager.getLicenseStatus()
        assertFalse(tampered.hasProAccess)
        assertTrue(tampered.isVaultTampered)
        assertEquals(LicenseTier.FREE, tampered.tier)
    }
}
