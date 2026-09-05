package com.tessera.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.tessera.launcher.ui.screens.HomeScreen
import com.tessera.launcher.ui.theme.TesseraTheme
import com.tessera.launcher.ui.viewmodel.MainViewModel
import com.tessera.launcher.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = MainViewModelFactory(this)
        val viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            TesseraTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
