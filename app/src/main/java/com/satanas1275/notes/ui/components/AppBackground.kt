package com.satanas1275.notes.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.satanas1275.notes.data.BackgroundStyle
import com.satanas1275.notes.data.SettingsRepository.backgroundImageFile
import com.satanas1275.notes.ui.theme.NotePalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Fond de l'application : dégradé (par défaut), couleur unie, ou image
 * choisie par l'utilisateur. Utilisé à la fois comme fond visible et comme
 * ce que les cartes de notes réfractent (voir `meshBackdrop` dans NotesApp).
 */
@Composable
fun AppBackground(
    style: BackgroundStyle,
    colorIndex: Int,
    hasCustomImage: Boolean,
    modifier: Modifier = Modifier
) {
    when (style) {
        BackgroundStyle.GRADIENT -> MeshBackground(modifier)

        BackgroundStyle.SOLID_COLOR -> {
            val color = NotePalette[colorIndex.coerceIn(0, NotePalette.lastIndex)]
            Box(modifier.background(color))
        }

        BackgroundStyle.IMAGE -> {
            if (hasCustomImage) {
                val context = LocalContext.current
                val bitmap by produceState<ImageBitmap?>(initialValue = null, hasCustomImage) {
                    value = withContext(Dispatchers.IO) {
                        runCatching {
                            val file = context.applicationContext.backgroundImageFile
                            if (!file.exists()) return@runCatching null
                            // On sous-échantillonne pour éviter de décoder une
                            // photo en pleine résolution juste pour un fond flouté.
                            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeFile(file.path, bounds)
                            val metrics = context.resources.displayMetrics
                            var sample = 1
                            while (bounds.outWidth / sample > metrics.widthPixels * 2 ||
                                bounds.outHeight / sample > metrics.heightPixels * 2
                            ) {
                                sample *= 2
                            }
                            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                            BitmapFactory.decodeFile(file.path, opts)?.asImageBitmap()
                        }.getOrNull()
                    }
                }
                val currentBitmap = bitmap
                if (currentBitmap != null) {
                    Image(
                        bitmap = currentBitmap,
                        contentDescription = null,
                        modifier = modifier,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    MeshBackground(modifier)
                }
            } else {
                MeshBackground(modifier)
            }
        }
    }
}
