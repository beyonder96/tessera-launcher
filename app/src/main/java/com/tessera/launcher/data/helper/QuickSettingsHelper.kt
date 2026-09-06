package com.tessera.launcher.data.helper

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuickSettingsHelper(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _ringerMode = MutableStateFlow(audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL)
    val ringerMode: StateFlow<Int> = _ringerMode.asStateFlow()

    private var cameraId: String? = null

    init {
        runCatching {
            cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            cameraManager?.registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(camId: String, enabled: Boolean) {
                    if (camId == cameraId) {
                        _isTorchOn.value = enabled
                    }
                }
            }, null)
        }
    }

    fun toggleTorch() {
        val target = !_isTorchOn.value
        val id = cameraId ?: return
        runCatching {
            cameraManager?.setTorchMode(id, target)
            _isTorchOn.value = target
        }
    }

    fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun openBluetoothSettings() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun cycleRingerMode() {
        val am = audioManager ?: return
        val newMode = when (am.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
            AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
            else -> AudioManager.RINGER_MODE_NORMAL
        }
        runCatching {
            am.ringerMode = newMode
            _ringerMode.value = am.ringerMode
        }
    }
}
