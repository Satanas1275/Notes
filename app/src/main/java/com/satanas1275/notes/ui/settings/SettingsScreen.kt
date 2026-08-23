package com.satanas1275.notes.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Gradient
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.net.Uri
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.RoundedRectangle
import com.satanas1275.notes.R
import com.satanas1275.notes.data.AppSettings
import com.satanas1275.notes.data.BackgroundStyle
import com.satanas1275.notes.ui.components.AppBackground
import com.satanas1275.notes.ui.glass.GlassIconButton
import com.satanas1275.notes.ui.glass.GlassSurface
import com.satanas1275.notes.ui.glass.LiquidBottomTab
import com.satanas1275.notes.ui.glass.LiquidBottomTabs
import com.satanas1275.notes.ui.glass.LiquidToggle
import com.satanas1275.notes.ui.theme.NotePalette

private data class LanguageOption(val tag: String?, val label: String)

private val LanguageOptions = listOf(
    LanguageOption(null, ""), // libellé résolu via stringResource (langue du système)
    LanguageOption("fr", "Français"),
    LanguageOption("en", "English"),
    LanguageOption("es", "Español"),
    LanguageOption("de", "Deutsch"),
    LanguageOption("it", "Italiano"),
    LanguageOption("pt", "Português"),
    LanguageOption("ja", "日本語"),
    LanguageOption("ko", "한국어"),
    LanguageOption("zh", "中文")
)

@Composable
fun SettingsChrome(
    backdrop: Backdrop,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val textColor = if (dark) Color.White else Color(0xFF14161B)
    Row(
        modifier
            .fillMaxWidth()
            .safeContentPadding()
            .padding(horizontal = 20f.dp, vertical = 12f.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.cd_back),
            backdrop = backdrop,
            onClick = onBack
        )
        Spacer(Modifier.size(14f.dp))
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun SettingsContent(
    backdrop: Backdrop,
    settings: AppSettings,
    onSetBackgroundStyle: (BackgroundStyle) -> Unit,
    onSetBackgroundColorIndex: (Int) -> Unit,
    onPickBackgroundImage: () -> Unit,
    onClearBackgroundImage: () -> Unit,
    onSetCloudSyncEnabled: (Boolean) -> Unit,
    onSetLanguage: (String?) -> Unit,
    onRequestResetAllNotes: () -> Unit
) {
    val safePadding = WindowInsets.safeDrawing.asPaddingValues()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20f.dp)
            .padding(
                top = safePadding.calculateTopPadding() + 88f.dp,
                bottom = safePadding.calculateBottomPadding() + 40f.dp
            ),
        verticalArrangement = Arrangement.spacedBy(28f.dp)
    ) {
        SettingsSection(title = stringResource(R.string.section_appearance)) {
            AppearanceSettings(
                backdrop = backdrop,
                style = settings.backgroundStyle,
                colorIndex = settings.backgroundColorIndex,
                hasCustomImage = settings.hasCustomBackgroundImage,
                onSetStyle = onSetBackgroundStyle,
                onSetColorIndex = onSetBackgroundColorIndex,
                onPickImage = onPickBackgroundImage,
                onClearImage = onClearBackgroundImage
            )
        }

        SettingsSection(title = stringResource(R.string.section_sync)) {
            CloudSyncRow(
                backdrop = backdrop,
                enabled = settings.cloudSyncEnabled,
                onToggle = onSetCloudSyncEnabled
            )
        }

        SettingsSection(title = stringResource(R.string.section_language)) {
            LanguageRow(
                backdrop = backdrop,
                currentTag = settings.languageTag,
                onSelect = onSetLanguage
            )
        }

        SettingsSection(title = stringResource(R.string.section_data)) {
            DangerRow(
                backdrop = backdrop,
                label = stringResource(R.string.action_reset_all_notes),
                onClick = onRequestResetAllNotes
            )
        }

        SettingsSection(title = stringResource(R.string.section_about)) {
            AboutRow()
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val dark = isSystemInDarkTheme()
    Column(verticalArrangement = Arrangement.spacedBy(10f.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = (if (dark) Color.White else Color(0xFF14161B)).copy(alpha = 0.55f),
            modifier = Modifier.padding(start = 4f.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(10f.dp), content = content)
    }
}

@Composable
private fun AppearanceSettings(
    backdrop: Backdrop,
    style: BackgroundStyle,
    colorIndex: Int,
    hasCustomImage: Boolean,
    onSetStyle: (BackgroundStyle) -> Unit,
    onSetColorIndex: (Int) -> Unit,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val textColor = if (dark) Color.White else Color(0xFF14161B)
    val labelGradient = stringResource(R.string.style_gradient)
    val labelSolidColor = stringResource(R.string.style_solid_color)
    val labelImage = stringResource(R.string.style_image)
    val styles = remember(labelGradient, labelSolidColor, labelImage) {
        listOf(
            Triple(BackgroundStyle.GRADIENT, labelGradient, Icons.Rounded.Gradient),
            Triple(BackgroundStyle.SOLID_COLOR, labelSolidColor, Icons.Rounded.Palette),
            Triple(BackgroundStyle.IMAGE, labelImage, Icons.Rounded.Wallpaper)
        )
    }

    LiquidBottomTabs(
        selectedTabIndex = { style.ordinal },
        onTabSelected = { index -> onSetStyle(BackgroundStyle.entries[index]) },
        backdrop = backdrop,
        tabsCount = styles.size,
        modifier = Modifier.fillMaxWidth(),
        accentColor = MaterialTheme.colorScheme.primary
    ) {
        styles.forEach { (value, label, icon) ->
            LiquidBottomTab(onClick = { onSetStyle(value) }) {
                Icon(icon, contentDescription = label, tint = textColor, modifier = Modifier.size(22f.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = textColor)
            }
        }
    }

    AnimatedVisibility(
        visible = style == BackgroundStyle.SOLID_COLOR,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 4f.dp)) {
            LiquidBottomTabs(
                selectedTabIndex = { colorIndex },
                onTabSelected = onSetColorIndex,
                backdrop = backdrop,
                tabsCount = NotePalette.size,
                modifier = Modifier.fillMaxWidth(),
                // accentColor suit la couleur sélectionnée plutôt qu'un bleu
                // fixe : le "reveal" du composant retente le point avec SA
                // PROPRE couleur au lieu d'écraser tout le monde en bleu.
                accentColor = NotePalette[colorIndex.coerceIn(0, NotePalette.lastIndex)]
            ) {
                NotePalette.forEachIndexed { index, color ->
                    val selected = index == colorIndex
                    LiquidBottomTab(onClick = { onSetColorIndex(index) }) {
                        Box(
                            Modifier
                                .size(if (selected) 24f.dp else 20f.dp)
                                .background(color, CircleShape)
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = style == BackgroundStyle.IMAGE,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(Modifier.fillMaxWidth().padding(top = 4f.dp), verticalArrangement = Arrangement.spacedBy(10f.dp)) {
            if (hasCustomImage) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10f.dp)
                ) {
                    Box(
                        Modifier
                            .size(64f.dp)
                            .clip(RoundedCornerShape(16f.dp))
                    ) {
                        AppBackground(
                            style = BackgroundStyle.IMAGE,
                            colorIndex = colorIndex,
                            hasCustomImage = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(Modifier.align(Alignment.CenterVertically)) {
                        Text(
                            stringResource(R.string.image_active),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                        Text(
                            stringResource(R.string.action_remove),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onClearImage
                            )
                        )
                    }
                }
            }
            GlassSurface(
                backdrop = backdrop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52f.dp),
                onClick = onPickImage
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Wallpaper, contentDescription = null, tint = textColor, modifier = Modifier.size(20f.dp))
                    Spacer(Modifier.size(10f.dp))
                    Text(
                        if (hasCustomImage) stringResource(R.string.action_change_image) else stringResource(R.string.action_choose_image),
                        color = textColor,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun CloudSyncRow(backdrop: Backdrop, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    val dark = isSystemInDarkTheme()
    val textColor = if (dark) Color.White else Color(0xFF14161B)

    GlassSurface(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth().height(72f.dp),
        isInteractive = false
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 18f.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Cloud, contentDescription = null, tint = textColor.copy(alpha = 0.7f), modifier = Modifier.size(22f.dp))
            Spacer(Modifier.size(14f.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.cloud_sync_title), color = textColor, style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.cloud_sync_subtitle),
                    color = textColor.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            // Toggle en verre (glisser ou taper), désactivé visuellement : la
            // fonctionnalité n'est pas encore implémentée côté serveur (WIP),
            // on ne veut pas laisser croire que l'activer fait quoi que ce
            // soit pour l'instant.
            LiquidToggle(
                selected = { enabled },
                onSelect = onToggle,
                backdrop = backdrop,
                enabled = false
            )
        }
    }
}

@Composable
private fun LanguageRow(backdrop: Backdrop, currentTag: String?, onSelect: (String?) -> Unit) {
    val dark = isSystemInDarkTheme()
    val textColor = if (dark) Color.White else Color(0xFF14161B)
    var expanded by remember { mutableStateOf(false) }
    val systemDefaultLabel = stringResource(R.string.language_system_default)
    val current = LanguageOptions.firstOrNull { it.tag == currentTag } ?: LanguageOptions.first()
    val currentLabel = if (current.tag == null) systemDefaultLabel else current.label
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "chevron")

    Column {
        GlassSurface(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth().height(52f.dp),
            onClick = { expanded = !expanded }
        ) {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 18f.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Language, contentDescription = null, tint = textColor.copy(alpha = 0.7f), modifier = Modifier.size(20f.dp))
                Spacer(Modifier.size(10f.dp))
                Text(currentLabel, color = textColor, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(20f.dp).rotate(rotation)
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8f.dp)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedRectangle(20f.dp) },
                        effects = {
                            vibrancy()
                            blur(8f.dp.toPx())
                            lens(8f.dp.toPx(), 16f.dp.toPx())
                        },
                        highlight = { Highlight.Plain },
                        onDrawSurface = {
                            drawRect(if (dark) Color(0xFF10131A).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.55f))
                        }
                    )
            ) {
                LanguageOptions.forEach { option ->
                    val selected = option.tag == currentTag
                    val label = if (option.tag == null) systemDefaultLabel else option.label
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onSelect(option.tag)
                                expanded = false
                            }
                            .padding(horizontal = 18f.dp, vertical = 14f.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            label,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18f.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DangerRow(backdrop: Backdrop, label: String, onClick: () -> Unit) {
    GlassSurface(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth().height(52f.dp),
        tint = MaterialTheme.colorScheme.error,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(20f.dp))
            Spacer(Modifier.size(10f.dp))
            Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AboutRow() {
    val dark = isSystemInDarkTheme()
    val textColor = if (dark) Color.White else Color(0xFF14161B)
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            @Suppress("DEPRECATION")
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName
        }.getOrNull() ?: "—"
    }
    Text(
        "Rocknite Notes · v$versionName",
        color = textColor.copy(alpha = 0.5f),
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 4f.dp)
    )
}

/**
 * Lance le sélecteur d'image système et transmet l'URI choisie.
 * Composant séparé pour garder un point d'entrée unique (rememberLauncherForActivityResult
 * doit être appelé depuis un composable, on ne peut pas le déclencher à la demande).
 */
@Composable
fun rememberImagePickerLauncher(onImagePicked: (Uri) -> Unit) =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onImagePicked(uri)
    }
