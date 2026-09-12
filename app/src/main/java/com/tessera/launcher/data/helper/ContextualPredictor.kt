package com.tessera.launcher.data.helper

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.data.preference.LauncherPreferences
import org.json.JSONObject
import java.util.Calendar

/**
 * Motor de Previsão Contextual de Aplicativos (Smart Dock).
 *
 * Utiliza heurística local (Markov / contagem de frequência e recência) considerando:
 * 1. Slot de horário do dia (Manhã, Tarde, Noite, Madrugada).
 * 2. Conexão de fones de ouvido ou dispositivos de áudio Bluetooth.
 * 3. Histórico real de hábitos do usuário.
 * 4. Fallback semântico (cold-start) via AppSemanticIndex para início imediato.
 */
class ContextualPredictor(
    private val preferences: LauncherPreferences
) {
    private val semanticIndex = AppSemanticIndex()

    enum class TimeSlot(val key: String) {
        MORNING("morning"),       // 06h - 12h
        AFTERNOON("afternoon"),   // 12h - 18h
        EVENING("evening"),       // 18h - 23h
        NIGHT("night")            // 23h - 06h
    }

    /**
     * Retorna o slot de tempo atual com base na hora do sistema.
     */
    fun getCurrentTimeSlot(): TimeSlot {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..11 -> TimeSlot.MORNING
            in 12..17 -> TimeSlot.AFTERNOON
            in 18..22 -> TimeSlot.EVENING
            else -> TimeSlot.NIGHT
        }
    }

    /**
     * Detecta se fones de ouvido (cabo ou Bluetooth de áudio) estão conectados ao dispositivo.
     */
    fun isAudioOutputConnected(context: Context): Boolean {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                devices.any { device ->
                    when (device.type) {
                        AudioDeviceInfo.TYPE_WIRED_HEADSET,
                        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                        AudioDeviceInfo.TYPE_USB_HEADSET -> true
                        else -> false
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.isWiredHeadsetOn
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Registra a abertura de um aplicativo para alimentar o histórico contextual.
     */
    fun recordAppLaunch(packageName: String, context: Context) {
        if (packageName.isBlank()) return
        val currentSlot = getCurrentTimeSlot()
        val isAudioConnected = isAudioOutputConnected(context)

        try {
            val historyJson = preferences.getAppLaunchHistoryJson() ?: "{}"
            val root = JSONObject(historyJson)
            val pkgObj = root.optJSONObject(packageName) ?: JSONObject()

            // Incrementa contador do slot de tempo atual
            val slotCount = pkgObj.optInt(currentSlot.key, 0) + 1
            pkgObj.put(currentSlot.key, slotCount)

            // Incrementa contador de uso com fones de ouvido se conectado
            if (isAudioConnected) {
                val headsetCount = pkgObj.optInt("headset", 0) + 1
                pkgObj.put("headset", headsetCount)
            }

            // Total de aberturas gerais
            val totalCount = pkgObj.optInt("total", 0) + 1
            pkgObj.put("total", totalCount)

            // Último timestamp
            pkgObj.put("lastLaunch", System.currentTimeMillis())

            root.put(packageName, pkgObj)
            preferences.setAppLaunchHistoryJson(root.toString())
        } catch (_: Exception) {}
    }

    /**
     * Retorna a lista de aplicativos previstos para o contexto atual.
     */
    fun getPredictedApps(
        allApps: List<AppInfo>,
        hiddenPackages: Set<String>,
        count: Int = 4,
        context: Context
    ): List<AppInfo> {
        val targetCount = count.coerceIn(3, 5)
        val eligibleApps = allApps.filterNot { hiddenPackages.contains(it.packageName) }
        if (eligibleApps.isEmpty()) return emptyList()

        val currentSlot = getCurrentTimeSlot()
        val isAudioConnected = isAudioOutputConnected(context)
        val historyJson = preferences.getAppLaunchHistoryJson() ?: "{}"
        val root = try { JSONObject(historyJson) } catch (_: Exception) { JSONObject() }

        val scoredApps = mutableListOf<Pair<AppInfo, Double>>()

        for (app in eligibleApps) {
            val pkg = app.packageName
            val pkgObj = root.optJSONObject(pkg)
            var score = 0.0

            if (pkgObj != null) {
                val slotFrequency = pkgObj.optInt(currentSlot.key, 0)
                val headsetFrequency = pkgObj.optInt("headset", 0)
                val totalFrequency = pkgObj.optInt("total", 0)
                val lastLaunch = pkgObj.optLong("lastLaunch", 0L)

                // Peso do slot atual (forte correlação temporal)
                score += slotFrequency * 4.0

                // Peso do fone de ouvido
                if (isAudioConnected) {
                    score += headsetFrequency * 6.0
                }

                // Peso de recência (usado nas últimas 24h)
                val recencyHours = (System.currentTimeMillis() - lastLaunch) / (1000 * 60 * 60)
                if (recencyHours in 0..24) {
                    score += (24 - recencyHours) * 0.2
                }

                score += totalFrequency * 0.5
            }

            // Heurística contextual semântica adicional / cold-start
            val semanticBonus = getSemanticContextBonus(app, currentSlot, isAudioConnected)
            score += semanticBonus

            scoredApps.add(Pair(app, score))
        }

        // Ordenar por maior pontuação
        val sorted = scoredApps
            .filter { it.second > 0.0 }
            .sortedByDescending { it.second }
            .map { it.first }
            .toMutableList()

        // Se ainda não atingiu a quantidade desejada (ex: primeiro uso), preenche com os primeiros apps
        if (sorted.size < targetCount) {
            val remaining = eligibleApps.filterNot { sorted.contains(it) }
            sorted.addAll(remaining.take(targetCount - sorted.size))
        }

        return sorted.take(targetCount)
    }

    /**
     * Bônus semântico com base nas intenções típicas do horário e dispositivos.
     */
    private fun getSemanticContextBonus(app: AppInfo, slot: TimeSlot, isAudioConnected: Boolean): Double {
        var bonus = 0.0

        if (isAudioConnected) {
            // Prioriza tocadores de música, podcast e áudio quando fone conectado
            val audioScore = semanticIndex.scoreApp(app, "musica", "musica") +
                    semanticIndex.scoreApp(app, "podcast", "podcast")
            if (audioScore > 0) bonus += 8.0
        }

        when (slot) {
            TimeSlot.MORNING -> {
                // Manhã: Notícias, Clima, Transporte, Agenda, Mensagens
                if (semanticIndex.scoreApp(app, "clima", "clima") > 0) bonus += 3.0
                if (semanticIndex.scoreApp(app, "noticias", "noticias") > 0) bonus += 3.0
                if (semanticIndex.scoreApp(app, "transporte", "transporte") > 0) bonus += 2.5
                if (semanticIndex.scoreApp(app, "calendario", "calendario") > 0) bonus += 2.0
            }
            TimeSlot.AFTERNOON -> {
                // Tarde: E-mail, Produtividade, Banco, Navegador, Trabalho
                if (semanticIndex.scoreApp(app, "email", "email") > 0) bonus += 3.0
                if (semanticIndex.scoreApp(app, "banco", "banco") > 0) bonus += 2.5
                if (semanticIndex.scoreApp(app, "navegador", "navegador") > 0) bonus += 2.5
                if (semanticIndex.scoreApp(app, "nota", "nota") > 0) bonus += 2.0
            }
            TimeSlot.EVENING -> {
                // Noite: Mídia, Vídeo, Streaming, Comida, Redes Sociais
                if (semanticIndex.scoreApp(app, "video", "video") > 0) bonus += 3.5
                if (semanticIndex.scoreApp(app, "streaming", "streaming") > 0) bonus += 3.5
                if (semanticIndex.scoreApp(app, "comida", "comida") > 0) bonus += 3.0
                if (semanticIndex.scoreApp(app, "social", "social") > 0) bonus += 2.5
            }
            TimeSlot.NIGHT -> {
                // Madrugada: Relógio, Alarme, Notas
                if (semanticIndex.scoreApp(app, "relogio", "relogio") > 0) bonus += 4.0
                if (semanticIndex.scoreApp(app, "alarme", "alarme") > 0) bonus += 4.0
            }
        }

        return bonus
    }
}
