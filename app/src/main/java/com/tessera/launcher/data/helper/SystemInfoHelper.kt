package com.tessera.launcher.data.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean
)

class SystemInfoHelper(private val context: Context) {

    fun observeBattery(): Flow<BatteryInfo> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL

                    val percentage = if (level >= 0 && scale > 0) {
                        (level * 100) / scale
                    } else {
                        100
                    }
                    trySend(BatteryInfo(percentage, isCharging))
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = context.registerReceiver(receiver, filter)
        initialIntent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            val percentage = if (level >= 0 && scale > 0) (level * 100) / scale else 100
            trySend(BatteryInfo(percentage, isCharging))
        }

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: IllegalArgumentException) {}
        }
    }

    fun observeTime(): Flow<Pair<String, String>> = flow {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.getDefault())

        while (true) {
            val now = Date()
            val time = timeFormat.format(now)
            val date = dateFormat.format(now).replaceFirstChar { it.uppercase() }
            emit(time to date)
            delay(1000)
        }
    }
}
