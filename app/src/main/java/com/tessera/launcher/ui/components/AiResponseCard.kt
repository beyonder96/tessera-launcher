package com.tessera.launcher.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.AmoledCardBackground
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary

@Composable
fun AiResponseCard(
    query: String,
    response: String?,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    isLightMode: Boolean = false,
    isAmoledMode: Boolean = false
) {
    val context = LocalContext.current

    val surfaceBg = when {
        isLightMode -> LightCardBackground
        isAmoledMode -> AmoledCardBackground
        else -> DarkSurface
    }
    val borderCol = when {
        isLightMode -> LightCardBorder
        isAmoledMode -> AmoledCardBorder
        else -> DarkSurfaceBorder
    }
    val textPrimaryCol = if (isLightMode) LightTextPrimary else TextPrimary
    val textSecondaryCol = if (isLightMode) LightTextSecondary else TextSecondary

    Surface(
        shape = CardShape,
        color = surfaceBg,
        border = BorderStroke(1.dp, borderCol),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cabeçalho: Ícone IA + Título + Botão de Ação
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = textSecondaryCol,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "RESPOSTA RÁPIDA • GEMINI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = textSecondaryCol,
                    modifier = Modifier.weight(1f)
                )

                if (!response.isNullOrBlank() && !isLoading) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(),
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Resposta Gemini", response))
                                    Toast.makeText(context, "Resposta copiada", Toast.LENGTH_SHORT).show()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copiar resposta",
                            tint = textSecondaryCol,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Conteúdo principal baseado no estado
            when {
                isLoading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = textSecondaryCol
                        )
                        Text(
                            text = "Pensando...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textSecondaryCol
                        )
                    }
                }

                !error.isNullOrBlank() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = textSecondaryCol,
                            lineHeight = 18.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Surface(
                                shape = PillShape,
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, borderCol),
                                modifier = Modifier
                                    .height(28.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(),
                                        onClick = onOpenSettings
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = null,
                                        tint = textSecondaryCol,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Configurar Chave",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimaryCol
                                    )
                                }
                            }

                            Surface(
                                shape = PillShape,
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, borderCol),
                                modifier = Modifier
                                    .height(28.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(),
                                        onClick = onRetry
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = null,
                                        tint = textSecondaryCol,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tentar de novo",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textSecondaryCol
                                    )
                                }
                            }
                        }
                    }
                }

                !response.isNullOrBlank() -> {
                    Text(
                        text = response,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = textPrimaryCol,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
