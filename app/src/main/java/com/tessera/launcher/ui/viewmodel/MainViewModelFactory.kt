package com.tessera.launcher.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tessera.launcher.data.helper.SystemInfoHelper
import com.tessera.launcher.data.repository.AppRepository

class MainViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val appRepo = AppRepository(context.applicationContext)
            val systemHelper = SystemInfoHelper(context.applicationContext)
            return MainViewModel(appRepo, systemHelper) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida: ${modelClass.name}")
    }
}
