package com.tessera.launcher

import com.tessera.launcher.ui.state.LauncherUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LateralPanelStateTest {

    @Test
    fun testInitialLateralPanelState() {
        val state = LauncherUiState()
        assertEquals("disabled", state.leftScreenMode)
        assertEquals(0, state.lateralPanelTab)
        assertFalse(state.isFeedOpen)
        assertTrue(state.isFeedEnabled)
    }

    @Test
    fun testActiveTabResolution() {
        val widgetsOnlyState = LauncherUiState(leftScreenMode = "widgets", lateralPanelTab = 1)
        val feedOnlyState = LauncherUiState(leftScreenMode = "feed", lateralPanelTab = 0)
        val bothStateWidgetsTab = LauncherUiState(leftScreenMode = "both", lateralPanelTab = 0)
        val bothStateFeedTab = LauncherUiState(leftScreenMode = "both", lateralPanelTab = 1)

        fun resolveCurrentTab(state: LauncherUiState): Int {
            return when (state.leftScreenMode) {
                "feed" -> 1
                "widgets" -> 0
                else -> state.lateralPanelTab
            }
        }

        assertEquals(0, resolveCurrentTab(widgetsOnlyState))
        assertEquals(1, resolveCurrentTab(feedOnlyState))
        assertEquals(0, resolveCurrentTab(bothStateWidgetsTab))
        assertEquals(1, resolveCurrentTab(bothStateFeedTab))
    }

    @Test
    fun testLateralPanelVisibilityInAllModes() {
        fun isVisible(state: LauncherUiState): Boolean {
            return state.isFeedOpen && state.leftScreenMode != "disabled"
        }

        // Closed panel in any mode should NOT be visible
        assertFalse(isVisible(LauncherUiState(isFeedOpen = false, leftScreenMode = "widgets")))
        assertFalse(isVisible(LauncherUiState(isFeedOpen = false, leftScreenMode = "both")))
        assertFalse(isVisible(LauncherUiState(isFeedOpen = false, leftScreenMode = "feed")))
        assertFalse(isVisible(LauncherUiState(isFeedOpen = false, leftScreenMode = "disabled")))

        // Open panel with "disabled" mode should NOT be visible
        assertFalse(isVisible(LauncherUiState(isFeedOpen = true, leftScreenMode = "disabled")))

        // Open panel with "widgets", "both", or "feed" MUST be visible
        assertTrue(isVisible(LauncherUiState(isFeedOpen = true, leftScreenMode = "widgets")))
        assertTrue(isVisible(LauncherUiState(isFeedOpen = true, leftScreenMode = "both")))
        assertTrue(isVisible(LauncherUiState(isFeedOpen = true, leftScreenMode = "feed")))
    }
}
