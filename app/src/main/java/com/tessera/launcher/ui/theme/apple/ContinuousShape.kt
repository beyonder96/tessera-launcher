package com.tessera.launcher.ui.theme.apple

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Forma contínua inspirada na geometria proprietária de squircle da Apple (Corner Smoothing a ~60%).
 * Elimina as quinas abruptas de transição entre retas e arcos circulares padrão do RoundedCornerShape.
 */
@Immutable
class ContinuousShape(
    val cornerRadius: Dp,
    val smoothing: Float = 0.60f
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radiusPx = with(density) { cornerRadius.toPx() }
        val maxRadius = min(size.width, size.height) / 2f
        val r = radiusPx.coerceIn(0f, maxRadius)

        if (r <= 0f) {
            return Outline.Rectangle(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
        }

        val w = size.width
        val h = size.height

        // Se o raio for praticamente metade do menor lado, forma uma cápsula contínua perfeita
        if (r >= maxRadius - 1f) {
            return Outline.Rounded(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = w,
                    bottom = h,
                    radiusX = r,
                    radiusY = r
                )
            )
        }

        // Construção do contorno suave contínuo via curvas cúbicas de Bézier
        val p = smoothing.coerceIn(0f, 1f)
        val l = r * (1f + p) // extensão do canto
        val d = r * 0.45f    // deslocamento de suavização

        val path = Path().apply {
            moveTo(l, 0f)

            // Topo -> Direita
            lineTo(w - l, 0f)
            cubicTo(
                w - l + d, 0f,
                w - r + d, r - d,
                w, r
            )
            lineTo(w, r)

            // Direita -> Fundo
            lineTo(w, h - l)
            cubicTo(
                w, h - l + d,
                w - r + d, h - r + d,
                w - r, h
            )
            lineTo(w - l, h)

            // Fundo -> Esquerda
            lineTo(l, h)
            cubicTo(
                l - d, h,
                r - d, h - r + d,
                0f, h - r
            )
            lineTo(0f, h - r)

            // Esquerda -> Topo
            lineTo(0f, l)
            cubicTo(
                0f, l - d,
                r - d, r - d,
                r, 0f
            )
            close()
        }

        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContinuousShape) return false
        return cornerRadius == other.cornerRadius && smoothing == other.smoothing
    }

    override fun hashCode(): Int {
        return 31 * cornerRadius.hashCode() + smoothing.hashCode()
    }
}

/**
 * Padrões de raios contínuos do iOS 26.
 */
object AppleShapes {
    val card = ContinuousShape(24.dp)
    val widget = ContinuousShape(22.dp)
    val button = ContinuousShape(14.dp)
    val capsule = ContinuousShape(999.dp)
    val sheet = ContinuousShape(38.dp)
    val small = ContinuousShape(10.dp)
    val icon = ContinuousShape(16.dp)
}
