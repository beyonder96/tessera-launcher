package com.tessera.launcher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary

@Composable
fun SearchoDock(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    isWidgetExpanded: Boolean,
    onToggleWidgets: () -> Unit,
    onSearchFocused: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Surface(
        shape = PillShape,
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Buscar aplicativos",
                tint = TextSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            focusRequester.requestFocus()
                            onSearchFocused()
                        }
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            BasicTextField(
                value = searchQuery,
                onValueChange = {
                    onQueryChange(it)
                    if (it.isNotEmpty()) {
                        onSearchFocused()
                    }
                },
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                cursorBrush = SolidColor(TextPrimary),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                    }
                ),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Searcho...",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )

            if (searchQuery.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Limpar busca",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onQueryChange("")
                            }
                        )
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Widgets,
                    contentDescription = "Alternar widgets",
                    tint = if (isWidgetExpanded) TextPrimary else TextTertiary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleWidgets
                        )
                )
            }
        }
    }
}
