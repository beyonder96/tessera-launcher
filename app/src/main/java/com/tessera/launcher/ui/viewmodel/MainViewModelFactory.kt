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
import com.tessera.launcher.data.repository.FeedRepository

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
            val fileSearchHelper = com.tessera.launcher.data.helper.FileSearchHelper(context.applicationContext)
            val messageSearchHelper = com.tessera.launcher.data.helper.MessageSearchHelper(context.applicationContext)
            val weatherHelper = com.tessera.launcher.data.helper.WeatherHelper(context.applicationContext)
            val feedRepository = FeedRepository(context.applicationContext, prefs)
            return MainViewModel(
                appRepo,
                systemHelper,
                calendarHelper,
                prefs,
                quickSettingsHelper,
                contactSearchHelper,
                fileSearchHelper,
                messageSearchHelper,
                weatherHelper,
                feedRepository
            ) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida: ${modelClass.name}")
    }
}

