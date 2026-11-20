package com.tessera.launcher.ui.theme.tokens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Camada semântica de feedback háptico do Tessera Launcher.
 * Centraliza os tipos táteis modernos do Compose para consistência em interações de alta frequência.
 */
@Stable
class TesseraHaptics(private val haptic: HapticFeedback) {
    fun tap() = haptic.performHapticFeedback(HapticFeedbackType.ContextClick)

    fun tick() = haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)

    fun threshold() = haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)

    fun confirm() = haptic.performHapticFeedback(HapticFeedbackType.Confirm)

    fun reject() = haptic.performHapticFeedback(HapticFeedbackType.Reject)

    fun longPress() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)

    fun toggle(checked: Boolean) = haptic.performHapticFeedback(
        if (checked) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
    )
}

@Composable
fun rememberTesseraHaptics(): TesseraHaptics {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) { TesseraHaptics(haptic) }
}
