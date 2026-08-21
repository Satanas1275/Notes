package com.satanas.notes.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

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
    val transition = rememberInfiniteTransition(label = "mesh")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(durationMillis = 36_000, easing = LinearEasing)),
        label = "phase"
    )
    val palette = if (dark) DarkBlobColors else LightBlobColors

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val radius = max(w, h) * 0.6f

        fun blob(fx: Float, fy: Float, ax: Float, ay: Float, ph: Float, color: Color) {
            val cx = w * fx + ax * w * sin(phase + ph)
            val cy = h * fy + ay * h * cos(phase * 0.8f + ph)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                radius = radius,
                center = Offset(cx, cy)
            )
        }

        blob(0.15f, 0.12f, 0.06f, 0.05f, 0f, palette[0])
        blob(0.85f, 0.20f, 0.05f, 0.07f, 1.6f, palette[1])
        blob(0.25f, 0.80f, 0.07f, 0.05f, 3.2f, palette[2])
        blob(0.85f, 0.85f, 0.05f, 0.06f, 4.8f, palette[3])
    }
}
