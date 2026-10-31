package com.tessera.launcher.ui.components

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
    isDockMode: Boolean = true,
    isLiquidGlass: Boolean = false,
    isLightMode: Boolean = false
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(0) }

    var dinoY by remember { mutableFloatStateOf(0f) }
    var velocityY by remember { mutableFloatStateOf(0f) }
    val gravity = 0.95f
    val jumpVelocity = -11.5f

    var obstacleX by remember { mutableFloatStateOf(420f) }
    var obstacleType by remember { mutableIntStateOf(0) }
    val obstacleSpeed = 6.2f

    var groundScroll by remember { mutableFloatStateOf(0f) }
    var cloudX by remember { mutableFloatStateOf(260f) }
    var legFrame by remember { mutableIntStateOf(0) }
    var tickCounter by remember { mutableIntStateOf(0) }

    // Loop do Jogo (60 FPS)
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        obstacleX = 420f
        obstacleType = (0..2).random()
        dinoY = 0f
        velocityY = 0f
        score = 0
        isGameOver = false
        groundScroll = 0f
        cloudX = 260f
        tickCounter = 0

        while (isActive && isPlaying) {
            delay(16) // ~60 FPS
            tickCounter++

            // Alternar pernas da corrida a cada ~100ms
            if (tickCounter % 6 == 0) {
                legFrame = 1 - legFrame
            }

            // Física do Pulo
            dinoY += velocityY
            velocityY += gravity
            if (dinoY >= 0f) {
                dinoY = 0f
                velocityY = 0f
            }

            // Rolagem contínua do terreno e nuvens
            groundScroll = (groundScroll + obstacleSpeed) % 200f
            cloudX -= 0.6f
            if (cloudX < -40f) {
                cloudX = 450f
            }

            // Movimento do Obstáculo
            obstacleX -= obstacleSpeed
            if (obstacleX < -40f) {
                obstacleX = (380..440).random().toFloat()
                obstacleType = (0..2).random()
                score += 10
                if (score > highScore) highScore = score
            }

            // Colisão com Cacto
            val dinoLeft = 32f
            val dinoRight = dinoLeft + 18f
            val obstacleLeft = obstacleX
            val obstacleRight = obstacleX + if (obstacleType == 1) 22f else 12f

            val isHorizCollision = dinoRight > obstacleLeft && dinoLeft < obstacleRight
            val obstacleHeight = if (obstacleType == 2) 25f else 20f
            val isVertCollision = (dinoY + 26f) > (26f - obstacleHeight + 4f)

            if (isHorizCollision && isVertCollision) {
                isGameOver = true
                isPlaying = false
                break
            }
        }
    }

    val boxModifier = if (isDockMode) {
        modifier
            .fillMaxWidth()
            .height(105.dp)
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
            .padding(horizontal = 10.dp, vertical = 6.dp)
    } else {
        val border = if (isLiquidGlass) {
            BorderStroke(1.dp, LiquidGlassBorderBrush)
        } else {
            BorderStroke(1.dp, DarkSurfaceBorder)
        }
        val background = if (isLiquidGlass) LiquidGlassBackground else DarkSurface

        modifier
            .fillMaxWidth()
            .height(120.dp)
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
    }

    Box(modifier = boxModifier) {
        // Placar Superior Estilo Arcade Chrome
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DINO RUN",
                color = if (isLightMode) Color(0xFF78716C) else TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "HI ${highScore.toString().padStart(4, '0')}  ${score.toString().padStart(4, '0')}",
                color = if (isLightMode) Color(0xFF1C1917) else TextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        val inkColor = if (isLightMode) Color(0xFF1C1917) else Color.White
        val groundColor = if (isLightMode) Color(0xFFC5BFB0) else Color(0x55FFFFFF)

        // Canvas com Sprite Autêntico do Chrome T-Rex, Cactos e Terreno Infinito
        Canvas(modifier = Modifier.fillMaxSize()) {
            val groundY = size.height - 8f

            // 1. Nuvens em pixel art no céu
            if (cloudX < size.width + 30f) {
                val cloudColor = if (isLightMode) Color(0xFFD6D1C4) else Color(0x40FFFFFF)
                drawRect(cloudColor, Offset(cloudX, 14f), Size(26f, 6f))
                drawRect(cloudColor, Offset(cloudX + 5f, 9f), Size(14f, 6f))
                drawRect(cloudColor, Offset(cloudX + 2f, 17f), Size(8f, 3f))
                drawRect(cloudColor, Offset(cloudX + 16f, 17f), Size(8f, 3f))
            }

            // 2. Linha do Chão e Textura Retrô com pedrinhas em rolagem contínua
            drawLine(
                color = groundColor,
                start = Offset(0f, groundY),
                end = Offset(size.width, groundY),
                strokeWidth = 1.5f
            )
            val segWidth = 80f
            for (i in -1..(size.width / segWidth).toInt() + 1) {
                val segX = i * segWidth - (groundScroll % segWidth)
                drawRect(groundColor, Offset(segX + 10f, groundY + 3f), Size(3f, 1.5f))
                drawRect(groundColor, Offset(segX + 28f, groundY + 5f), Size(2f, 1.5f))
                drawRect(groundColor, Offset(segX + 45f, groundY + 2f), Size(5f, 1.5f))
                drawRect(groundColor, Offset(segX + 65f, groundY + 4f), Size(2.5f, 1.5f))
            }

            // 3. T-Rex Autêntico do Chrome em Pixel Art
            val dinoBaseX = 30f
            val currentDinoY = groundY - 26f + dinoY

            // Cabeça & Focinho
            drawRect(inkColor, Offset(dinoBaseX + 11f, currentDinoY), Size(12f, 10f))
            drawRect(inkColor, Offset(dinoBaseX + 15f, currentDinoY), Size(8f, 4f))

            // Olho do Dino (recorte)
            drawRect(
                color = if (isLightMode) Color(0xFFF7F4EB) else Color.Black,
                topLeft = Offset(dinoBaseX + 13f, currentDinoY + 2f),
                size = Size(2.5f, 2.5f)
            )

            // Recorte da boca
            drawRect(
                color = if (isLightMode) Color(0xFFF7F4EB) else Color.Black,
                topLeft = Offset(dinoBaseX + 18f, currentDinoY + 7f),
                size = Size(5f, 1.5f)
            )

            // Pescoço e Peito
            drawRect(inkColor, Offset(dinoBaseX + 9f, currentDinoY + 9f), Size(7f, 6f))

            // Corpo e Costas
            drawRect(inkColor, Offset(dinoBaseX + 3f, currentDinoY + 11f), Size(12f, 9f))

            // Cauda curvada
            drawRect(inkColor, Offset(dinoBaseX, currentDinoY + 8f), Size(3.5f, 6f))
            drawRect(inkColor, Offset(dinoBaseX + 2f, currentDinoY + 13f), Size(3f, 5f))

            // Braço do T-Rex
            drawRect(inkColor, Offset(dinoBaseX + 16f, currentDinoY + 13f), Size(3f, 2f))
            drawRect(inkColor, Offset(dinoBaseX + 18f, currentDinoY + 14.5f), Size(1.5f, 2f))

            // Patinhas do T-Rex (com animação de corrida fiel ao Chrome)
            if (dinoY < 0f || isGameOver || !isPlaying) {
                // Parado / Pulando: pernas esticadas prontas para pousar
                drawRect(inkColor, Offset(dinoBaseX + 7f, currentDinoY + 20f), Size(2.5f, 6f))
                drawRect(inkColor, Offset(dinoBaseX + 7f, currentDinoY + 25f), Size(4.5f, 1.5f))

                drawRect(inkColor, Offset(dinoBaseX + 12f, currentDinoY + 20f), Size(2.5f, 6f))
                drawRect(inkColor, Offset(dinoBaseX + 12f, currentDinoY + 25f), Size(4.5f, 1.5f))
            } else if (legFrame == 0) {
                // Frame 0: Perna esquerda no chão, direita levantada
                drawRect(inkColor, Offset(dinoBaseX + 6f, currentDinoY + 20f), Size(2.5f, 6f))
                drawRect(inkColor, Offset(dinoBaseX + 6f, currentDinoY + 25f), Size(4.5f, 1.5f))

                drawRect(inkColor, Offset(dinoBaseX + 12f, currentDinoY + 20f), Size(2.5f, 3.5f))
                drawRect(inkColor, Offset(dinoBaseX + 14f, currentDinoY + 22.5f), Size(3.5f, 2f))
            } else {
                // Frame 1: Perna direita no chão, esquerda levantada
                drawRect(inkColor, Offset(dinoBaseX + 6f, currentDinoY + 20f), Size(2.5f, 3.5f))
                drawRect(inkColor, Offset(dinoBaseX + 4f, currentDinoY + 22.5f), Size(3.5f, 2f))

                drawRect(inkColor, Offset(dinoBaseX + 12f, currentDinoY + 20f), Size(2.5f, 6f))
                drawRect(inkColor, Offset(dinoBaseX + 12f, currentDinoY + 25f), Size(4.5f, 1.5f))
            }

            // 4. Cactos Autênticos em Pixel Art
            if (obstacleX < size.width + 40f) {
                fun drawCactus(x: Float, height: Float) {
                    val cBaseY = groundY - height
                    // Tronco central
                    drawRect(inkColor, Offset(x + 4f, cBaseY), Size(4f, height))
                    drawRect(inkColor, Offset(x + 4.5f, cBaseY - 1.5f), Size(3f, 1.5f))

                    // Braço Esquerdo
                    val leftArmY = cBaseY + height * 0.35f
                    drawRect(inkColor, Offset(x, leftArmY + 3f), Size(4.5f, 3f))
                    drawRect(inkColor, Offset(x, leftArmY - 3f), Size(3f, 7f))
                    drawRect(inkColor, Offset(x + 0.5f, leftArmY - 4.5f), Size(2f, 1.5f))

                    // Braço Direito
                    val rightArmY = cBaseY + height * 0.48f
                    drawRect(inkColor, Offset(x + 7.5f, rightArmY + 3f), Size(4.5f, 3f))
                    drawRect(inkColor, Offset(x + 9f, rightArmY - 2f), Size(3f, 6f))
                    drawRect(inkColor, Offset(x + 9.5f, rightArmY - 3.5f), Size(2f, 1.5f))
                }

                when (obstacleType) {
                    1 -> {
                        // Cacto Duplo
                        drawCactus(obstacleX, 19f)
                        drawCactus(obstacleX + 12f, 16f)
                    }
                    2 -> {
                        // Cacto Alto
                        drawCactus(obstacleX, 25f)
                    }
                    else -> {
                        // Cacto Clássico Único
                        drawCactus(obstacleX, 20f)
                    }
                }
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
                    color = inkColor,
                    fontSize = 11.sp,
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
                        color = inkColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Toque para reiniciar",
                        color = if (isLightMode) Color(0xFF78716C) else TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
