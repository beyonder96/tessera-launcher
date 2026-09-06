package com.tessera.launcher.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tessera.launcher.data.helper.CalendarHelper
import com.tessera.launcher.data.helper.ContactSearchHelper
import com.tessera.launcher.data.helper.QuickSettingsHelper
import com.tessera.launcher.data.helper.SystemInfoHelper
import com.tessera.launcher.data.preference.LauncherPreferences
import com.tessera.launcher.data.repository.AppRepository

class MainViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val appRepo = AppRepository(context.applicationContext)
            val systemHelper = SystemInfoHelper(context.applicationContext)
            val calendarHelper = CalendarHelper(context.applicationContext)
            val prefs = LauncherPreferences(context.applicationContext)
            val quickSettingsHelper = QuickSettingsHelper(context.applicationContext)
            val contactSearchHelper = ContactSearchHelper(context.applicationContext)
            return MainViewModel(
                appRepo,
                systemHelper,
                calendarHelper,
                prefs,
                quickSettingsHelper,
                contactSearchHelper
            ) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida: ${modelClass.name}")
    }
}
