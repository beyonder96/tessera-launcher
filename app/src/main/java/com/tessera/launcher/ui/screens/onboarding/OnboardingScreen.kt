package com.tessera.launcher.ui.screens.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.design.components.CapsuleButton
import com.tessera.launcher.ui.design.components.SquircleIcon
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography
import com.tessera.launcher.ui.theme.apple.appleSpring
import kotlinx.coroutines.delay

/**
 * Onboarding Oficial Apple "Olá" (Designed in Cupertino).
 * Animação de saudações internacionais com caligrafia Apple e apresentação dos pilares do Tessera 3.0.
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    // Permite que o botão/gesto Back encerre o onboarding imediatamente
    BackHandler {
        onFinish()
    }

    val greetings = listOf("Olá", "Hello", "Bonjour", "Ciao", "Hola")
    var currentGreetingIndex by remember { mutableIntStateOf(0) }
    var isGreetingStage by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repeat(greetings.size) { index ->
            currentGreetingIndex = index
            delay(900)
        }
        isGreetingStage = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.systemBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Toque em qualquer lugar na tela "Olá" avança imediatamente
                if (isGreetingStage) {
                    isGreetingStage = false
                }
            }
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        if (isGreetingStage) {
            // Estágio 1: Saudação Cursiva "Olá" / "Hello"
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = greetings[currentGreetingIndex],
                    transitionSpec = {
                        fadeIn(animationSpec = appleSpring()) togetherWith fadeOut(animationSpec = appleSpring())
                    },
                    label = "greeting_anim"
                ) { greeting ->
                    Text(
                        text = greeting,
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Serif,
                        color = colors.label,
                        textAlign = TextAlign.Center
                    )
                }

                // Dica sutil para avançar
                Text(
                    text = "Toque para continuar",
                    style = typography.caption1,
                    color = colors.tertiaryLabel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                )
            }
        } else {
            // Estágio 2: Apresentação dos Pilares Tessera 3.0
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar com botão "Pular"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Pular",
                        style = typography.subheadline.copy(fontWeight = FontWeight.Medium),
                        color = colors.secondaryLabel,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                runCatching { Taptic.lightImpact(context) }
                                onFinish()
                            }
                            .padding(8.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Bem-vindo ao\nTessera 3.0",
                        style = typography.largeTitle.copy(fontWeight = FontWeight.Bold),
                        color = colors.label,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Designed in Cupertino • Made for Android",
                        style = typography.subheadline,
                        color = colors.secondaryLabel
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Destaques de Recursos
                    OnboardingFeatureRow(
                        icon = Icons.Outlined.AutoAwesome,
                        iconBackground = Color(0xFF007AFF),
                        title = "Liquid Glass & Dynamic Island",
                        description = "Transparência óptica real com desfoque de fundo e ilha dinâmica de mídia e notificações ativas."
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OnboardingFeatureRow(
                        icon = Icons.Outlined.Search,
                        iconBackground = Color(0xFF34C759),
                        title = "Spotlight Search Unificado",
                        description = "Busca inteligente de apps, contatos e IA com barra inferior ergonômica acima do teclado."
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OnboardingFeatureRow(
                        icon = Icons.Outlined.Layers,
                        iconBackground = Color(0xFFAF52DE),
                        title = "Smart Stack & Relógio Hero",
                        description = "Widgets empilháveis com rotação inteligente contextual e relógio monumental personalizável."
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Botão "Começar" de alto contraste e legibilidade
                val buttonBg = if (colors.accent.luminance() > 0.8f) Color(0xFF007AFF) else colors.accent
                val buttonTextColor = if (buttonBg.luminance() > 0.55f) Color(0xFF000000) else Color.White

                CapsuleButton(
                    text = "Começar",
                    onClick = {
                        runCatching { Taptic.lightImpact(context) }
                        onFinish()
                    },
                    backgroundColor = buttonBg,
                    contentColor = buttonTextColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeatureRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    description: String
) {
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        SquircleIcon(
            imageVector = icon,
            backgroundColor = iconBackground,
            size = 44.dp,
            iconSize = 24.dp
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.headline,
                color = colors.label
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = typography.footnote,
                color = colors.secondaryLabel
            )
        }
    }
}
