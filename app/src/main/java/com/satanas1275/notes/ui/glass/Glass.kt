package com.satanas1275.notes.ui.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule

@Composable
fun GlassSurface(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: Shape = Capsule(),
    containerColor: Color? = null,
    tint: Color? = null,
    onClick: (() -> Unit)? = null,
    overlay: (DrawScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val resolvedContainer = containerColor
        ?: if (dark) Color(0xFF10131A).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.42f)

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val progress by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 800f),
        label = "glassPress"
    )

    Box(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(10f.dp.toPx())
                    lens((14f + 8f * progress).dp.toPx(), (18f + 10f * progress).dp.toPx())
                },
                highlight = { Highlight.Default.copy(alpha = 0.55f + 0.45f * progress) },
                shadow = { Shadow(alpha = 0.10f + 0.16f * progress) },
                layerBlock = {
                    val scale = 1f - 0.04f * progress
                    scaleX = scale
                    scaleY = scale
                },
                onDrawSurface = {
                    drawRect(resolvedContainer)
                    tint?.let { t ->
                        drawRect(t, blendMode = BlendMode.Hue)
                        drawRect(t.copy(alpha = t.alpha * 0.72f))
                    }
                    overlay?.invoke(this)
                }
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun GlassIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    backdrop: Backdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    size: Dp = 52f.dp,
    shape: Shape = Capsule()
) {
    val dark = isSystemInDarkTheme()
    GlassSurface(
        backdrop = backdrop,
        modifier = modifier.size(size),
        shape = shape,
        onClick = onClick
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = iconTint
                ?: if (dark) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.75f),
            modifier = Modifier.size(22f.dp)
        )
    }
}
