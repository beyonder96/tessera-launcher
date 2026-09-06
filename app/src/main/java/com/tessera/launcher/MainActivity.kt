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

import android.os.Build
import android.view.WindowManager

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

    // Launcher de Permissão de Contatos
    private val contactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setContactsPermissionGranted(isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply {
                blurBehindRadius = 45
            }
        }

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
                    },
                    onRequestContactsPermission = {
                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                )
            }
        }
    }
}
