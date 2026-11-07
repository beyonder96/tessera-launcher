package com.tessera.launcher.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.AmoledCardBackground
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.GeminiCyan
import com.tessera.launcher.ui.theme.GeminiPurple
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
    provider: String = "GROQ",
    isLightMode: Boolean = false,
    isAmoledMode: Boolean = false
) {
    val context = LocalContext.current

    // Não renderiza caixa vazia se não houver resposta, não estiver carregando e não houver erro
    if (response.isNullOrBlank() && !isLoading && error.isNullOrBlank()) {
        return
    }

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

    val providerHeader = if (provider.equals("GEMINI", true)) {
        "GEMINI 2.5 FLASH"
    } else {
        "GROQ LLAMA 3.3"
    }

    val cleanPrompt = query
        .replaceFirst(Regex("^@(ai|gemini|groq)\\s*", RegexOption.IGNORE_CASE), "")
        .trim()

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = surfaceBg,
        border = BorderStroke(1.dp, borderCol),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Acento com gradiente estilo Gemini no topo do card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .clip(PillShape)
                    .background(com.tessera.launcher.ui.theme.geminiLinearBrush(0.85f))
            )

            // Cabeçalho: Ícone IA + Título do Modelo + Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    GeminiCyan.copy(alpha = 0.25f),
                                    GeminiPurple.copy(alpha = 0.25f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = GeminiCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = providerHeader,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = if (isLightMode) Color(0xFF4338CA) else GeminiCyan
                    )
                    if (cleanPrompt.isNotBlank()) {
                        Text(
                            text = cleanPrompt,
                            fontSize = 11.5.sp,
                            color = textSecondaryCol,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!response.isNullOrBlank() && !isLoading) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isLightMode) Color(0x181C1917) else Color(0x20FFFFFF))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Resposta Pronta",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondaryCol
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
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = GeminiCyan
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Pensando e sintetizando...",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimaryCol
                            )
                            Text(
                                text = "Consultando $providerHeader em tempo real",
                                fontSize = 11.5.sp,
                                color = textSecondaryCol
                            )
                        }
                    }
                }

                !error.isNullOrBlank() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = error,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isLightMode) Color(0xFFDC2626) else Color(0xFFF87171),
                            lineHeight = 19.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            AiResponseActionButton(
                                icon = Icons.Outlined.Settings,
                                label = "Configurar Chave",
                                onClick = onOpenSettings,
                                borderColor = borderCol,
                                contentColor = textPrimaryCol
                            )

                            AiResponseActionButton(
                                icon = Icons.Outlined.Refresh,
                                label = "Tentar de novo",
                                onClick = onRetry,
                                borderColor = borderCol,
                                contentColor = textSecondaryCol
                            )
                        }
                    }
                }

                !response.isNullOrBlank() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Renderização formatada da resposta com suporte a parágrafos e negritos
                        FormattedAiText(
                            rawText = response,
                            primaryColor = textPrimaryCol,
                            secondaryColor = textSecondaryCol
                        )

                        // Barra de Ações Rápidas (Copiar, Compartilhar, Regenerar)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AiResponseActionButton(
                                icon = Icons.Outlined.ContentCopy,
                                label = "Copiar",
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Resposta IA", response))
                                    Toast.makeText(context, "Resposta copiada para a área de transferência", Toast.LENGTH_SHORT).show()
                                },
                                borderColor = borderCol,
                                contentColor = textSecondaryCol
                            )

                            AiResponseActionButton(
                                icon = Icons.Outlined.Share,
                                label = "Compartilhar",
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, response)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartilhar resposta da IA").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                },
                                borderColor = borderCol,
                                contentColor = textSecondaryCol
                            )

                            AiResponseActionButton(
                                icon = Icons.Outlined.Refresh,
                                label = "Regenerar",
                                onClick = onRetry,
                                borderColor = borderCol,
                                contentColor = textSecondaryCol
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente que renderiza texto da IA com formatação leve (parágrafos, negrito **texto** e listas).
 */
@Composable
private fun FormattedAiText(
    rawText: String,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val paragraphs = remember(rawText) {
        rawText.trim().split(Regex("\n{2,}"))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (paragraph in paragraphs) {
            val lines = paragraph.split("\n")
            for (line in lines) {
                val trimmedLine = line.trim()
                if (trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") || trimmedLine.startsWith("• ")) {
                    // Marcador de lista com recuo e ponto elegante
                    val itemContent = trimmedLine.drop(2).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeminiCyan
                        )
                        Text(
                            text = formatMarkdownAnnotated(itemContent, primaryColor, primaryColor),
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = primaryColor
                        )
                    }
                } else {
                    Text(
                        text = formatMarkdownAnnotated(trimmedLine, primaryColor, primaryColor),
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = primaryColor
                    )
                }
            }
        }
    }
}

/**
 * Converte blocos de **negrito** em AnnotatedString estilizado.
 */
private fun formatMarkdownAnnotated(text: String, defaultColor: Color, boldColor: Color) = buildAnnotatedString {
    val tokens = text.split("**")
    tokens.forEachIndexed { index, token ->
        if (index % 2 == 1) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                append(token)
            }
        } else {
            withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = defaultColor)) {
                append(token)
            }
        }
    }
}

@Composable
private fun AiResponseActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    borderColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = PillShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .height(30.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.5.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
