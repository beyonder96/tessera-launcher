package com.tessera.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Aplica acabamento chanfrado e lapidado no estilo Journal/Tessera:
 * - Gradiente zenital sutil no fundo
 * - Borda de 3 paradas (topo iluminado, lateral média, base sombreada)
 * - Chanfro especular superior de reflexo de luz linear
 */
fun Modifier.journalFacetedSurface(
    shape: Shape = CircleShape,
    isLightMode: Boolean = true,
    isActive: Boolean = false,
    specularBevel: Boolean = true
): Modifier = composed {
    val bgBrush = if (isActive) {
        if (isLightMode) {
            Brush.verticalGradient(listOf(Color(0xFF262320), Color(0xFF161412)))
        } else {
            Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE5E5EB)))
        }
    } else {
        if (isLightMode) {
            Brush.verticalGradient(listOf(Color(0xFFF4F1E8), Color(0xFFEAE5D9)))
        } else {
            Brush.verticalGradient(listOf(Color(0xFF1E212A), Color(0xFF13151B)))
        }
    }

    val borderBrush = if (isActive) {
        if (isLightMode) {
            Brush.verticalGradient(
                listOf(
                    Color(0x60FFFFFF),
                    Color(0x30FFFFFF),
                    Color(0x10000000)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFCCCCCC),
                    Color(0x40000000)
                )
            )
        }
    } else {
        if (isLightMode) {
            Brush.verticalGradient(
                listOf(
                    Color(0x90FFFFFF),
                    Color(0xFFD6D0C2),
                    Color(0x301C1917)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0x35FFFFFF),
                    Color(0x18FFFFFF),
                    Color(0x10000000)
                )
            )
        }
    }

    this
        .clip(shape)
        .background(brush = bgBrush, shape = shape)
        .border(width = 1.dp, brush = borderBrush, shape = shape)
        .then(
            if (specularBevel) {
                Modifier.drawWithContent {
                    drawContent()
                    val specularColor = if (isLightMode) {
                        if (isActive) Color(0x30FFFFFF) else Color(0x70FFFFFF)
                    } else {
                        if (isActive) Color(0x40000000) else Color(0x35FFFFFF)
                    }
                    drawLine(
                        color = specularColor,
                        start = Offset(x = size.width * 0.2f, y = 1f),
                        end = Offset(x = size.width * 0.8f, y = 1f),
                        strokeWidth = 1.5f
                    )
                }
            } else Modifier
        )
}

/**
 * Medalhão lapidado de borda dupla com aro óptico interno rebaixado e badge externo
 * para ícones da Central de Widgets e cabeçalhos.
 */
@Composable
fun JournalMedallion(
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp,
    isLightMode: Boolean = true,
    isActive: Boolean = false,
    badgeCount: Int = 0,
    hasDotBadge: Boolean = false,
    badgeColor: Color = Color(0xFFFF5252),
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val iconTint = if (isActive) {
        if (isLightMode) Color(0xFFF7F4EB) else Color(0xFF121318)
    } else {
        if (isLightMode) Color(0xFF1C1917) else Color(0xFFE4E4E8)
    }

    Box(
        modifier = modifier
            .size(size)
            .tesseraPressable(
                pressedScale = 0.90f,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Base lapidada externa
        Box(
            modifier = Modifier
                .size(size)
                .journalFacetedSurface(
                    shape = CircleShape,
                    isLightMode = isLightMode,
                    isActive = isActive,
                    specularBevel = true
                ),
            contentAlignment = Alignment.Center
        ) {
            // Anel óptico interno sutil para efeito de relevo e profundidade
            Box(
                modifier = Modifier
                    .size(size - 6.dp)
                    .clip(CircleShape)
                    .border(
                        width = 0.5.dp,
                        color = if (isActive) {
                            if (isLightMode) Color(0x20FFFFFF) else Color(0x20000000)
                        } else {
                            if (isLightMode) Color(0x40FFFFFF) else Color(0x15FFFFFF)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        // Badge posicionado FORA do clip para evitar corte nos cantos
        if (hasDotBadge || badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(if (badgeCount > 0) 16.dp else 9.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .border(
                        width = 1.dp,
                        color = if (isLightMode) Color(0xFFF7F4EB) else Color(0xFF0F1014),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (badgeCount > 0) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
