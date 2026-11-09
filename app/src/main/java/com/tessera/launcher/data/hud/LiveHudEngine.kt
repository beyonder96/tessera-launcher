package com.tessera.launcher.data.hud

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.hud.adapters.AdapterRegistry
import com.tessera.launcher.data.model.HudClassification
import com.tessera.launcher.data.model.HudConfig
import com.tessera.launcher.data.model.HudDecision
import com.tessera.launcher.data.model.HudPeek
import com.tessera.launcher.data.model.HudState
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.ObservedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Orquestrador central e reativo do Live HUD 2.0 ("Ilha Dinâmica Tessera").
 * Executa em thread dedicada em background (Dispatchers.Default com paralelismo serial),
 * garantindo resposta instantânea da interface e baixo consumo de energia.
 */
class LiveHudEngine(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))
) {
    companion object {
        val instance by lazy { LiveHudEngine() }
    }

    private var currentConfig = HudConfig()

    private val _hudState = MutableStateFlow(HudState())
    val hudState: StateFlow<HudState> = _hudState.asStateFlow()

    private val _observedApps = MutableStateFlow<List<ObservedApp>>(emptyList())
    val observedApps: StateFlow<List<ObservedApp>> = _observedApps.asStateFlow()

    // Atividades indexadas pela chave da notificação
    private val activeActivities = mutableMapOf<String, LiveActivityInfo>()

    // Atividades persistentes / sticky (ex: encomendas em rota)
    private val stickyActivities = mutableMapOf<String, LiveActivityInfo>()

    // Controle de Peek ativo
    private var peekDismissJob: Job? = null
    private val recentPeekTimestamps = mutableMapOf<String, Long>()

    // Monitoramento de expiração de atividades sticky
    private var expirationCheckJob: Job? = null

    fun updateConfig(config: HudConfig) {
        scope.launch {
            currentConfig = config
            if (!config.enabled) {
                _hudState.value = HudState()
            } else {
                refreshHudState()
            }
        }
    }

    fun loadInitialStickyActivities(persisted: List<LiveActivityInfo>) {
        scope.launch {
            val now = System.currentTimeMillis()
            persisted.forEach { act ->
                val expires = act.expiresAt ?: Long.MAX_VALUE
                if (expires > now) {
                    stickyActivities[act.id] = act
                }
            }
            refreshHudState()
            startExpirationCheckIfNeeded()
        }
    }

    fun getPersistableStickyActivities(): List<LiveActivityInfo> {
        return stickyActivities.values.toList()
    }

    fun onNotificationPosted(
        sbn: StatusBarNotification,
        ranking: NotificationListenerService.Ranking?,
        context: Context
    ) {
        if (!currentConfig.enabled) return

        scope.launch {
            val snapshot = HudNotificationSnapshot.from(sbn, ranking, context)
            val classification = HudSignalClassifier.classify(snapshot, currentConfig)

            recordObservedApp(snapshot.packageName, snapshot.appName, classification)

            when (classification.decision) {
                HudDecision.LIVE -> {
                    // Se o tipo estiver desativado nas configurações do usuário, ignora
                    val liveActivity = AdapterRegistry.parse(snapshot, sbn, context)
                    if (liveActivity != null && liveActivity.type !in currentConfig.disabledTypes) {
                        if (liveActivity.isSticky) {
                            stickyActivities[liveActivity.id] = liveActivity
                            startExpirationCheckIfNeeded()
                        } else {
                            activeActivities[sbn.key] = liveActivity
                        }
                        refreshHudState()
                    }
                }
                HudDecision.PEEK -> {
                    if (currentConfig.peekEnabled) {
                        triggerPeek(snapshot, sbn.notification, context)
                    }
                }
                HudDecision.IGNORE -> {
                    // Se uma notificação deixou de ser Live, remove se já estava ativa
                    if (activeActivities.remove(sbn.key) != null) {
                        refreshHudState()
                    }
                }
            }
        }
    }

    fun onNotificationRemoved(key: String) {
        scope.launch {
            // Notificações contínuas normais somem ao serem removidas
            if (activeActivities.remove(key) != null) {
                refreshHudState()
            }
            // Encomendas e atividades com isSticky NÃO somem no onNotificationRemoved,
            // permanecem até a expiração (TTL) ou dispensa manual
        }
    }

    fun dismissActivity(id: String) {
        scope.launch {
            activeActivities.remove(id)
            stickyActivities.remove(id)
            refreshHudState()
        }
    }

    fun dismissPeek() {
        scope.launch {
            peekDismissJob?.cancel()
            peekDismissJob = null
            _hudState.update { it.copy(peek = null) }
        }
    }

    private fun triggerPeek(
        snapshot: HudNotificationSnapshot,
        notification: Notification?,
        context: Context
    ) {
        val now = System.currentTimeMillis()
        val lastPeek = recentPeekTimestamps[snapshot.packageName] ?: 0L

        // Throttle de 10 segundos por app para evitar poluição visual com rajadas
        if (now - lastPeek < 10_000L) {
            return
        }
        recentPeekTimestamps[snapshot.packageName] = now

        val avatar = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notification?.getLargeIcon()?.loadDrawable(context)?.let { drawableToBitmap(it) }
            } else {
                @Suppress("DEPRECATION")
                notification?.largeIcon
            }
        }.getOrNull()

        val peekItem = HudPeek(
            key = snapshot.key,
            packageName = snapshot.packageName,
            appName = snapshot.appName,
            title = snapshot.title.ifBlank { snapshot.appName },
            text = snapshot.text.ifBlank { snapshot.bigText },
            avatar = avatar,
            brandColor = snapshot.color,
            contentIntent = notification?.contentIntent,
            postedAt = now
        )

        peekDismissJob?.cancel()
        _hudState.update { it.copy(peek = peekItem) }

        peekDismissJob = scope.launch {
            delay(currentConfig.peekDurationMs)
            _hudState.update { it.copy(peek = null) }
            peekDismissJob = null
        }
    }

    private fun refreshHudState() {
        val allActivities = mutableMapOf<String, LiveActivityInfo>()
        allActivities.putAll(stickyActivities)
        allActivities.putAll(activeActivities)

        val filtered = allActivities.values.filter { it.type !in currentConfig.disabledTypes }
        val ranked = HudRanker.rank(filtered)

        _hudState.update { current ->
            current.copy(
                primary = ranked.primary,
                secondary = ranked.secondary,
                others = ranked.others
            )
        }
    }

    private fun recordObservedApp(pkg: String, name: String, classification: HudClassification) {
        _observedApps.update { list ->
            val existing = list.firstOrNull { it.packageName == pkg }
            val updated = ObservedApp(
                packageName = pkg,
                appName = name,
                lastDecision = classification.decision,
                lastReason = classification.reason,
                lastSeen = System.currentTimeMillis(),
                postCount = (existing?.postCount ?: 0) + 1
            )
            val filtered = list.filterNot { it.packageName == pkg }
            listOf(updated) + filtered.take(39) // Mantém os 40 mais recentes
        }
    }

    private fun startExpirationCheckIfNeeded() {
        if (expirationCheckJob != null && expirationCheckJob?.isActive == true) return
        if (stickyActivities.isEmpty()) return

        expirationCheckJob = scope.launch {
            while (stickyActivities.isNotEmpty()) {
                delay(60_000L) // Checa a cada minuto
                val now = System.currentTimeMillis()
                val expiredKeys = stickyActivities.filter { (_, act) ->
                    val exp = act.expiresAt ?: Long.MAX_VALUE
                    exp <= now
                }.keys

                if (expiredKeys.isNotEmpty()) {
                    expiredKeys.forEach { stickyActivities.remove(it) }
                    refreshHudState()
                }
            }
            expirationCheckJob = null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceIn(1, 128) else 64
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceIn(1, 128) else 64
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
