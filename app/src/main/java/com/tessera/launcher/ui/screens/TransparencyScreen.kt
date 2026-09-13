package com.tessera.launcher.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.LightBackground
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.viewmodel.MainViewModel

@Composable
fun TransparencyScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLight = uiState.themeMode == "LIGHT"

    val bg = if (isLight) LightBackground else AmoledBlack
    val cardBg = if (isLight) LightCardBackground else Color(0xFF0D0D11)
    val cardBorder = if (isLight) LightCardBorder else Color(0xFF1E1E26)
    val textMain = if (isLight) LightTextPrimary else TextPrimary
    val textSub = if (isLight) LightTextSecondary else TextSecondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Barra Superior de Navegação
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isLight) Color(0xFFF1F3F5) else Color(0xFF16161C))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = textMain,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Transparência & Privacidade",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cartão de Resumo Principal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(cardBg)
                    .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(26.dp))
                    .padding(22.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (isLight) Color(0xFFE5E7EB) else Color(0xFF181820)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = "Privacidade",
                            tint = textMain,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Seus dados pertencem a você",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMain
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "O Tessera Launcher foi construído com a convicção de que sua tela inicial deve ser rápida, bela e 100% privada. Nenhum dado pessoal jamais deixa seu aparelho.",
                        fontSize = 13.sp,
                        color = textSub,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "NOSSOS COMPROMISSOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSub.copy(alpha = 0.8f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)
            )

            // Cards de Pilares
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(cardBg)
                    .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(22.dp))
            ) {
                Column {
                    PillarItem(
                        icon = Icons.Outlined.Block,
                        title = "Zero Rastreamento & Telemetria",
                        description = "Não usamos Firebase Analytics, Mixpanel, Crashlytics de terceiros ou identificadores de publicidade.",
                        isLight = isLight
                    )
                    PillarDivider(isLight)

                    PillarItem(
                        icon = Icons.Outlined.Storage,
                        title = "Processamento 100% Local",
                        description = "Toda a busca de apps, contatos, atalhos, notas e histórico reside exclusivamente no armazenamento seguro do seu celular.",
                        isLight = isLight
                    )
                    PillarDivider(isLight)

                    PillarItem(
                        icon = Icons.Outlined.CheckCircleOutline,
                        title = "Livre de Anúncios para Sempre",
                        description = "Sem banners, sem vídeos patrocinados e sem sugestões pagas de aplicativos.",
                        isLight = isLight
                    )
                    PillarDivider(isLight)

                    PillarItem(
                        icon = Icons.Outlined.Lock,
                        title = "Permissões Sob Demanda",
                        description = "O launcher solicita acesso a contatos, notificações e calendário apenas se você desejar usar os widgets e atalhos correspondentes.",
                        isLight = isLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AUDITORIA E CONTROLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSub.copy(alpha = 0.8f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(20.dp))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isLight) Color(0xFFF1F3F5) else Color(0xFF16161C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = "Gerenciar Permissões",
                                tint = textMain,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gerenciar Permissões do App no Sistema",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textMain
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Inspecione e revogue permissões a qualquer momento",
                                fontSize = 12.sp,
                                color = textSub
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = textSub.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    PillarDivider(isLight)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/beyonder96/tessera-launcher"))
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    }
                                }
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isLight) Color(0xFFF1F3F5) else Color(0xFF16161C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Code,
                                contentDescription = "Código Aberto",
                                tint = textMain,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auditar Código Aberto",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textMain
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Verifique o repositório público no GitHub",
                                fontSize = 12.sp,
                                color = textSub
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = textSub.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PillarItem(
    icon: ImageVector,
    title: String,
    description: String,
    isLight: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isLight) Color(0xFFF1F3F5) else Color(0xFF16161C)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isLight) LightTextPrimary else TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isLight) LightTextPrimary else TextPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = if (isLight) LightTextSecondary else TextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun PillarDivider(isLight: Boolean) {
    HorizontalDivider(
        thickness = 1.dp,
        color = if (isLight) Color(0xFFF1F3F5) else Color(0xFF181820)
    )
}
