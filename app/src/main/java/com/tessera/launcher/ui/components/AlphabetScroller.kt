package com.tessera.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextTertiary
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

    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val maxOffsetPx = with(density) { 46.dp.toPx() }
    val bubbleSize = 52.dp
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
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onLetterSelected(letter)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val itemHeightPx = columnHeightPx.toFloat() / Alphabet.size
        val waveRadiusPx = (itemHeightPx * 4.5f).coerceAtLeast(1f)

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
                val targetScale = 1.0f + 0.85f * curveFactor

                val animatedOffsetX by animateFloatAsState(
                    targetValue = targetOffsetX,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
                    label = "wave_offset_${char}"
                )
                val animatedScale by animateFloatAsState(
                    targetValue = targetScale,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
                    label = "wave_scale_${char}"
                )

                Text(
                    text = char.toString(),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = if (isCurrentChar || curveFactor > 0.6f) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isCurrentChar -> accentColor
                        curveFactor > 0.3f -> TextPrimary
                        isAvailable -> TextPrimary.copy(alpha = 0.7f)
                        else -> TextTertiary.copy(alpha = 0.45f)
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(22.dp)
                        .graphicsLayer {
                            translationX = animatedOffsetX
                            scaleX = animatedScale
                            scaleY = animatedScale
                        }
                )
            }
        }

        // Balão Flutuante (Magnifier Bubble) estilo Niagara ao arrastar
        if (isDragging && selectedLetter != null && touchY >= 0f) {
            val clampedY = (touchY - bubbleSizePx / 2f).coerceIn(0f, (columnHeightPx - bubbleSizePx).coerceAtLeast(0f))

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        translationX = with(density) { (-82).dp.toPx() }
                        translationY = clampedY
                    }
                    .size(bubbleSize)
                    .shadow(elevation = 14.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xE614141A))
                    .border(BorderStroke(1.5.dp, if (accentColor != Color.White) accentColor.copy(alpha = 0.8f) else Color(0x33FFFFFF)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedLetter.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (accentColor != Color.White) accentColor else Color.White
                )
            }
        }
    }
}
