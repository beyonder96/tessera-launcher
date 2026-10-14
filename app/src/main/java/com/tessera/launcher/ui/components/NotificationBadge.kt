package com.tessera.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Badge / Ponto de notificação minimalista e moderno para ícones de apps.
 * Exibe uma bolinha sutil para 1 notificação ou pílula com contador para > 1.
 */
@Composable
fun NotificationBadge(
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isLightMode: Boolean = false
) {
    if (count <= 0) return

    val borderColor = if (isLightMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val badgeColor = accentColor

    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = CircleShape)
            .border(width = 1.5.dp, color = borderColor, shape = CircleShape)
            .clip(CircleShape)
            .background(badgeColor),
        contentAlignment = Alignment.Center
    ) {
        if (count == 1) {
            // Dot sutil
            Box(modifier = Modifier.size(9.dp))
        } else {
            // Contador numérico
            val text = if (count > 99) "99+" else count.toString()
            Text(
                text = text,
                color = Color.White,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .defaultMinSize(minWidth = 15.dp)
                    .height(15.dp)
                    .padding(horizontal = 3.dp),
                lineHeight = 15.sp
            )
        }
    }
}
