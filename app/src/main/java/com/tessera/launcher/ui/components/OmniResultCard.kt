package com.tessera.launcher.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.tessera.launcher.data.helper.OmniCategory
import com.tessera.launcher.data.helper.OmniResult
import com.tessera.launcher.ui.theme.AmoledCardBackground
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary

@Composable
fun OmniResultCard(
    result: OmniResult,
    accentColor: Color,
    isLightMode: Boolean,
    isAmoledMode: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val cardBg = when {
        isLightMode -> LightCardBackground
        isAmoledMode -> AmoledCardBackground
        else -> DarkSurface
    }

    val cardBorder = when {
        isLightMode -> LightCardBorder
        isAmoledMode -> AmoledCardBorder
        else -> DarkSurfaceBorder
    }

    val textPrimary = if (isLightMode) LightTextPrimary else TextPrimary
    val textSecondary = if (isLightMode) LightTextSecondary else TextSecondary

    val onCopy = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Resultado", result.primaryResult)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Copiado: ${result.primaryResult}", Toast.LENGTH_SHORT).show()
    }

    val onAction = {
        when (result.actionType) {
            "whatsapp" -> {
                try {
                    val query = result.actionPayload.orEmpty().trim()
                    val uri = if (query.matches(Regex("""^\+?\d+$"""))) {
                        Uri.parse("https://api.whatsapp.com/send?phone=$query")
                    } else {
                        Uri.parse("whatsapp://send?text=")
                    }
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.whatsapp")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    try {
                        val fallback = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                        if (fallback != null) {
                            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(fallback)
                        } else {
                            Toast.makeText(context, "WhatsApp não instalado", Toast.LENGTH_SHORT).show()
                        }
                    } catch (_: Exception) {}
                }
            }
            "call" -> {
                try {
                    val number = result.actionPayload.orEmpty().trim()
                    val dialUri = if (number.isNotBlank()) Uri.parse("tel:$number") else Uri.parse("tel:")
                    val dialIntent = Intent(Intent.ACTION_DIAL, dialUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(dialIntent)
                } catch (_: Exception) {
                    Toast.makeText(context, "Não foi possível abrir o discador", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(1.dp, if (accentColor != Color.White) accentColor.copy(alpha = 0.4f) else cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Linha Superior: Categoria & Botão Copiar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge da Categoria
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(if (accentColor != Color.White) accentColor.copy(alpha = 0.15f) else Color(0xFF1E1E26))
                            .border(BorderStroke(1.dp, if (accentColor != Color.White) accentColor.copy(alpha = 0.45f) else Color(0x33FFFFFF)), PillShape)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${result.category.emoji} ${result.category.displayName.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (accentColor != Color.White) accentColor else textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = result.sourceQuery,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = textSecondary,
                        maxLines = 1
                    )
                }

                // Botão Copiar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isLightMode) Color(0xFFE5E7EB) else Color(0xFF1E1E26))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onCopy
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copiar Resultado",
                        tint = textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resultado Principal em Destaque
            Text(
                text = result.primaryResult,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp
                ),
                color = textPrimary,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCopy
                )
            )

            // Detalhes Secundários ou Ação Rápida
            if (result.actionType != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (accentColor != Color.White) accentColor else Color.White)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onAction
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (result.actionType == "whatsapp") Icons.Outlined.Chat else Icons.Outlined.Call,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (result.actionType == "whatsapp") "Abrir WhatsApp" else "Discar Número",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                }
            } else if (!result.secondaryDetail.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = result.secondaryDetail,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = textSecondary
                )
            }
        }
    }
}
