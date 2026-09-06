package com.tessera.launcher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.LiquidGlassBackground
import com.tessera.launcher.ui.theme.LiquidGlassBorderBrush
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun DinoWidgetCard(
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(0) }

    var dinoY by remember { mutableFloatStateOf(0f) }
    var velocityY by remember { mutableFloatStateOf(0f) }
    val gravity = 1.6f
    val jumpVelocity = -18f

    var obstacleX by remember { mutableFloatStateOf(300f) }
    val obstacleSpeed = 7f

    // Loop do Jogo
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        obstacleX = 400f
        dinoY = 0f
        velocityY = 0f
        score = 0
        isGameOver = false

        while (isActive && isPlaying) {
            delay(16) // ~60 FPS

            // Física do Pulo
            dinoY += velocityY
            velocityY += gravity
            if (dinoY >= 0f) {
                dinoY = 0f
                velocityY = 0f
            }

            // Movimento do Obstáculo
            obstacleX -= obstacleSpeed
            if (obstacleX < -30f) {
                obstacleX = 400f
                score += 10
                if (score > highScore) highScore = score
            }

            // Colisão (Dino ~30px wide, 35px high no chão vs Cacto a 40px da esquerda)
            val dinoLeft = 40f
            val dinoRight = dinoLeft + 24f
            val obstacleLeft = obstacleX
            val obstacleRight = obstacleX + 16f

            val isHorizCollision = dinoRight > obstacleLeft && dinoLeft < obstacleRight
            val isVertCollision = dinoY > -24f // Se não pulou alto o suficiente

            if (isHorizCollision && isVertCollision) {
                isGameOver = true
                isPlaying = false
                break
            }
        }
    }

    val border = if (isLiquidGlass) {
        BorderStroke(1.dp, LiquidGlassBorderBrush)
    } else {
        BorderStroke(1.dp, DarkSurfaceBorder)
    }
    val background = if (isLiquidGlass) LiquidGlassBackground else DarkSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .border(border, CardShape)
            .clip(CardShape)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!isPlaying) {
                        isPlaying = true
                        isGameOver = false
                    } else if (dinoY == 0f) {
                        velocityY = jumpVelocity
                    }
                }
            )
            .padding(12.dp)
    ) {
        // Placar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "DINO RUN",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "HI ${highScore.toString().padStart(4, '0')}  ${score.toString().padStart(4, '0')}",
                color = TextPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Canvas do Jogo
        Canvas(modifier = Modifier.fillMaxSize()) {
            val groundY = size.height - 18f

            // Linha do Chão Pontilhada
            drawLine(
                color = Color(0x66FFFFFF),
                start = Offset(0f, groundY),
                end = Offset(size.width, groundY),
                strokeWidth = 1.5f
            )

            // Dinossauro Retrô Minimalista (Pixels)
            val dinoBaseX = 40f
            val currentDinoY = groundY - 28f + dinoY

            // Corpo
            drawRect(
                color = Color.White,
                topLeft = Offset(dinoBaseX, currentDinoY),
                size = Size(20f, 24f)
            )
            // Cabeça
            drawRect(
                color = Color.White,
                topLeft = Offset(dinoBaseX + 10f, currentDinoY - 8f),
                size = Size(14f, 12f)
            )
            // Olho
            drawRect(
                color = Color.Black,
                topLeft = Offset(dinoBaseX + 18f, currentDinoY - 6f),
                size = Size(3f, 3f)
            )

            // Cacto Retrô
            if (obstacleX < size.width + 50f) {
                drawRect(
                    color = Color.White,
                    topLeft = Offset(obstacleX, groundY - 24f),
                    size = Size(14f, 24f)
                )
                // Braço do Cacto
                drawRect(
                    color = Color.White,
                    topLeft = Offset(obstacleX - 4f, groundY - 18f),
                    size = Size(5f, 10f)
                )
                drawRect(
                    color = Color.White,
                    topLeft = Offset(obstacleX + 13f, groundY - 14f),
                    size = Size(5f, 10f)
                )
            }
        }

        // Mensagens de Estado
        if (!isPlaying && !isGameOver) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Toque para Jogar",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else if (isGameOver) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GAME OVER",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Toque para reiniciar",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
