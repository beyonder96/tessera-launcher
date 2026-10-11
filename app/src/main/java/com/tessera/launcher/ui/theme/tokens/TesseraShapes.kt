package com.tessera.launcher.ui.theme.tokens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Escala harmônica de formas geométricas do Tessera Launcher.
 * Reduzida a 6 tokens semânticos que transmitem intenção tátil clara.
 */
@Immutable
data class TesseraShapes(
    val micro: Shape = RoundedCornerShape(4.dp),
    val small: Shape = RoundedCornerShape(8.dp),
    val medium: Shape = RoundedCornerShape(12.dp),
    val large: Shape = RoundedCornerShape(16.dp),
    val card: Shape = RoundedCornerShape(20.dp),
    val sheet: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val pill: Shape = RoundedCornerShape(28.dp),
    val full: Shape = CircleShape
)
