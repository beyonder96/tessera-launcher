package com.tessera.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextTertiary

val Alphabet = ('A'..'Z').toList() + '#'

@Composable
fun AlphabetScroller(
    availableLetters: Set<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    var columnHeightPx by remember { mutableStateOf(1) }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(26.dp)
            .padding(vertical = 12.dp)
            .onGloballyPositioned { coordinates ->
                columnHeightPx = coordinates.size.height.coerceAtLeast(1)
            }
            .pointerInput(columnHeightPx, availableLetters) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        val itemHeight = columnHeightPx.toFloat() / Alphabet.size
                        val index = (offset.y / itemHeight).toInt().coerceIn(0, Alphabet.size - 1)
                        val letter = Alphabet[index]
                        selectedLetter = letter
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onLetterSelected(letter)
                    },
                    onDragEnd = { selectedLetter = null },
                    onDragCancel = { selectedLetter = null },
                    onVerticalDrag = { change, _ ->
                        change.consume()
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
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Alphabet.forEach { char ->
                val isAvailable = availableLetters.contains(char)
                val isSelected = selectedLetter == char
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.4f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                    label = "char_scale"
                )

                Text(
                    text = char.toString(),
                    fontSize = if (isSelected) 12.sp else 9.5.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = when {
                        isSelected -> TextPrimary
                        isAvailable -> TextPrimary.copy(alpha = 0.65f)
                        else -> TextTertiary
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .scale(scale)
                        .width(20.dp)
                )
            }
        }
    }
}
