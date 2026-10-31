package com.tessera.launcher

import android.Manifest
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
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

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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

    // Seletor de Papel de Parede Personalizado (Photo Picker Nativo)
    private val wallpaperPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setCustomWallpaper(uri, this)
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

    // Launcher de Permissão de Localização (Android 12+ exige FINE e COARSE em conjunto)
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.updateLocationPermission(isGranted)
    }

    // Gerenciador de Widgets Nativos do Android (AppWidgetHost)
    private val APPWIDGET_HOST_ID = 2048
    private var pendingAppWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var appWidgetHost: AppWidgetHost
    private lateinit var appWidgetManager: AppWidgetManager

    private val widgetConfigLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && pendingAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            viewModel.addHostedWidgetId(pendingAppWidgetId)
        } else if (pendingAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            appWidgetHost.deleteAppWidgetId(pendingAppWidgetId)
        }
        pendingAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private val widgetPickLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && pendingAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val appWidgetInfo = appWidgetManager.getAppWidgetInfo(pendingAppWidgetId)
            if (appWidgetInfo?.configure != null) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = appWidgetInfo.configure
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingAppWidgetId)
                }
                widgetConfigLauncher.launch(intent)
            } else {
                viewModel.addHostedWidgetId(pendingAppWidgetId)
                pendingAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            }
        } else if (pendingAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            appWidgetHost.deleteAppWidgetId(pendingAppWidgetId)
            pendingAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        }
    }

    fun pickAndAddAppWidget() {
        val newId = appWidgetHost.allocateAppWidgetId()
        pendingAppWidgetId = newId
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newId)
        }
        try {
            widgetPickLauncher.launch(pickIntent)
        } catch (_: Exception) {
            appWidgetHost.deleteAppWidgetId(newId)
            pendingAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)

        // Maximizar taxa de atualização da tela para 120Hz / alta fluidez
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val currentDisplay = display
            val maxMode = currentDisplay?.supportedModes?.maxByOrNull { it.refreshRate }
            if (maxMode != null) {
                val params = window.attributes
                params.preferredDisplayModeId = maxMode.modeId
                window.attributes = params
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            val currentDisplay = window.windowManager.defaultDisplay
            val maxMode = currentDisplay?.supportedModes?.maxByOrNull { it.refreshRate }
            if (maxMode != null) {
                val params = window.attributes
                params.preferredDisplayModeId = maxMode.modeId
                window.attributes = params
            }
        }

        val factory = MainViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.isShowStatusBarEnabled) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                if (uiState.isShowStatusBarEnabled) {
                    insetsController.show(WindowInsetsCompat.Type.statusBars())
                } else {
                    insetsController.hide(WindowInsetsCompat.Type.statusBars())
                    insetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }

            LaunchedEffect(
                uiState.isDrawerOpen,
                uiState.isSearchExpanded,
                uiState.searchQuery,
                uiState.isDrawerGlassEnabled,
                uiState.drawerGlassOpacity,
                uiState.isSettingsOpen,
                uiState.isFeedOpen,
                uiState.homeWallpaperBlur
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val isAppListOrSearchOpen = uiState.isDrawerOpen || uiState.isSearchExpanded || uiState.searchQuery.isNotEmpty()
                    val isOtherScreenOpen = uiState.isSettingsOpen || uiState.isFeedOpen

                    val targetBlur = when {
                        isAppListOrSearchOpen -> {
                            // Borrão profundo ao abrir a lista de apps / busca para máximo contraste
                            val baseBlur = if (uiState.homeWallpaperBlur > 0) {
                                ((uiState.homeWallpaperBlur / 100f) * 80f + 30f).toInt()
                            } else {
                                60
                            }
                            val extraBlur = if (uiState.isDrawerGlassEnabled && uiState.drawerGlassOpacity > 0) {
                                ((uiState.drawerGlassOpacity / 100f) * 70f).toInt()
                            } else {
                                35
                            }
                            (baseBlur + extraBlur).coerceIn(40, 160)
                        }
                        isOtherScreenOpen -> {
                            85
                        }
                        else -> {
                            // Tela inicial (Home em repouso): aplica o borrão no papel de parede configurado pelo usuário
                            if (uiState.homeWallpaperBlur <= 0) {
                                0
                            } else {
                                ((uiState.homeWallpaperBlur / 100f) * 110f + 10f).toInt().coerceIn(10, 120)
                            }
                        }
                    }

                    // Blurring nativo do fundo da janela e do papel de parede (Android 12+)
                    try {
                        window.setBackgroundBlurRadius(targetBlur)
                    } catch (_: Throwable) {}

                    // Blurring entre superfícies
                    try {
                        if (targetBlur > 0) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                        }
                        val params = window.attributes
                        params.blurBehindRadius = targetBlur
                        window.attributes = params
                    } catch (_: Throwable) {}
                }
            }

            TesseraTheme(
                themeMode = uiState.themeMode,
                accentColor = uiState.activeAccentColor
            ) {
                HomeScreen(
                    viewModel = viewModel,
                    onPickPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onPickWallpaper = {
                        wallpaperPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRequestCalendarPermission = {
                        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                    },
                    onRequestContactsPermission = {
                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    },
                    onRequestLocationPermission = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    appWidgetHost = appWidgetHost,
                    appWidgetManager = appWidgetManager,
                    onAddAppWidget = { pickAndAddAppWidget() },
                    onRemoveAppWidget = { widgetId ->
                        appWidgetHost.deleteAppWidgetId(widgetId)
                        viewModel.removeHostedWidgetId(widgetId)
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (::appWidgetHost.isInitialized) {
            appWidgetHost.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::appWidgetHost.isInitialized) {
            appWidgetHost.stopListening()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.refreshCalendarAndPermissions()
            viewModel.refreshWeather()
            viewModel.refreshPredictedApps()
            viewModel.refreshScreenTime()
            if (viewModel.uiState.value.customWallpaperPath == null && viewModel.uiState.value.isSystemWallpaperEnabled) {
                viewModel.loadWallpaper()
            }
        }
    }
}
