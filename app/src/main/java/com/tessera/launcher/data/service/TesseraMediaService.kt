package com.tessera.launcher.data.service

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MediaPlaybackInfo(
    val title: String = "Nenhuma mídia reproduzindo",
    val artist: String = "Toque para abrir reprodutor",
    val isPlaying: Boolean = false,
    val packageName: String? = null,
    val hasActiveSession: Boolean = false
)

class TesseraMediaService : NotificationListenerService() {

    companion object {
        private val _mediaState = MutableStateFlow(MediaPlaybackInfo())
        val mediaState: StateFlow<MediaPlaybackInfo> = _mediaState.asStateFlow()

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
    }

    private var mediaSessionManager: MediaSessionManager? = null
    private val handler = Handler(Looper.getMainLooper())

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
        try {
            mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val component = ComponentName(this, TesseraMediaService::class.java)
            mediaSessionManager?.addOnActiveSessionsChangedListener(sessionsChangedListener, component)
            val controllers = mediaSessionManager?.getActiveSessions(component)
            updateActiveController(controllers)
        } catch (_: SecurityException) {
            // Permissão ainda não concedida
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
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

        // Prioriza o controller que está tocando
        val selected = list?.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: list?.firstOrNull()

        activeController = selected
        selected?.registerCallback(controllerCallback, handler)
        updateFromActiveController()
    }

    private fun updateFromActiveController() {
        val controller = activeController
        if (controller == null) {
            _mediaState.value = MediaPlaybackInfo(
                title = "Nenhuma mídia reproduzindo",
                artist = "Toque para abrir reprodutor",
                isPlaying = false,
                packageName = null,
                hasActiveSession = false
            )
            return
        }

        val metadata = controller.metadata
        val playbackState = controller.playbackState
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: "Reprodução Ativa"

        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            ?: controller.packageName

        _mediaState.value = MediaPlaybackInfo(
            title = title,
            artist = artist,
            isPlaying = isPlaying,
            packageName = controller.packageName,
            hasActiveSession = true
        )
    }
}
