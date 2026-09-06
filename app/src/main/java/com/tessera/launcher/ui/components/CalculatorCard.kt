package com.tessera.launcher.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.LiquidGlassBackground
import com.tessera.launcher.ui.theme.LiquidGlassBorderBrush
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import java.text.DecimalFormat

object MathEvaluator {
    private val format = DecimalFormat("#,##0.######")

    fun evaluate(expr: String): String? {
        val sanitized = expr.trim()
            .replace("x", "*", ignoreCase = true)
            .replace("X", "*")
            .replace("÷", "/")
            .replace(",", ".")

        // Deve conter ao menos 1 operador e 1 dígito
        if (!sanitized.any { it in "+-*/^%" } || !sanitized.any { it.isDigit() }) {
            return null
        }

        return runCatching {
            val result = parseExpression(sanitized)
            if (result.isNaN() || result.isInfinite()) null else format.format(result)
        }.getOrNull()
    }

    private fun parseExpression(expr: String): Double {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < expr.length) expr[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return +parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression(expr)
                eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = expr.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Caractere inesperado: " + ch.toChar())
            }

            if (eat('^'.code)) x = Math.pow(x, parseFactor())
            if (eat('%'.code)) x = x / 100.0
            return x
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> x /= parseFactor()
                    eat('%'.code) -> x = (x / 100.0)
                    else -> return x
                }
            }
        }

        fun parse(): Double {
            nextChar()
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        return parse()
    }
}

@Composable
fun CalculatorCard(
    query: String,
    result: String,
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true
) {
    val context = LocalContext.current
    val border = if (isLiquidGlass) {
        BorderStroke(1.dp, LiquidGlassBorderBrush)
    } else {
        BorderStroke(1.dp, DarkSurfaceBorder)
    }
    val background = if (isLiquidGlass) LiquidGlassBackground else DarkSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(border, CardShape)
            .clip(CardShape)
            .background(background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1D2029)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Calculate,
                        contentDescription = "Calculadora",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = query,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                    Text(
                        text = "= $result",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = TextPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(Color(0xFF222632))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Resultado", result))
                            Toast.makeText(context, "Resultado copiado: $result", Toast.LENGTH_SHORT).show()
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copiar",
                        tint = TextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copiar",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
