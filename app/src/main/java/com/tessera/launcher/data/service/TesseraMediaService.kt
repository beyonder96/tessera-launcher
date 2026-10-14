package com.tessera.launcher.data.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import com.tessera.launcher.data.helper.LiveActivityParser
import com.tessera.launcher.data.model.AppNotificationItem
import com.tessera.launcher.data.model.LiveActivityInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MediaPlaybackInfo(
    val title: String = "Spotify",
    val artist: String = "Toque para abrir e reproduzir",
    val isPlaying: Boolean = false,
    val packageName: String? = "com.spotify.music",
    val hasActiveSession: Boolean = false,
    val artwork: Bitmap? = null
)

class TesseraMediaService : NotificationListenerService() {

    companion object {
        val KNOWN_MUSIC_PACKAGES = setOf(
            "com.spotify.music",
            "com.google.android.apps.youtube.music",
            "com.deezer.android.app",
            "com.apple.android.music",
            "com.amazon.mp3",
            "com.soundcloud.android",
            "com.audiomack",
            "com.maxmpz.audioplayer"
        )

        val IGNORED_MEDIA_PACKAGES = setOf(
            "ai.perplexity.app",
            "com.android.chrome",
            "com.google.android.googlequicksearchbox",
            "org.mozilla.firefox",
            "com.microsoft.emmx"
        )

        private var serviceInstance: TesseraMediaService? = null

        private val _mediaState = MutableStateFlow(MediaPlaybackInfo())
        val mediaState: StateFlow<MediaPlaybackInfo> = _mediaState.asStateFlow()

        private val _liveActivities = MutableStateFlow<Map<String, LiveActivityInfo>>(emptyMap())
        private val _topLiveActivity = MutableStateFlow<LiveActivityInfo?>(null)
        val topLiveActivity: StateFlow<LiveActivityInfo?> = _topLiveActivity.asStateFlow()

        private val _activeNotifications = MutableStateFlow<Map<String, AppNotificationItem>>(emptyMap())
        private val _recentNotifications = MutableStateFlow<List<AppNotificationItem>>(emptyList())
        val recentNotifications: StateFlow<List<AppNotificationItem>> = _recentNotifications.asStateFlow()

        private val _notificationCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
        val notificationCounts: StateFlow<Map<String, Int>> = _notificationCounts.asStateFlow()

        private var activeController: MediaController? = null

        fun isNotificationAccessGranted(context: Context): Boolean {
            return NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        }

        fun togglePlayPause() {
            activeController?.let { controller ->
                val state = controller.playbackState?.state
                if (state == PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause()
                } else {
                    controller.transportControls.play()
                }
            }
        }

        fun skipNext() {
            activeController?.transportControls?.skipToNext()
        }

        fun skipPrevious() {
            activeController?.transportControls?.skipToPrevious()
        }

        private fun refreshTopLiveActivity() {
            val top = _liveActivities.value.values.maxByOrNull { it.priorityScore }
            _topLiveActivity.value = top
        }

        private fun updateNotificationsState() {
            val items = _activeNotifications.value.values.toList()
            _recentNotifications.value = items.sortedByDescending { it.timestamp }
            val counts = mutableMapOf<String, Int>()
            items.forEach { item ->
                counts[item.packageName] = (counts[item.packageName] ?: 0) + 1
            }
            _notificationCounts.value = counts
        }

        fun dismissNotification(key: String) {
            try {
                serviceInstance?.cancelNotification(key)
            } catch (_: Exception) {}
            val current = _activeNotifications.value.toMutableMap()
            if (current.remove(key) != null) {
                _activeNotifications.value = current
                updateNotificationsState()
            }
        }

        fun dismissAllNotifications() {
            try {
                serviceInstance?.cancelAllNotifications()
            } catch (_: Exception) {}
            _activeNotifications.value = emptyMap()
            updateNotificationsState()
        }

        fun parseNotificationItem(sbn: StatusBarNotification, context: Context): AppNotificationItem? {
            val pkg = sbn.packageName ?: return null
            if (pkg == context.packageName) return null
            val notification = sbn.notification ?: return null

            // Ignora notificações persistentes de sistema do Android
            val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
            if (pkg == "android" && isOngoing) return null

            val extras = notification.extras ?: return null
            val isGroupSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0

            val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
            val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
                ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()
                ?: ""

            if (rawTitle.isBlank() && rawText.isBlank()) return null
            if (isGroupSummary && rawText.isBlank()) return null

            val appName = runCatching {
                val pm = context.packageManager
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            }.getOrDefault(pkg)

            val largeIcon: Bitmap? = runCatching {
                notification.getLargeIcon()?.loadDrawable(context)?.let { drawable ->
                    if (drawable is BitmapDrawable && drawable.bitmap != null) {
                        drawable.bitmap
                    } else {
                        val w = drawable.intrinsicWidth.coerceIn(1, 192)
                        val h = drawable.intrinsicHeight.coerceIn(1, 192)
                        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bmp)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        bmp
                    }
                }
            }.getOrNull()

            val timestamp = if (sbn.postTime > 0) sbn.postTime else System.currentTimeMillis()

            return AppNotificationItem(
                key = sbn.key,
                packageName = pkg,
                appName = appName,
                title = rawTitle.ifBlank { appName },
                text = rawText,
                timestamp = timestamp,
                largeIcon = largeIcon,
                contentIntent = notification.contentIntent,
                isOngoing = isOngoing
            )
        }
    }

    private var mediaSessionManager: MediaSessionManager? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        serviceInstance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceInstance == this) {
            serviceInstance = null
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val activity = LiveActivityParser.parse(sbn, this)
        if (activity != null) {
            val current = _liveActivities.value.toMutableMap()
            current[sbn.key] = activity
            _liveActivities.value = current
            refreshTopLiveActivity()
        }

        val notifItem = parseNotificationItem(sbn, this)
        if (notifItem != null) {
            val current = _activeNotifications.value.toMutableMap()
            current[sbn.key] = notifItem
            _activeNotifications.value = current
            updateNotificationsState()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return

        if (_liveActivities.value.containsKey(sbn.key)) {
            val current = _liveActivities.value.toMutableMap()
            current.remove(sbn.key)
            _liveActivities.value = current
            refreshTopLiveActivity()
        }

        if (_activeNotifications.value.containsKey(sbn.key)) {
            val current = _activeNotifications.value.toMutableMap()
            current.remove(sbn.key)
            _activeNotifications.value = current
            updateNotificationsState()
        }
    }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateFromActiveController()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateFromActiveController()
        }

        override fun onSessionDestroyed() {
            updateActiveController()
        }
    }

    private val sessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateActiveController(controllers)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        serviceInstance = this
        try {
            mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val component = ComponentName(this, TesseraMediaService::class.java)
            mediaSessionManager?.addOnActiveSessionsChangedListener(sessionsChangedListener, component)
            val controllers = mediaSessionManager?.getActiveSessions(component)
            updateActiveController(controllers)
        } catch (_: SecurityException) {}

        try {
            val currentNotifs = mutableMapOf<String, AppNotificationItem>()
            activeNotifications?.forEach { sbn ->
                LiveActivityParser.parse(sbn, this)?.let { act ->
                    val current = _liveActivities.value.toMutableMap()
                    current[sbn.key] = act
                    _liveActivities.value = current
                }
                parseNotificationItem(sbn, this)?.let { item ->
                    currentNotifs[sbn.key] = item
                }
            }
            _activeNotifications.value = currentNotifs
            updateNotificationsState()
            refreshTopLiveActivity()
        } catch (_: Exception) {}
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (serviceInstance == this) {
            serviceInstance = null
        }
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsChangedListener)
        } catch (_: Exception) {}
        activeController?.unregisterCallback(controllerCallback)
        activeController = null
    }

    private fun updateActiveController(controllers: List<MediaController>? = null) {
        val list = controllers ?: runCatching {
            val component = ComponentName(this, TesseraMediaService::class.java)
            mediaSessionManager?.getActiveSessions(component)
        }.getOrNull()

        activeController?.unregisterCallback(controllerCallback)

        // 1. Procura primeiro sessão de app de música tocando (ex: Spotify tocando)
        var selected = list?.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING &&
                    KNOWN_MUSIC_PACKAGES.contains(it.packageName)
        }

        // 2. Procura qualquer sessão tocando que NÃO seja ignorada (navegadores/IA)
        if (selected == null) {
            selected = list?.firstOrNull {
                it.playbackState?.state == PlaybackState.STATE_PLAYING &&
                        !IGNORED_MEDIA_PACKAGES.contains(it.packageName)
            }
        }

        // 3. Procura sessão de app de música pausada
        if (selected == null) {
            selected = list?.firstOrNull {
                KNOWN_MUSIC_PACKAGES.contains(it.packageName)
            }
        }

        activeController = selected
        selected?.registerCallback(controllerCallback, handler)
        updateFromActiveController()
    }

    private fun updateFromActiveController() {
        val controller = activeController
        if (controller == null) {
            _mediaState.value = MediaPlaybackInfo(
                title = "Spotify",
                artist = "Toque para abrir e reproduzir",
                isPlaying = false,
                packageName = "com.spotify.music",
                hasActiveSession = false
            )
            return
        }

        val metadata = controller.metadata
        val playbackState = controller.playbackState
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: "Mídia em Reprodução"

        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            ?: "Spotify"

        val artwork: Bitmap? = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: metadata?.description?.iconBitmap
            ?: metadata?.description?.iconUri?.let { uri ->
                runCatching {
                    contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
            }

        _mediaState.value = MediaPlaybackInfo(
            title = title,
            artist = artist,
            isPlaying = isPlaying,
            packageName = controller.packageName,
            hasActiveSession = true,
            artwork = artwork
        )
    }
}
