package com.satanas1275.notes.ui.glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
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

/**
 * Surface en verre liquide. Le retour visuel au toucher (halo qui suit le doigt,
 * légère loupe/échelle) reprend le même mécanisme que les boutons du catalogue
 * de la lib Backdrop (`LiquidButton`) : un [InteractiveHighlight] par instance
 * plutôt qu'un simple `pressed` booléen, ce qui donne un rendu identique sur
 * tous les boutons, la barre de recherche, etc.
 */
@Composable
fun GlassSurface(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: Shape = Capsule(),
    containerColor: Color? = null,
    tint: Color? = null,
    isInteractive: Boolean = true,
    onClick: (() -> Unit)? = null,
    overlay: (DrawScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val resolvedContainer = containerColor
        ?: if (dark) Color(0xFF10131A).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.42f)

    val animationScope = rememberCoroutineScope()
    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(animationScope = animationScope)
    }
    val interactive = isInteractive && onClick != null

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    val progress = interactiveHighlight.pressProgress
                    vibrancy()
                    blur(10f.dp.toPx())
                    lens((14f + 8f * progress).dp.toPx(), (18f + 10f * progress).dp.toPx())
                },
                highlight = {
                    val progress = interactiveHighlight.pressProgress
                    Highlight.Default.copy(alpha = 0.55f + 0.45f * progress)
                },
                shadow = {
                    val progress = interactiveHighlight.pressProgress
                    Shadow(alpha = 0.10f + 0.16f * progress)
                },
                layerBlock = {
                    val progress = interactiveHighlight.pressProgress
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
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .then(if (interactive) interactiveHighlight.modifier else Modifier)
            .then(if (interactive) interactiveHighlight.gestureModifier else Modifier),
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
