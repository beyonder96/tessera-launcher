package com.tessera.launcher

import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.components.formatMediaDuration
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaPlaybackCalculationTest {

    @Test
    fun testPositionCalculationWhenPaused() {
        val info = MediaPlaybackInfo(
            title = "Test Song",
            isPlaying = false,
            positionMs = 30_000L,
            durationMs = 180_000L,
            playbackSpeed = 1.0f,
            lastPositionUpdateTime = 100_000L
        )

        // Quando pausado, o tempo calculado deve permanecer inalterado mesmo se o tempo de hardware avançar
        val calculated = info.getCalculatedPositionMs(currentTimeRealtimeMs = 150_000L)
        assertEquals(30_000L, calculated)
    }

    @Test
    fun testPositionCalculationWhenPlayingNormalSpeed() {
        val info = MediaPlaybackInfo(
            title = "Test Song",
            isPlaying = true,
            positionMs = 15_000L,
            durationMs = 200_000L,
            playbackSpeed = 1.0f,
            lastPositionUpdateTime = 50_000L
        )

        // 10 segundos depois em velocidade 1.0x -> posição deve avançar 10.000ms
        val calculated = info.getCalculatedPositionMs(currentTimeRealtimeMs = 60_000L)
        assertEquals(25_000L, calculated)
    }

    @Test
    fun testPositionCalculationWhenPlayingFasterSpeed() {
        val info = MediaPlaybackInfo(
            title = "Podcast Episode",
            isPlaying = true,
            positionMs = 60_000L,
            durationMs = 600_000L,
            playbackSpeed = 1.5f,
            lastPositionUpdateTime = 100_000L
        )

        // 20 segundos depois em velocidade 1.5x -> avanço de 30.000ms
        val calculated = info.getCalculatedPositionMs(currentTimeRealtimeMs = 120_000L)
        assertEquals(90_000L, calculated)
    }

    @Test
    fun testPositionCalculationCappedAtDuration() {
        val info = MediaPlaybackInfo(
            title = "Short Track",
            isPlaying = true,
            positionMs = 50_000L,
            durationMs = 60_000L,
            playbackSpeed = 1.0f,
            lastPositionUpdateTime = 10_000L
        )

        // Se o tempo decorrido passar da duração da música, deve limitar a durationMs sem overflow
        val calculated = info.getCalculatedPositionMs(currentTimeRealtimeMs = 30_000L)
        assertEquals(60_000L, calculated)
    }

    @Test
    fun testFormatMediaDuration() {
        // Zero ou negativo
        assertEquals("00:00", formatMediaDuration(0L))
        assertEquals("00:00", formatMediaDuration(-500L))

        // Menos de 1 minuto
        assertEquals("00:45", formatMediaDuration(45_000L))

        // Minutos normais
        assertEquals("03:34", formatMediaDuration(214_000L))

        // Duração longa (> 1 hora, comum em podcasts ou sets)
        assertEquals("1:01:05", formatMediaDuration(3_665_000L))
    }
}
