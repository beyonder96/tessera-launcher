package com.tessera.launcher

import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.tokens.LateralSpatialMath
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

    @Test
    fun testLateralSpatialMathVelocityClamping() {
        val screenWidthPx = 1080f
        // Extreme positive velocity must clamp to MAX_NORMALIZED_VELOCITY
        val clampedHigh = LateralSpatialMath.clampNormalizedVelocity(9000f, screenWidthPx)
        assertEquals(LateralSpatialMath.MAX_NORMALIZED_VELOCITY, clampedHigh, 0.001f)

        // Extreme negative velocity must clamp to -MAX_NORMALIZED_VELOCITY
        val clampedLow = LateralSpatialMath.clampNormalizedVelocity(-9000f, screenWidthPx)
        assertEquals(-LateralSpatialMath.MAX_NORMALIZED_VELOCITY, clampedLow, 0.001f)

        // Moderate velocity must scale linearly
        val normal = LateralSpatialMath.clampNormalizedVelocity(540f, screenWidthPx)
        assertEquals(0.5f, normal, 0.001f)
    }

    @Test
    fun testLateralSpatialMathHomeTransforms() {
        val screenWidthPx = 1080f

        // Progress = 0 (Home em repouso): sem escala reduzida, sem paralaxe, opacidade 100%
        assertEquals(1.0f, LateralSpatialMath.homeScale(0f), 0.001f)
        assertEquals(0f, LateralSpatialMath.homeParallaxX(0f, screenWidthPx), 0.001f)
        assertEquals(1.0f, LateralSpatialMath.homeAlpha(0f), 0.001f)

        // Progress = 1 (Painel Lateral totalmente acoplado): Home recua suavemente com paralaxe
        assertEquals(0.915f, LateralSpatialMath.homeScale(1f), 0.001f)
        assertEquals(screenWidthPx * 0.24f, LateralSpatialMath.homeParallaxX(1f, screenWidthPx), 0.001f)
        assertEquals(0.18f, LateralSpatialMath.homeAlpha(1f), 0.001f)
    }

    @Test
    fun testLateralSpatialMathPanelTransforms() {
        // Progress = 0: Cantos dinâmicos em 100% (32dp), escala inicial 0.945f
        assertEquals(1.0f, LateralSpatialMath.panelCornerFraction(0f), 0.001f)
        assertEquals(0.945f, LateralSpatialMath.panelScale(0f), 0.001f)
        assertEquals(0.38f, LateralSpatialMath.panelAlpha(0f), 0.001f)

        // Progress = 1: Cantos dinâmicos zerados (0dp, tela cheia perfeita), escala 1.0f, alpha 1.0f
        assertEquals(0f, LateralSpatialMath.panelCornerFraction(1f), 0.001f)
        assertEquals(1.0f, LateralSpatialMath.panelScale(1f), 0.001f)
        assertEquals(1.0f, LateralSpatialMath.panelAlpha(1f), 0.001f)
    }

    @Test
    fun testLateralSpatialMathCascadeMonotonicity() {
        // Em qualquer progresso, itens anteriores devem ter progresso de entrada igual ou superior aos posteriores
        val progressValues = listOf(0.1f, 0.25f, 0.5f, 0.75f, 1.0f)
        for (p in progressValues) {
            var previousItemProgress = 1.0f
            for (index in 0..6) {
                val itemProgress = LateralSpatialMath.cascadeItemProgress(p, index)
                assertTrue("Item $index deve ter progresso <= item anterior em p=$p", itemProgress <= previousItemProgress + 0.001f)
                assertTrue("Item progresso deve estar no range [0, 1]", itemProgress in 0f..1.0001f)
                previousItemProgress = itemProgress
            }
        }
    }
}
