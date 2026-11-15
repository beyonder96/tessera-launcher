package com.tessera.launcher

import android.app.Application
import com.tessera.launcher.data.helper.TesseraCrashHandler

class TesseraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TesseraCrashHandler.install(this)
    }
}
