package com.satanas1275.notes.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.max

private val LightBlobColors = listOf(
    Color(0xFFB7C9FF).copy(alpha = 0.85f),
    Color(0xFFFFC9E9).copy(alpha = 0.80f),
    Color(0xFFCDB4FF).copy(alpha = 0.75f),
    Color(0xFFFFE3C4).copy(alpha = 0.80f)
)

private val DarkBlobColors = listOf(
    Color(0xFF1B3B6F).copy(alpha = 0.60f),
    Color(0xFF5B2A86).copy(alpha = 0.55f),
    Color(0xFF83224E).copy(alpha = 0.50f),
    Color(0xFF7A4A21).copy(alpha = 0.45f)
)

@Composable
fun MeshBackground(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val palette = if (dark) DarkBlobColors else LightBlobColors

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val radius = max(w, h) * 0.6f

        fun blob(fx: Float, fy: Float, color: Color) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    center = Offset(w * fx, h * fy),
                    radius = radius
                ),
                radius = radius,
                center = Offset(w * fx, h * fy)
            )
        }

        blob(0.15f, 0.12f, palette[0])
        blob(0.85f, 0.20f, palette[1])
        blob(0.25f, 0.80f, palette[2])
        blob(0.85f, 0.85f, palette[3])
    }
}
