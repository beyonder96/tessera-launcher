package com.tessera.launcher.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PhotoWidget(
    photoUriString: String?,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showOptionsDialog by remember { mutableStateOf(false) }

    val bitmap by produceState<Bitmap?>(initialValue = null, photoUriString) {
        value = if (!photoUriString.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                runCatching {
                    val uri = Uri.parse(photoUriString)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
            }
        } else {
            null
        }
    }

    if (showOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            containerColor = DarkSurface,
            shape = CardShape,
            title = {
                Text(
                    text = "Moldura de Foto",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showOptionsDialog = false
                                onPickPhoto()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Trocar foto", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showOptionsDialog = false
                                onRemovePhoto()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Remover foto", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Surface(
                    shape = PillShape,
                    color = Color.Transparent,
                    modifier = Modifier.clickable { showOptionsDialog = false }
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

    Surface(
        modifier = modifier
            .sizeIn(maxWidth = 260.dp, maxHeight = 280.dp)
            .fillMaxWidth(0.72f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (bitmap != null) {
                        showOptionsDialog = true
                    } else {
                        onPickPhoto()
                    }
                }
            ),
        shape = RoundedCornerShape(24.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        if (bitmap != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Foto da tela inicial",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Vinheta sutil e sofisticada na base
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0x66000000)),
                                startY = 100f
                            )
                        )
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = "Adicionar foto",
                    tint = TextTertiary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Toque para adicionar uma foto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
