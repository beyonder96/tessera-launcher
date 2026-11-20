package com.tessera.launcher.ui.theme.tokens

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Gerador lazy de ruído monocromático 128x128 tileable para acabamento editorial fosco.
 * Criado uma única vez no processo e reutilizado via BitmapShader sem alocações por frame.
 */
private object TesseraGrainCache {
    val shaderBrush: ShaderBrush by lazy {
        val size = 128
        val pixels = IntArray(size * size)
        var seed = 0x5DEECE66DL
        for (i in pixels.indices) {
            seed = (seed * 0x5DEECE66DL + 0xBL) and ((1L shl 48) - 1)
            val luma = ((seed ushr 40).toInt() and 0xFF)
            val alpha = 0xFF
            pixels[i] = (alpha shl 24) or (luma shl 16) or (luma shl 8) or luma
        }
        val bitmap = Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
        val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        ShaderBrush(shader)
    }
}

/**
 * Aplica textura editorial de grão sutil (0.025f..0.04f) sobre superfícies amplas ou glass.
 */
fun Modifier.tesseraGrain(alpha: Float = 0.03f): Modifier = this.drawWithCache {
    val brush = TesseraGrainCache.shaderBrush
    onDrawWithContent {
        drawContent()
        drawRect(brush = brush, alpha = alpha)
    }
}

/**
 * Superfície unificada de vidro / card do Tessera Design System.
 * Centraliza gradiente de preenchimento, borda especular direcional e sombras semânticas.
 */
@Composable
fun Modifier.tesseraGlass(
    shape: Shape,
    isLiquidGlass: Boolean = true,
    opacityFactor: Float = 1f,
    elevation: Dp = 8.dp,
    borderWidth: Dp = 0.75.dp,
    grainAlpha: Float = 0.025f
): Modifier {
    val palette = Tessera.palette
    val alphaClamped = opacityFactor.coerceIn(0f, 1f)

    val fillBrush = remember(palette, isLiquidGlass, alphaClamped) {
        if (isLiquidGlass) {
            Brush.verticalGradient(
                colors = listOf(
                    palette.glassFill.copy(alpha = palette.glassFill.alpha * alphaClamped),
                    palette.glassFillEnd.copy(alpha = palette.glassFillEnd.alpha * alphaClamped)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    palette.surface.copy(alpha = alphaClamped),
                    palette.surface.copy(alpha = alphaClamped)
                )
            )
        }
    }

    val borderBrush = remember(palette, isLiquidGlass, alphaClamped) {
        if (isLiquidGlass) {
            Brush.verticalGradient(
                colors = listOf(
                    palette.glassStrokeTop.copy(alpha = (palette.glassStrokeTop.alpha * alphaClamped).coerceIn(0.08f, 0.95f)),
                    palette.glassStrokeBottom.copy(alpha = (palette.glassStrokeBottom.alpha * alphaClamped).coerceIn(0.03f, 0.45f))
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(palette.surfaceBorderHighlight, palette.surfaceBorder)
            )
        }
    }

    val effectiveShadow = elevation * alphaClamped
    return this
        .then(
            if (effectiveShadow > 0.dp) {
                Modifier.shadow(
                    elevation = effectiveShadow,
                    shape = shape,
                    ambientColor = palette.shadowAmbient,
                    spotColor = if (alphaClamped < 0.9f) Color.Transparent else palette.shadowSpot
                )
            } else Modifier
        )
        .clip(shape)
        .background(fillBrush)
        .then(if (grainAlpha > 0f && isLiquidGlass) Modifier.tesseraGrain(grainAlpha) else Modifier)
        .border(BorderStroke(borderWidth, borderBrush), shape)
}
