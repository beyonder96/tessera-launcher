package com.tessera.launcher.data.model

import android.app.PendingIntent
import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

/**
 * Decisão do classificador para uma notificação ou evento recebido.
 */
enum class HudDecision {
    LIVE,   // Deve aparecer como Live Activity contínua na ilha
    PEEK,   // Deve aparecer temporariamente como pop-up peek (~4s) e sumir
    IGNORE  // Notificação de segundo plano / ruído / ignorada pelo usuário
}

/**
 * Regra configurável pelo usuário para um aplicativo específico.
 */
enum class HudAppRule {
    AUTO,           // Classificação automática inteligente (padrão)
    LIVE_AND_PEEK,  // Permite tanto Live Activities quanto peeks de notificações
    LIVE_ONLY,      // Permite apenas Live Activities contínuas (silencia peeks deste app)
    PEEK_ONLY,      // Trata apenas como peek temporário (não deixa ficar grudado)
    OFF             // Totalmente desativado na ilha (bloqueia Live e Peek)
}

/**
 * Motivo detalhado pelo qual uma decisão foi tomada (usado para transparência nas configurações).
 */
enum class HudReason {
    USER_RULE,             // Sobrescrito explicitamente por regra configurada pelo usuário
    NATIVE_LIVE_UPDATE,    // Live Update nativa do Android 16 (ProgressStyle / requestPromotedOngoing)
    CALL_STYLE,            // Chamada de voz/vídeo ativa (CallStyle ou CATEGORY_CALL)
    KNOWN_ADAPTER,         // Reconhecido por adapter dedicado (Uber, 99, iFood, Maps, etc.)
    CATEGORY,              // Categoria de sistema elegível (Transport, Navigation, Stopwatch, etc.)
    CHRONOMETER,           // Contagem regressiva / cronômetro ao vivo detectado
    PROGRESS,              // Barra de progresso determinada com avanço percentual
    PACKAGE_TRACKING,      // Rastreamento de encomenda de e-commerce em rota de entrega
    MESSAGING,             // Mensagem pessoal ou notificação de alta relevância (Peek)
    BACKGROUND_SERVICE,    // Serviço persistente em segundo plano / status passivo (Ignorado)
    LOW_IMPORTANCE,        // Notificação de baixa prioridade / sem relevância de tempo real
    SYSTEM,                // Notificação de sistema Android pura (USB, bateria do sistema, etc.)
    MEDIA_SESSION,         // App de música/mídia (já controlado nativamente pelo MediaSession)
    GROUP_SUMMARY,         // Resumo de grupo sem texto novo
    DND                    // Silenciado pelo modo Não Perturbe do sistema
}

/**
 * Resultado da classificação com nível de confiança (0..100) e justificativa.
 */
@Immutable
data class HudClassification(
    val decision: HudDecision,
    val confidence: Int,
    val reason: HudReason
)

/**
 * Peek temporário exibido na ilha (estilo Dynamic Island) para alertas importantes.
 */
@Immutable
data class HudPeek(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val avatar: Bitmap? = null,
    val brandColor: Int? = null,
    val contentIntent: PendingIntent? = null,
    val postedAt: Long = System.currentTimeMillis()
)

/**
 * Estado unificado da Ilha Dinâmica Tessera exposto para a UI.
 */
@Immutable
data class HudState(
    val primary: LiveActivityInfo? = null,
    val secondary: LiveActivityInfo? = null,
    val others: List<LiveActivityInfo> = emptyList(),
    val peek: HudPeek? = null
) {
    val hasActiveActivity: Boolean get() = primary != null
    val isSplit: Boolean get() = primary != null && secondary != null
    val totalActivitiesCount: Int get() = (if (primary != null) 1 else 0) + (if (secondary != null) 1 else 0) + others.size
}

/**
 * Registro de aplicativo observado pelo motor para a tela de configurações / transparência.
 */
@Immutable
data class ObservedApp(
    val packageName: String,
    val appName: String,
    val lastDecision: HudDecision,
    val lastReason: HudReason,
    val lastSeen: Long = System.currentTimeMillis(),
    val postCount: Int = 1
)

/**
 * Configurações completas do Live HUD persistidas nas preferências.
 */
@Immutable
data class HudConfig(
    val enabled: Boolean = true,
    val peekEnabled: Boolean = true,
    val peekDurationMs: Long = 4000L,
    val hidePeekContent: Boolean = false,
    val showMedia: Boolean = true,
    val showCharging: Boolean = true,
    val haptics: Boolean = true,
    val disabledTypes: Set<LiveActivityType> = emptySet(),
    val appRules: Map<String, HudAppRule> = emptyMap()
)
