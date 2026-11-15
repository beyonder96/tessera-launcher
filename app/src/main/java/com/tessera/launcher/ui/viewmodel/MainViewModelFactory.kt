package com.tessera.launcher.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tessera.launcher.data.helper.CalendarHelper
import com.tessera.launcher.data.helper.ContactSearchHelper
import com.tessera.launcher.data.helper.QuickSettingsHelper
import com.tessera.launcher.data.helper.SystemInfoHelper
import com.tessera.launcher.data.license.LicenseManager
import com.tessera.launcher.data.preference.LauncherPreferences
import com.tessera.launcher.data.repository.AppRepository
import com.tessera.launcher.data.repository.FeedRepository

class MainViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val appContext = context.applicationContext
            val appRepo = AppRepository(appContext)
            val systemHelper = SystemInfoHelper(appContext)
            val calendarHelper = CalendarHelper(appContext)
            val prefs = LauncherPreferences(appContext)
            val licenseManager = LicenseManager(appContext)
            val quickSettingsHelper = QuickSettingsHelper(appContext)
            val contactSearchHelper = ContactSearchHelper(appContext)
            val weatherHelper = com.tessera.launcher.data.helper.WeatherHelper(appContext, prefs)
            val feedRepository = FeedRepository(appContext, prefs, licenseManager)
            return MainViewModel(
                appRepository = appRepo,
                systemInfoHelper = systemHelper,
                calendarHelper = calendarHelper,
                preferences = prefs,
                quickSettingsHelper = quickSettingsHelper,
                contactSearchHelper = contactSearchHelper,
                weatherHelper = weatherHelper,
                feedRepository = feedRepository,
                licenseManager = licenseManager,
                appContext = appContext
            ) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida: ${modelClass.name}")
    }
}

