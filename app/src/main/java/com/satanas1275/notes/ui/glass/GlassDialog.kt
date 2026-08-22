package com.satanas1275.notes.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.satanas1275.notes.R
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

/**
 * Boîte de dialogue "verre liquide", construite comme `DialogContent` dans le
 * catalogue de la lib Backdrop : une carte dessinée directement dans l'arbre de
 * composition (pas de `Dialog()` système, qui ouvrirait une fenêtre séparée et
 * empêcherait le verre de réfracter ce qu'il y a derrière), avec un fond dimmé,
 * un flou + une loupe (lens) et deux boutons capsule.
 */
@Composable
fun GlassConfirmDialog(
    backdrop: Backdrop,
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = true
) {
    val dark = isSystemInDarkTheme()
    val contentColor = if (dark) Color.White else Color.Black
    val containerColor =
        if (dark) Color(0xFF121212).copy(alpha = 0.4f) else Color(0xFFFAFAFA).copy(alpha = 0.6f)
    val dimColor =
        if (dark) Color(0xFF121212).copy(alpha = 0.56f) else Color(0xFF29293A).copy(alpha = 0.23f)
    val confirmColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Box(
        Modifier
            .fillMaxSize()
            .background(dimColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .padding(40f.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedRectangle(32f.dp) },
                    effects = {
                        colorControls(
                            brightness = if (dark) 0f else 0.2f,
                            saturation = 1.5f
                        )
                        blur(if (dark) 6f.dp.toPx() else 12f.dp.toPx())
                        lens(16f.dp.toPx(), 32f.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Plain },
                    onDrawSurface = { drawRect(containerColor) }
                )
                .fillMaxWidth()
                // Avale le tap pour que toucher la carte ne ferme pas le dialog.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                modifier = Modifier.padding(24f.dp, 24f.dp, 24f.dp, 8f.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.68f),
                modifier = Modifier.padding(24f.dp, 8f.dp, 24f.dp, 12f.dp)
            )
            Row(
                Modifier
                    .padding(20f.dp, 8f.dp, 20f.dp, 20f.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12f.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .clip(Capsule())
                        .background(containerColor.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        )
                        .height(48f.dp)
                        .weight(1f)
                        .padding(horizontal = 16f.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.dialog_cancel), color = contentColor, style = MaterialTheme.typography.labelLarge)
                }
                Row(
                    Modifier
                        .clip(Capsule())
                        .background(confirmColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onConfirm
                        )
                        .height(48f.dp)
                        .weight(1f)
                        .padding(horizontal = 16f.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(confirmLabel, color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
