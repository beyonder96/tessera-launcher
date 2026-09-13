package com.tessera.launcher.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.BuildConfig
import com.tessera.launcher.R
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.DarkBackground
import com.tessera.launcher.ui.theme.LightBackground
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import com.tessera.launcher.ui.viewmodel.MainViewModel
import kotlin.math.roundToInt

@Composable
fun DeveloperScreen(
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

    // Animação Notion Infinite Float
    val infiniteTransition = rememberInfiniteTransition(label = "notion_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_float"
    )
    val avatarRotate by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_rotate"
    )
    val sparklePulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_pulse"
    )

    val versionName = try {
        BuildConfig.VERSION_NAME
    } catch (_: Throwable) {
        "1.8.8"
    }
    val versionCode = try {
        BuildConfig.VERSION_CODE
    } catch (_: Throwable) {
        10
    }

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
                    text = "Desenvolvedor",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Cartão de Destaque com Animação Estilo Notion
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(cardBg)
                    .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(28.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Fundo decorativo de linhas suaves estilo Notion
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Contêiner do Avatar Flutuante
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .offset { IntOffset(0, floatOffset.roundToInt()) }
                            .rotate(avatarRotate),
                        contentAlignment = Alignment.Center
                    ) {
                        // Halo de sombra suave
                        Box(
                            modifier = Modifier
                                .size(115.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isLight) Color(0x15000000)
                                    else Color(0x33FFFFFF)
                                )
                        )

                        // Avatar do Desenvolvedor
                        Image(
                            painter = painterResource(id = R.drawable.ic_developer_avatar),
                            contentDescription = "Developer Notion Avatar",
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(
                                    BorderStroke(2.5.dp, if (isLight) Color(0xFF222222) else Color(0xFFE5E7EB)),
                                    CircleShape
                                )
                        )

                        // Partícula / Sparkle Notion flutuante
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .scale(sparklePulse)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isLight) Color(0xFF111827) else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✦",
                                color = if (isLight) Color.White else Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Tessera Core Lab",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMain
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Projetado com foco em minimalismo, velocidade e privacidade total.",
                        fontSize = 13.sp,
                        color = textSub,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Badge de Versão Dinâmica Estilo Notion
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(if (isLight) Color(0xFFE5E7EB) else Color(0xFF1A1A22))
                            .border(BorderStroke(1.dp, if (isLight) Color(0xFFD1D5DB) else Color(0xFF2D2D38)), PillShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "v$versionName ($versionCode) • Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = textMain
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seção: Informações Técnicas & Telemetria Zero
            Text(
                text = "ESPECIFICAÇÕES DO SISTEMA",
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
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column {
                    SpecRow(label = "Versão do App", value = "v$versionName", isLight = isLight)
                    SpecDivider(isLight)
                    SpecRow(label = "Código da Build", value = versionCode.toString(), isLight = isLight)
                    SpecDivider(isLight)
                    SpecRow(label = "SDK Alvo", value = "Android 16 (API 36)", isLight = isLight)
                    SpecDivider(isLight)
                    SpecRow(label = "Arquitetura", value = "Jetpack Compose Modern UI", isLight = isLight)
                    SpecDivider(isLight)
                    SpecRow(label = "Apps Indexados", value = "${uiState.allInstalledApps.size} instalados", isLight = isLight)
                    SpecDivider(isLight)
                    SpecRow(label = "Rastreamento / Anúncios", value = "0% (Zero Tracker)", isLight = isLight)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seção: Ferramentas do Desenvolvedor
            Text(
                text = "AÇÕES DE DEPURAÇÃO",
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
                    DevActionItem(
                        icon = Icons.Outlined.ContentCopy,
                        title = "Copiar Diagnóstico do Sistema",
                        subtitle = "Copia versão, modelo e status para a área de transferência",
                        onClick = {
                            val diagnosticInfo = """
                                Tessera Launcher
                                Versão: $versionName ($versionCode)
                                Aparelho: ${Build.MANUFACTURER} ${Build.MODEL}
                                Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
                                Apps: ${uiState.allInstalledApps.size}
                                Tema: ${uiState.themeMode}
                            """.trimIndent()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Tessera Diagnostics", diagnosticInfo))
                            Toast.makeText(context, "Diagnóstico copiado!", Toast.LENGTH_SHORT).show()
                        },
                        isLight = isLight
                    )

                    SpecDivider(isLight)

                    DevActionItem(
                        icon = Icons.Outlined.DeleteOutline,
                        title = "Limpar Cache Temporário de Notícias",
                        subtitle = "Força a recarga limpa das fontes de feed",
                        onClick = {
                            viewModel.refreshFeed()
                            Toast.makeText(context, "Feed atualizado com sucesso.", Toast.LENGTH_SHORT).show()
                        },
                        isLight = isLight
                    )

                    SpecDivider(isLight)

                    DevActionItem(
                        icon = Icons.Outlined.OpenInNew,
                        title = "Repositório GitHub",
                        subtitle = "Código-fonte aberto e documentação do projeto",
                        onClick = {
                            runCatching {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/beyonder96/tessera-launcher"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        },
                        isLight = isLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String, isLight: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (isLight) LightTextSecondary else TextSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isLight) LightTextPrimary else TextPrimary
        )
    }
}

@Composable
private fun SpecDivider(isLight: Boolean) {
    HorizontalDivider(
        thickness = 1.dp,
        color = if (isLight) Color(0xFFF1F3F5) else Color(0xFF181820)
    )
}

@Composable
private fun DevActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isLight: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (isLight) LightTextSecondary else TextSecondary
            )
        }
    }
}
