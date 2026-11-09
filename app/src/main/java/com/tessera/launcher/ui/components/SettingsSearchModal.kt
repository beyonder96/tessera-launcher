package com.tessera.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.state.SettingsSubScreen
import com.tessera.launcher.ui.theme.AmoledBlack
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.LightBackground
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.Normalizer

/**
 * Item individual de configuração indexado para busca inteligente.
 */
data class SettingIndexItem(
    val id: String,
    val title: String,
    val description: String,
    val sectionName: String,
    val targetSubScreen: SettingsSubScreen,
    val icon: ImageVector,
    val keywords: List<String>
)

/**
 * Catálogo completo de recursos das configurações do Tessera Launcher.
 */
object SettingsCatalog {
    val items = listOf(
        // 1. Personalização
        SettingIndexItem(
            id = "custom_wallpaper",
            title = "Papel de Parede & Desfoque",
            description = "Alterar imagem de fundo, blur da tela inicial e escurecimento do wallpaper.",
            sectionName = "Personalização",
            targetSubScreen = SettingsSubScreen.CUSTOMIZATION,
            icon = Icons.Outlined.Palette,
            keywords = listOf("wallpaper", "papel de parede", "fundo", "imagem", "plano de fundo", "blur", "desfoque", "foto de fundo")
        ),
        SettingIndexItem(
            id = "custom_icons",
            title = "Ícones & Rótulos de Apps",
            description = "Pacotes de ícones (Icon Packs), tamanho dos ícones e exibição de nomes dos apps.",
            sectionName = "Personalização",
            targetSubScreen = SettingsSubScreen.CUSTOMIZATION,
            icon = Icons.Outlined.Palette,
            keywords = listOf("icones", "icons", "icon pack", "pacote de icones", "tamanho", "rotulos", "esconder nomes", "nomes", "aparencia")
        ),
        SettingIndexItem(
            id = "custom_theme",
            title = "Tema & Modo AMOLED",
            description = "Alternar entre Modo Claro, Escuro e Preto Puro AMOLED, além de paletas de cores de acento.",
            sectionName = "Personalização",
            targetSubScreen = SettingsSubScreen.CUSTOMIZATION,
            icon = Icons.Outlined.Palette,
            keywords = listOf("tema", "escuro", "claro", "amoled", "preto", "cor", "cores", "accent", "destaque", "dark", "light", "modo escuro")
        ),
        SettingIndexItem(
            id = "custom_dock",
            title = "Barra de Pesquisa (Dock Inferior)",
            description = "Personalizar texto do dock, estilo pílula dividida (Split Pill), opacidade e fixação.",
            sectionName = "Personalização",
            targetSubScreen = SettingsSubScreen.CUSTOMIZATION,
            icon = Icons.Outlined.Tune,
            keywords = listOf("dock", "barra de pesquisa", "pesquisa", "barra inferior", "split pill", "opacidade", "texto customizado", "lupa")
        ),
        SettingIndexItem(
            id = "custom_clock",
            title = "Estilo do Relógio da Home",
            description = "Relógio Flip mecânico 3D, editorial, minimalista, formato de 12h/24h e alinhamento.",
            sectionName = "Personalização",
            targetSubScreen = SettingsSubScreen.CUSTOMIZATION,
            icon = Icons.Outlined.Palette,
            keywords = listOf("relogio", "clock", "flip clock", "mecanico", "horas", "data", "hora", "editorial", "estilo do relogio")
        ),
        SettingIndexItem(
            id = "custom_liquid_glass",
            title = "Efeito Vidro Líquido (Liquid Glass)",
            description = "Ativar bordas especulares com física óptica e acabamento de vidro na interface.",
            sectionName = "Personalização",
            targetSubScreen = SettingsSubScreen.CUSTOMIZATION,
            icon = Icons.Outlined.Opacity,
            keywords = listOf("vidro liquido", "liquid glass", "vidro", "glass", "reflexo", "brilho", "especular", "apple glass")
        ),
        SettingIndexItem(
            id = "live_hud",
            title = "Live HUD (Ilha Dinâmica)",
            description = "Atividades ao vivo, entregas, encomendas, peeks de notificação e regras por app.",
            sectionName = "Personalização",
            targetSubScreen = SettingsSubScreen.LIVE_HUD,
            icon = Icons.Outlined.NotificationsActive,
            keywords = listOf("live hud", "hud", "ilha dinamica", "dynamic island", "capsule", "peek", "notificacao", "notificacoes", "entrega", "ifood", "uber", "encomendas", "correios", "mercado livre")
        ),

        // 2. Busca & IA
        SettingIndexItem(
            id = "search_ai_engine",
            title = "Provedores de Inteligência Artificial",
            description = "Configurar chave de API gratuita do Google Gemini ou Groq (Llama 3.3).",
            sectionName = "Busca & IA",
            targetSubScreen = SettingsSubScreen.SEARCH,
            icon = Icons.Outlined.AutoAwesome,
            keywords = listOf("ia", "ai", "gemini", "groq", "llama", "chave de api", "api key", "inteligencia artificial", "respostas", "consultar ia")
        ),
        SettingIndexItem(
            id = "search_web_providers",
            title = "Motores de Busca na Web",
            description = "Ativar atalhos para Google, YouTube, Play Store e Google Maps na busca.",
            sectionName = "Busca & IA",
            targetSubScreen = SettingsSubScreen.SEARCH,
            icon = Icons.Outlined.Tune,
            keywords = listOf("busca web", "motores", "google", "youtube", "maps", "play store", "pesquisar na internet")
        ),
        SettingIndexItem(
            id = "search_omni",
            title = "Omni-Search (Cálculos & Conversões)",
            description = "Cálculo instantâneo de expressões matemáticas e conversão direta no dock.",
            sectionName = "Busca & IA",
            targetSubScreen = SettingsSubScreen.SEARCH,
            icon = Icons.Outlined.Code,
            keywords = listOf("omni", "calculadora", "calc", "contas", "conversoes", "matematica", "moedas")
        ),

        // 3. Central & Feed (Tela -1)
        SettingIndexItem(
            id = "panel_widgets",
            title = "Central de Widgets Bento (Tela −1)",
            description = "Configurar widgets laterais de clima, bateria, player de música e atalhos de apps.",
            sectionName = "Central & Feed",
            targetSubScreen = SettingsSubScreen.WIDGETS_CENTER,
            icon = Icons.Outlined.Dashboard,
            keywords = listOf("widgets", "central de widgets", "painel lateral", "tela -1", "bento", "clima", "bateria", "musica", "cards")
        ),
        SettingIndexItem(
            id = "panel_feed",
            title = "Feed de Notícias RSS",
            description = "Adicionar fontes de notícias personalizadas ou habilitar leitura rápida na tela lateral.",
            sectionName = "Central & Feed",
            targetSubScreen = SettingsSubScreen.FEED,
            icon = Icons.Outlined.RssFeed,
            keywords = listOf("feed", "noticias", "rss", "artigos", "jornais", "tecnologia", "desativar feed")
        ),

        // 4. Gestos
        SettingIndexItem(
            id = "gestures_home",
            title = "Gestos da Tela Inicial",
            description = "Deslizar para cima, toque duplo para bloquear tela e gestos de pinça.",
            sectionName = "Gestos",
            targetSubScreen = SettingsSubScreen.GESTURES,
            icon = Icons.Outlined.TouchApp,
            keywords = listOf("gestos", "swipe", "deslizar", "toque duplo", "bloquear tela", "lock screen", "pinca", "atalhos de toque")
        ),

        // 5. Transparência & Desfoque
        SettingIndexItem(
            id = "transparency_drawer",
            title = "Transparência da Gaveta de Apps",
            description = "Ajustar o nível de desfoque (blur) e opacidade do vidro fosco ao abrir a gaveta.",
            sectionName = "Transparência",
            targetSubScreen = SettingsSubScreen.TRANSPARENCY,
            icon = Icons.Outlined.Opacity,
            keywords = listOf("transparencia", "vidro fosco", "frosted glass", "opacidade", "blur da gaveta", "gaveta transparente")
        ),

        // 6. Extras & Produtividade
        SettingIndexItem(
            id = "extras_zen_mode",
            title = "Modo Foco Zen",
            description = "Temporizador minimalista para eliminar distrações e manter concentração total.",
            sectionName = "Extras & Foco",
            targetSubScreen = SettingsSubScreen.EXTRAS,
            icon = Icons.Outlined.HourglassEmpty,
            keywords = listOf("zen", "foco", "modo foco", "produtividade", "bloquear distracoes", "pomodoro", "timer")
        ),
        SettingIndexItem(
            id = "extras_standby",
            title = "Modo Repouso (Standby)",
            description = "Relógio flip de cabeceira com foto, música em reprodução e chip de clima/bateria.",
            sectionName = "Extras & Foco",
            targetSubScreen = SettingsSubScreen.EXTRAS,
            icon = Icons.Outlined.Extension,
            keywords = listOf("standby", "modo repouso", "relogio de cabeceira", "carregador", "foto standby", "relogio flip")
        ),

        // 7. Pastas de Aplicativos
        SettingIndexItem(
            id = "folders_manage",
            title = "Pastas de Aplicativos",
            description = "Criar novas pastas, renomear e agrupar aplicativos para organizar sua gaveta.",
            sectionName = "Pastas",
            targetSubScreen = SettingsSubScreen.FOLDERS,
            icon = Icons.Outlined.Folder,
            keywords = listOf("pastas", "folders", "agrupar", "organizacao", "criar pasta", "adicionar apps na pasta")
        ),

        // 8. Comandos Rápidos
        SettingIndexItem(
            id = "commands_manage",
            title = "Comandos & Atalhos Rápidos",
            description = "Configurar prefixos para acionar IA, contatos, calculadora e pastas na barra de busca.",
            sectionName = "Comandos",
            targetSubScreen = SettingsSubScreen.COMMANDS,
            icon = Icons.Outlined.Code,
            keywords = listOf("comandos", "atalhos", "prefixos", "@ai", "@calc", "@con", "simbolo de comando")
        ),

        // 9. Aplicativos Ocultos
        SettingIndexItem(
            id = "hidden_apps_manage",
            title = "Aplicativos Ocultos",
            description = "Esconder aplicativos confidenciais da gaveta principal (bancos, mensagens, etc.).",
            sectionName = "Privacidade",
            targetSubScreen = SettingsSubScreen.HIDDEN_APPS,
            icon = Icons.Outlined.VisibilityOff,
            keywords = listOf("ocultar", "esconder apps", "hidden", "privacidade", "banco", "esconder aplicativos")
        ),

        // 10. Permissões
        SettingIndexItem(
            id = "permissions_manage",
            title = "Permissões do Sistema",
            description = "Definir como Launcher Padrão, gerenciar acessibilidade e acesso a notificações.",
            sectionName = "Permissões",
            targetSubScreen = SettingsSubScreen.PERMISSIONS,
            icon = Icons.Outlined.Security,
            keywords = listOf("permissoes", "launcher padrao", "acessibilidade", "notificacoes", "contatos", "calendario")
        ),

        // 11. Desenvolvedor & Atualizações
        SettingIndexItem(
            id = "developer_updates",
            title = "Atualizações & Versão do Launcher",
            description = "Verificar novidades via GitHub Releases, fazer backup e restaurar configurações.",
            sectionName = "Sobre & Sistema",
            targetSubScreen = SettingsSubScreen.DEVELOPER,
            icon = Icons.Outlined.Tune,
            keywords = listOf("atualizacoes", "updates", "versao", "github", "release", "backup", "restaurar", "sobre")
        )
    )

    private fun removeAccents(str: String): String {
        val nfd = Normalizer.normalize(str, Normalizer.Form.NFD)
        return Regex("\\p{InCombiningDiacriticalMarks}+").replace(nfd, "").lowercase()
    }

    /**
     * Busca rápida e ranqueada por correspondência de termos e sinônimos.
     */
    fun search(query: String): List<SettingIndexItem> {
        val clean = removeAccents(query.trim())
        if (clean.isBlank()) return items

        val tokens = clean.split(" ").filter { it.length >= 2 }

        return items.mapNotNull { item ->
            val normTitle = removeAccents(item.title)
            val normDesc = removeAccents(item.description)
            val normSection = removeAccents(item.sectionName)
            val normKeywords = item.keywords.map { removeAccents(it) }

            var score = 0

            // Correspondência exata no título ou palavras-chave
            if (normTitle.contains(clean)) score += 50
            if (normKeywords.any { it.contains(clean) }) score += 40
            if (normDesc.contains(clean)) score += 20
            if (normSection.contains(clean)) score += 15

            // Correspondência por tokens (palavras individuais)
            for (token in tokens) {
                if (normTitle.contains(token)) score += 15
                if (normKeywords.any { it.contains(token) }) score += 12
                if (normDesc.contains(token)) score += 8
            }

            if (score > 0) item to score else null
        }.sortedByDescending { it.second }.map { it.first }
    }
}

/**
 * Modal de Busca e Assistente de Configurações alimentado por IA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSearchModal(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onNavigateToSubScreen: (SettingsSubScreen) -> Unit,
    isAmoled: Boolean = true,
    isLight: Boolean = false,
    accentColor: Color = Color(0xFF67E8F9),
    onAskAi: (suspend (String) -> String?)? = null
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember(searchQuery) { SettingsCatalog.search(searchQuery) }

    // Estados de resposta da IA
    var isAiLoading by remember { mutableStateOf(false) }
    var aiResponseText by remember { mutableStateOf<String?>(null) }
    var aiSuggestedSubScreen by remember { mutableStateOf<SettingsSubScreen?>(null) }

    val containerBg = when {
        isLight -> Color(0xFFF7F4EB)
        isAmoled -> AmoledBlack
        else -> DarkSurface
    }

    val cardBg = when {
        isLight -> LightCardBackground
        isAmoled -> Color(0xFF0F0F14)
        else -> Color(0xFF181820)
    }

    val cardBorder = when {
        isLight -> LightCardBorder
        isAmoled -> AmoledCardBorder
        else -> Color(0xFF282832)
    }

    val primaryTextColor = if (isLight) LightTextPrimary else TextPrimary
    val secondaryTextColor = if (isLight) LightTextSecondary else TextSecondary

    LaunchedEffect(isOpen) {
        delay(150)
        try {
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (_: Exception) {}
    }

    // Função de consulta ao Assistente IA
    fun requestAiGuidance(prompt: String) {
        val queryText = prompt.trim()
        if (queryText.isBlank()) return

        scope.launch {
            isAiLoading = true
            aiResponseText = null

            // Análise semântica rápida do destino mais provável
            val topMatch = SettingsCatalog.search(queryText).firstOrNull()
            aiSuggestedSubScreen = topMatch?.targetSubScreen ?: SettingsSubScreen.CUSTOMIZATION

            if (onAskAi != null) {
                try {
                    val fullPrompt = "Como assistente do Tessera Launcher, responda em no máximo 1 frase direta e diga onde o usuário deve ir para: '$queryText'."
                    val response = onAskAi(fullPrompt)
                    aiResponseText = response?.ifBlank {
                        "Você pode configurar isso na seção ${topMatch?.sectionName ?: "Personalização"}."
                    }
                } catch (e: Exception) {
                    aiResponseText = "Você pode configurar isso na seção ${topMatch?.sectionName ?: "Personalização"}."
                }
            } else {
                delay(350)
                aiResponseText = "Para isso, acesse ${topMatch?.sectionName ?: "Personalização"}."
            }

            isAiLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerBg,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (isLight) Color(0x30000000) else Color(0x35FFFFFF))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .padding(horizontal = 20.dp)
        ) {
            // Cabeçalho da Busca
            Text(
                text = "Buscar Configurações",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )
            Text(
                text = "Encontre recursos instantaneamente ou pergunte à IA",
                fontSize = 13.sp,
                color = secondaryTextColor,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )

            // Campo de Entrada com Ícone Moderno de Busca e Botão de IA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(CircleShape)
                    .background(cardBg)
                    .border(BorderStroke(1.dp, cardBorder), CircleShape)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModernSearchIcon(
                        tint = secondaryTextColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Diga o que você quer configurar...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = secondaryTextColor.copy(alpha = 0.70f)
                            )
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = primaryTextColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(if (isLight) Color.Black else Color.White),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                    if (searchQuery.isNotBlank()) {
                                        requestAiGuidance(searchQuery)
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        searchQuery = ""
                                        aiResponseText = null
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Limpar",
                                tint = secondaryTextColor,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Botão Assistente IA (Perguntar)
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF67E8F9).copy(alpha = 0.22f),
                                        Color(0xFFA855F7).copy(alpha = 0.22f)
                                    )
                                )
                            )
                            .border(
                                BorderStroke(1.dp, Color(0xFF67E8F9).copy(alpha = 0.45f)),
                                CircleShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    keyboardController?.hide()
                                    requestAiGuidance(searchQuery.ifBlank { "Como personalizar o launcher?" })
                                }
                            )
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = "IA",
                                tint = Color(0xFF67E8F9),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "IA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF67E8F9)
                            )
                        }
                    }
                }
            }

            // Chips de Sugestão Rápida quando a busca estiver limpa
            if (searchQuery.isEmpty()) {
                val suggestions = listOf(
                    "🎨 Papel de Parede",
                    "⚡ Gestos de Toque",
                    "🔒 Ocultar Aplicativos",
                    "🧩 Widgets do Android",
                    "🤖 Configurar IA Gemini",
                    "🌙 Tema AMOLED",
                    "⏳ Modo Foco Zen"
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(suggestions) { tag ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isLight) Color(0x10000000) else Color(0x15FFFFFF))
                                .border(BorderStroke(1.dp, if (isLight) Color(0x15000000) else Color(0x20FFFFFF)), CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        val cleanTag = tag.substringAfter(" ")
                                        searchQuery = cleanTag
                                        requestAiGuidance(cleanTag)
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = primaryTextColor
                            )
                        }
                    }
                }
            }

            // Card de Resposta / Orientação do Assistente IA
            AnimatedVisibility(
                visible = isAiLoading || aiResponseText != null,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(150))
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isLight) Color(0xFFEFF6FF) else Color(0xFF0F1A28),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Assistente de Configurações",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (isAiLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Consultando catálogo de funções...",
                                    fontSize = 13.sp,
                                    color = secondaryTextColor
                                )
                            }
                        } else {
                            aiResponseText?.let { answer ->
                                Text(
                                    text = answer,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp,
                                    color = primaryTextColor
                                )

                                aiSuggestedSubScreen?.let { targetScreen ->
                                    val targetItem = SettingsCatalog.items.firstOrNull { it.targetSubScreen == targetScreen }
                                    val buttonLabel = targetItem?.sectionName ?: "Abrir Seção"

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFF38BDF8).copy(alpha = 0.20f))
                                            .border(BorderStroke(1.dp, Color(0xFF38BDF8)), CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = {
                                                    onDismiss()
                                                    onNavigateToSubScreen(targetScreen)
                                                }
                                            )
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Ir para $buttonLabel",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF38BDF8)
                                            )
                                            Icon(
                                                imageVector = Icons.Outlined.ArrowForward,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de Resultados Encontrados
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = searchResults,
                    key = { it.id }
                ) { item ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onDismiss()
                                    onNavigateToSubScreen(item.targetSubScreen)
                                }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isLight) Color(0x12000000) else Color(0x18FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = primaryTextColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryTextColor
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isLight) Color(0x14000000) else Color(0x20FFFFFF))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.sectionName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = secondaryTextColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = item.description,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = secondaryTextColor,
                                    maxLines = 2
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.ArrowForward,
                                contentDescription = "Acessar",
                                tint = secondaryTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
