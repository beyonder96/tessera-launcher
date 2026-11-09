package com.tessera.launcher.ui.components.hud

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.HudPeek
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback

/**
 * Modo Peek: Notificação importante temporária exibida em formato de pílula expandida.
 */
@Composable
fun IslandPeekContent(
    peek: HudPeek,
    hideContent: Boolean,
    defaultAccent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandAccent = peek.brandColor?.let { Color(it) } ?: defaultAccent

    Row(
        modifier = modifier
            .widthIn(min = 280.dp, max = 340.dp)
            .tesseraPressFeedback()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar ou Ícone do App
        val safeAvatar = remember(peek.avatar) {
            runCatching {
                peek.avatar?.takeIf { !it.isRecycled }?.asImageBitmap()
            }.getOrNull()
        }

        if (safeAvatar != null) {
            Image(
                bitmap = safeAvatar,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, brandAccent.copy(alpha = 0.4f), CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(brandAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = brandAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Conteúdo textual
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = peek.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• ${peek.appName}",
                    color = Color(0xFFA1A1AA),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            val displayText = if (hideContent) {
                "Conteúdo oculto por privacidade"
            } else {
                peek.text.ifBlank { "Toque para abrir" }
            }

            Text(
                text = displayText,
                color = if (hideContent) Color(0xFF71717A) else Color(0xFFE4E4E7),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
