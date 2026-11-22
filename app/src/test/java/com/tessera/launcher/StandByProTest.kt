package com.tessera.launcher

import androidx.compose.ui.graphics.Color
import com.tessera.launcher.ui.components.StandByClockStyle
import com.tessera.launcher.ui.components.StandByColorPreset
import com.tessera.launcher.ui.components.StandByViewMode
import com.tessera.launcher.ui.state.LauncherUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class StandByProTest {

    @Test
    fun testStandByViewModeResolutionAndOrder() {
        assertEquals(6, StandByViewMode.entries.size)
        assertEquals(StandByViewMode.CLOCK_TALL, StandByViewMode.entries[0])
        assertEquals(StandByViewMode.DASHBOARD, StandByViewMode.entries[1])
        assertEquals(StandByViewMode.PASTEL_MUSIC, StandByViewMode.entries[2])
        assertEquals(StandByViewMode.FULL_MUSIC, StandByViewMode.entries[3])
        assertEquals(StandByViewMode.AGENDA, StandByViewMode.entries[4])
        assertEquals(StandByViewMode.PHOTO, StandByViewMode.entries[5])

        assertEquals(StandByViewMode.CLOCK_TALL, StandByViewMode.fromKey("CLOCK_TALL"))
        assertEquals(StandByViewMode.DASHBOARD, StandByViewMode.fromKey("dashboard"))
        assertEquals(StandByViewMode.PASTEL_MUSIC, StandByViewMode.fromKey("PASTEL_MUSIC"))
        assertEquals(StandByViewMode.FULL_MUSIC, StandByViewMode.fromKey("full_music"))
        assertEquals(StandByViewMode.AGENDA, StandByViewMode.fromKey("AGENDA"))
        assertEquals(StandByViewMode.PHOTO, StandByViewMode.fromKey("photo"))

        // Retrocompatibilidade com chaves de versões anteriores
        assertEquals(StandByViewMode.DASHBOARD, StandByViewMode.fromKey("DUAL"))
        assertEquals(StandByViewMode.FULL_MUSIC, StandByViewMode.fromKey("music"))
        assertEquals(StandByViewMode.AGENDA, StandByViewMode.fromKey("EVENTS"))
        assertEquals(StandByViewMode.CLOCK_TALL, StandByViewMode.fromKey("pomodoro"))
        assertEquals(StandByViewMode.CLOCK_TALL, StandByViewMode.fromKey("UNKNOWN"))
    }

    @Test
    fun testStandByClockStyleCycle() {
        assertEquals(StandByClockStyle.EDITORIAL, StandByClockStyle.FLIP.next())
        assertEquals(StandByClockStyle.MINIMAL, StandByClockStyle.EDITORIAL.next())
        assertEquals(StandByClockStyle.FLIP, StandByClockStyle.MINIMAL.next())
        assertEquals(StandByClockStyle.MINIMAL, StandByClockStyle.fromKey("minimal"))
        assertEquals(StandByClockStyle.FLIP, StandByClockStyle.fromKey("invalid"))
    }

    @Test
    fun testStandByColorPresetResolutionAndCycle() {
        val accent = Color(0xFFFF5500)
        assertEquals(Color.White, StandByColorPreset.WHITE.resolveColor(accent))
        assertEquals(accent, StandByColorPreset.ACCENT.resolveColor(accent))
        assertEquals(Color(0xFFFFB040), StandByColorPreset.AMBER.resolveColor(accent))
        assertEquals(Color(0xFF34D399), StandByColorPreset.EMERALD.resolveColor(accent))
        assertEquals(Color(0xFF38BDF8), StandByColorPreset.CYAN.resolveColor(accent))

        assertEquals(StandByColorPreset.ACCENT, StandByColorPreset.WHITE.next())
        assertEquals(StandByColorPreset.WHITE, StandByColorPreset.CYAN.next())
    }

    @Test
    fun testLauncherUiStateStandByDefaults() {
        val state = LauncherUiState()
        assertEquals("CLOCK_TALL", state.standByViewMode)
        assertEquals("FLIP", state.standByClockStyle)
        assertEquals("WHITE", state.standByColorPreset)
        assertFalse(state.isStandByNotificationPrivacyEnabled)
        assertFalse(state.isPhotoStandByOpen)
    }
}
