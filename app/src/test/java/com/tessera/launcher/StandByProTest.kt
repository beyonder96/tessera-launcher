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
    fun testStandByViewModeResolution() {
        assertEquals(StandByViewMode.DUAL, StandByViewMode.fromKey("DUAL"))
        assertEquals(StandByViewMode.MUSIC, StandByViewMode.fromKey("music"))
        assertEquals(StandByViewMode.EVENTS, StandByViewMode.fromKey("EVENTS"))
        assertEquals(StandByViewMode.POMODORO, StandByViewMode.fromKey("pomodoro"))
        assertEquals(StandByViewMode.DUAL, StandByViewMode.fromKey("UNKNOWN"))
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
        assertEquals("DUAL", state.standByViewMode)
        assertEquals("FLIP", state.standByClockStyle)
        assertEquals("WHITE", state.standByColorPreset)
        assertFalse(state.isStandByNotificationPrivacyEnabled)
        assertFalse(state.isPhotoStandByOpen)
    }
}
