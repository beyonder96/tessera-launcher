package com.tessera.launcher

import com.tessera.launcher.ui.motion.MotionMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionMathTest {

    @Test
    fun rubberBandIsLinearUpToLimitAndBoundedAbove() {
        assertEquals(0f, MotionMath.rubberBand(0f, 75f), 0.001f)
        assertEquals(40f, MotionMath.rubberBand(40f, 75f), 0.001f)
        assertEquals(75f, MotionMath.rubberBand(75f, 75f), 0.001f)

        val slight = MotionMath.rubberBand(105f, 75f, factor = 0.55f)
        val hard = MotionMath.rubberBand(300f, 75f, factor = 0.55f)
        val extreme = MotionMath.rubberBand(10000f, 75f, factor = 0.55f)

        assertTrue("Rubber band must be above limit", slight > 75f)
        assertTrue("Rubber band must be monotonic", hard > slight)
        assertTrue("Rubber band must stay bounded", extreme < 75f + 75f * 0.86f)
    }

    @Test
    fun drawerCascadeRespectsBoundsAndMonotonicity() {
        assertEquals(0f, MotionMath.drawerCascadeProgress(0f, 0, 18, 12), 0.001f)
        assertEquals(1f, MotionMath.drawerCascadeProgress(1f, 5, 18, 12), 0.001f)

        // Item 0 always leads or equals later items
        val p = 0.35f
        var prev = 1.0f
        for (i in 0 until 12) {
            val cur = MotionMath.drawerCascadeProgress(p, i, 18, 12)
            assertTrue("Item $i should not lead item ${i - 1}", cur <= prev + 0.001f)
            assertTrue("Progress in [0,1]", cur in 0f..1f)
            prev = cur
        }

        // Items beyond staggerMaxItems track raw drawerProgress
        assertEquals(0.42f, MotionMath.drawerCascadeProgress(0.42f, 15, 18, 12), 0.001f)
        // Zero stagger (PRECISE) tracks raw drawerProgress
        assertEquals(0.42f, MotionMath.drawerCascadeProgress(0.42f, 2, 0, 0), 0.001f)
    }

    @Test
    fun backProgressToScaleInterpolatesCleanly() {
        assertEquals(1f, MotionMath.backProgressToScale(0f, 0.90f), 0.001f)
        assertEquals(0.95f, MotionMath.backProgressToScale(0.5f, 0.90f), 0.001f)
        assertEquals(0.90f, MotionMath.backProgressToScale(1f, 0.90f), 0.001f)
    }
}
