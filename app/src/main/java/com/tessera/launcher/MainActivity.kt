package com.tessera.launcher

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.tessera.launcher.ui.screens.HomeScreen
import com.tessera.launcher.ui.theme.TesseraTheme
import com.tessera.launcher.ui.viewmodel.MainViewModel
import com.tessera.launcher.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    // Seletor Moderno de Imagens (Photo Picker Nativo do Android)
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.setPhotoWidgetUri(uri.toString())
        }
    }

    // Launcher de Permissão de Calendário
    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.refreshCalendarAndPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = MainViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            TesseraTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onPickPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRequestCalendarPermission = {
                        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                    }
                )
            }
        }
    }
}
