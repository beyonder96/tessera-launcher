package com.tessera.launcher.ui.components

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary

@Composable
fun NativeWidgetHostContainer(
    appWidgetHost: AppWidgetHost?,
    appWidgetManager: AppWidgetManager?,
    hostedWidgetIds: List<Int>,
    onRemoveWidget: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var widgetToRemove by remember { mutableStateOf<Int?>(null) }

    if (widgetToRemove != null) {
        AlertDialog(
            onDismissRequest = { widgetToRemove = null },
            containerColor = DarkSurface,
            shape = CardShape,
            title = {
                Text(
                    text = "Remover Widget",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Deseja remover este widget da sua tela inicial?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Surface(
                    shape = PillShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.clickable {
                        widgetToRemove?.let { id ->
                            onRemoveWidget(id)
                            appWidgetHost?.deleteAppWidgetId(id)
                        }
                        widgetToRemove = null
                    }
                ) {
                    Text(
                        text = "Remover",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            },
            dismissButton = {
                Surface(
                    shape = PillShape,
                    color = Color.Transparent,
                    modifier = Modifier.clickable { widgetToRemove = null }
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        )
    }

    if (hostedWidgetIds.isEmpty()) {
        Box(modifier = modifier)
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            hostedWidgetIds.forEach { widgetId ->
                val widgetInfo = appWidgetManager?.getAppWidgetInfo(widgetId)
                if (widgetInfo != null && appWidgetHost != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                appWidgetHost.createView(ctx, widgetId, widgetInfo).apply {
                                    setAppWidget(widgetId, widgetInfo)
                                    setOnLongClickListener {
                                        widgetToRemove = widgetId
                                        true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
