package com.tessera.launcher.ui.screens.notifications

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tessera.launcher.data.model.AppNotificationItem
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleMotion
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography
import com.tessera.launcher.ui.theme.apple.appleSpring

/**
 * Central de Notificações Oficial da Apple (iOS Notification Center).
 * Exibição com agrupamento dinâmico em cartões de Liquid Glass, botão de limpar com confirmação
 * expansível em 2 etapas ("X" -> "Limpar") e toque para ação.
 */
@Composable
fun NotificationCenter(
    visible: Boolean,
    notifications: List<AppNotificationItem>,
    onNotificationClick: (AppNotificationItem) -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current
    val motion = LocalAppleMotion.current

    var isConfirmingClear by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = appleSpring()) +
                fadeIn(animationSpec = motion.fadeStandard),
        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = appleSpring()) +
                fadeOut(animationSpec = motion.fadeQuick)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.systemBackground.copy(alpha = 0.88f))
                .liquidGlass(style = GlassStyle.Chrome)
                .statusBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barra Superior com Título e Botão de Limpar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Central de Notificações",
                        style = typography.title2,
                        color = colors.label
                    )

                    if (notifications.isNotEmpty()) {
                        // Botão "X" que se expande para "Limpar" ao primeiro toque
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.fill)
                                .clickable {
                                    Taptic.lightImpact(context)
                                    if (isConfirmingClear) {
                                        onClearAll()
                                        isConfirmingClear = false
                                    } else {
                                        isConfirmingClear = true
                                    }
                                }
                                .padding(horizontal = if (isConfirmingClear) 12.dp else 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isConfirmingClear) {
                                Text(
                                    text = "Limpar",
                                    style = typography.caption1.copy(fontWeight = FontWeight.Bold),
                                    color = colors.label
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Limpar Tudo",
                                    tint = colors.secondaryLabel,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (notifications.isEmpty()) {
                    // Estado Vazio Estilo iOS
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsNone,
                                contentDescription = null,
                                tint = colors.tertiaryLabel,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Sem Notificações Mais Antigas",
                                style = typography.subheadline,
                                color = colors.secondaryLabel
                            )
                        }
                    }
                } else {
                    // Lista de Notificações em Cartões Squircle
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications, key = { it.key }) { item ->
                            val relativeTime = DateUtils.getRelativeTimeSpanString(
                                item.timestamp,
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_RELATIVE
                            ).toString()

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(ContinuousShape(18.dp))
                                    .background(colors.secondaryGrouped)
                                    .liquidGlass(style = GlassStyle.Regular, shape = ContinuousShape(18.dp))
                                    .clickable {
                                        Taptic.lightImpact(context)
                                        onNotificationClick(item)
                                    }
                                    .padding(14.dp)
                            ) {
                                Column {
                                    // Cabeçalho da Notificação: Ícone do App, Nome e Hora
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (item.largeIcon != null) {
                                            Image(
                                                bitmap = item.largeIcon.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(ContinuousShape(5.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(ContinuousShape(5.dp))
                                                    .background(colors.accent),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Notifications,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = item.appName.uppercase(),
                                            style = typography.caption2.copy(fontWeight = FontWeight.Bold),
                                            color = colors.secondaryLabel
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        Text(
                                            text = relativeTime,
                                            style = typography.caption2,
                                            color = colors.tertiaryLabel
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Conteúdo da Notificação
                                    Text(
                                        text = item.title,
                                        style = typography.subheadline.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (item.text.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.text,
                                            style = typography.footnote,
                                            color = colors.secondaryLabel,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
