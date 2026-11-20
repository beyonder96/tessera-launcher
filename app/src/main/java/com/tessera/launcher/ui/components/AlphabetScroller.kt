package com.tessera.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.tokens.Tessera
import com.tessera.launcher.ui.theme.tokens.rememberTesseraHaptics
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

val Alphabet = ('A'..'Z').toList() + '#'

@Composable
fun AlphabetScroller(
    availableLetters: Set<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color.White
) {
    var columnHeightPx by remember { mutableIntStateOf(1) }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var touchY by remember { mutableFloatStateOf(-1f) }

    val motion = Tessera.motion
    val palette = Tessera.palette
    val haptics = rememberTesseraHaptics()
    val density = LocalDensity.current
    val maxOffsetPx = with(density) { 28.dp.toPx() }
    val bubbleSize = 48.dp
    val bubbleSizePx = with(density) { bubbleSize.toPx() }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 24.dp, bottom = 16.dp)
            .graphicsLayer { clip = false }
            .onGloballyPositioned { coordinates ->
                columnHeightPx = coordinates.size.height.coerceAtLeast(1)
            }
            .pointerInput(columnHeightPx, availableLetters) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        touchY = offset.y
                        val itemHeight = columnHeightPx.toFloat() / Alphabet.size
                        val index = (offset.y / itemHeight).toInt().coerceIn(0, Alphabet.size - 1)
                        val letter = Alphabet[index]
                        selectedLetter = letter
                        haptics.tick()
                        onLetterSelected(letter)
                    },
                    onDragEnd = {
                        isDragging = false
                        touchY = -1f
                        selectedLetter = null
                    },
                    onDragCancel = {
                        isDragging = false
                        touchY = -1f
                        selectedLetter = null
                    },
                    onVerticalDrag = { change, _ ->
                        change.consume()
                        touchY = change.position.y
                        val itemHeight = columnHeightPx.toFloat() / Alphabet.size
                        val index = (change.position.y / itemHeight).toInt().coerceIn(0, Alphabet.size - 1)
                        val letter = Alphabet[index]
                        if (letter != selectedLetter) {
                            selectedLetter = letter
                            haptics.tick()
                            onLetterSelected(letter)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val itemHeightPx = columnHeightPx.toFloat() / Alphabet.size
        val waveRadiusPx = (itemHeightPx * 3.8f).coerceAtLeast(1f)

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .graphicsLayer { clip = false },
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Alphabet.forEachIndexed { index, char ->
                val isAvailable = availableLetters.contains(char)
                val isCurrentChar = selectedLetter == char

                val letterCenterY = (index + 0.5f) * itemHeightPx
                val distance = if (isDragging && touchY >= 0f) abs(letterCenterY - touchY) else Float.MAX_VALUE

                val curveFactor = if (isDragging && distance < waveRadiusPx) {
                    ((cos((distance / waveRadiusPx) * PI.toFloat()) + 1f) / 2f)
                } else {
                    0f
                }

                val targetOffsetX = if (isDragging) -maxOffsetPx * curveFactor else 0f
                val targetScale = 1.0f + 0.35f * curveFactor

                val animatedOffsetXState = animateFloatAsState(
                    targetValue = targetOffsetX,
                    animationSpec = motion.springQuick,
                    label = "wave_offset_${char}"
                )
                val animatedScaleState = animateFloatAsState(
                    targetValue = targetScale,
                    animationSpec = motion.springQuick,
                    label = "wave_scale_${char}"
                )

                Text(
                    text = char.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isCurrentChar || curveFactor > 0.6f) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = when {
                        isCurrentChar -> accentColor
                        curveFactor > 0.3f -> palette.textPrimary
                        isAvailable -> palette.textPrimary.copy(alpha = 0.72f)
                        else -> palette.textTertiary.copy(alpha = 0.45f)
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(22.dp)
                        .graphicsLayer {
                            translationX = animatedOffsetXState.value
                            val s = animatedScaleState.value
                            scaleX = s
                            scaleY = s
                        }
                )
            }
        }

        // Balão Flutuante (Magnifier Bubble) estilo Cupertino com entrada elástica
        val clampedY = (touchY - bubbleSizePx / 2f).coerceIn(0f, (columnHeightPx - bubbleSizePx).coerceAtLeast(0f))
        AnimatedVisibility(
            visible = isDragging && selectedLetter != null && touchY >= 0f,
            enter = fadeIn(motion.fadeQuick) + scaleIn(initialScale = 0.65f, animationSpec = motion.springBouncy),
            exit = fadeOut(motion.fadeQuick) + scaleOut(targetScale = 0.75f, animationSpec = motion.fadeQuick),
            modifier = Modifier
                .align(Alignment.TopStart)
                .graphicsLayer {
                    translationX = with(density) { (-64).dp.toPx() }
                    translationY = clampedY
                }
        ) {
            Box(
                modifier = Modifier
                    .size(bubbleSize)
                    .shadow(elevation = 12.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(palette.surface.copy(alpha = 0.94f))
                    .border(
                        BorderStroke(
                            0.75.dp,
                            if (accentColor != Color.White) accentColor.copy(alpha = 0.5f) else palette.surfaceBorderHighlight
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (selectedLetter ?: ' ').toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (accentColor != Color.White) accentColor else palette.textPrimary
                )
            }
        }
    }
}
