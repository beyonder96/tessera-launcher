package com.tessera.launcher.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tessera.launcher.data.bridge.TesseraBridgeContract
import com.tessera.launcher.data.bridge.TesseraCoreBridgeClient

/**
 * Receptor de broadcasts assinados emitidos pelo ecossistema Tessera (Core ↔ Launcher).
 * Sincroniza em tempo real status de Foco Pomodoro, Modo Zen, Tema e Mutações de Dados.
 */
class TesseraEcosystemReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "TesseraEcosystemReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(TAG, "Broadcast recebido: $action")

        when (action) {
            TesseraBridgeContract.ACTION_CORE_FOCUS_CHANGED -> {
                val isFocusActive = intent.getBooleanExtra(TesseraBridgeContract.EXTRA_FOCUS_START, false)
                val focusName = intent.getStringExtra(TesseraBridgeContract.EXTRA_FOCUS_NAME)
                Log.d(TAG, "Core Focus alterado: ativo=$isFocusActive ($focusName)")
                TesseraCoreBridgeClient.refreshSnapshot()
            }

            TesseraBridgeContract.ACTION_ECOSYSTEM_THEME_SYNC -> {
                val themeMode = intent.getStringExtra(TesseraBridgeContract.EXTRA_THEME_MODE)
                Log.d(TAG, "Sincronização de tema recebida: $themeMode")
                TesseraCoreBridgeClient.refreshSnapshot()
            }

            TesseraBridgeContract.ACTION_ECOSYSTEM_DATA_CHANGED -> {
                TesseraCoreBridgeClient.refreshSnapshot()
            }
        }
    }
}
