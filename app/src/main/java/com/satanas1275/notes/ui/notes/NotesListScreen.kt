package com.satanas1275.notes.ui.notes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.RoundedRectangle
import com.satanas1275.notes.NotesViewModel
import com.satanas1275.notes.NoteFilter
import com.satanas1275.notes.R
import com.satanas1275.notes.data.Note
import com.satanas1275.notes.ui.glass.GlassIconButton
import com.satanas1275.notes.ui.glass.GlassSurface
import com.satanas1275.notes.ui.glass.LiquidBottomTab
import com.satanas1275.notes.ui.glass.LiquidBottomTabs
import com.satanas1275.notes.ui.icons.PinIcon
import com.satanas1275.notes.ui.theme.NotePalette
import com.satanas1275.notes.ui.utils.formatNoteDate

@Composable
fun NotesListContent(
    backdrop: Backdrop,
    state: com.satanas1275.notes.NotesUiState,
    onOpenNote: (String) -> Unit,
    onRequestDelete: (Note) -> Unit
) {
    val dark = isSystemInDarkTheme()
    val safePadding = WindowInsets.safeDrawing.asPaddingValues()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20f.dp,
            end = 20f.dp,
            // + 76dp pour laisser la place à la barre de recherche flottante,
            // + un peu d'espace en plus pour ne pas coller "Mes notes" dessous.
            top = safePadding.calculateTopPadding() + 96f.dp,
            bottom = safePadding.calculateBottomPadding() + 112f.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12f.dp)
    ) {
        item(key = "header") {
            Column(Modifier.padding(bottom = 8f.dp)) {
                Text(
                    text = stringResource(R.string.header_my_notes),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4f.dp))
                Text(
                    text = pluralStringResource(R.plurals.notes_count, state.totalCount, state.totalCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        val searching = state.query.isNotBlank()
        if (state.pinned.isNotEmpty() && !searching) {
            item(key = "section-pinned") { SectionHeader(stringResource(R.string.section_pinned)) }
            items(state.pinned, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    dark = dark,
                    backdrop = backdrop,
                    onClick = { onOpenNote(note.id) },
                    onLongClick = { onRequestDelete(note) },
                    modifier = Modifier.animateItem()
                )
            }
        }
        if (state.others.isNotEmpty()) {
            if (!searching && state.pinned.isNotEmpty()) {
                item(key = "section-others") { SectionHeader(stringResource(R.string.section_notes)) }
            }
            items(state.others, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    dark = dark,
                    backdrop = backdrop,
                    onClick = { onOpenNote(note.id) },
                    onLongClick = { onRequestDelete(note) },
                    modifier = Modifier.animateItem()
                )
            }
        }
        if (state.visibleCount == 0) {
            item(key = "empty") {
                EmptyState(
                    showSearchHint = searching || state.filter == NoteFilter.PINNED
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
        modifier = Modifier.padding(top = 4f.dp, start = 4f.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(
    note: Note,
    dark: Boolean,
    backdrop: Backdrop,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "cardPress"
    )
    val cardTint = NotePalette[note.colorIndex.coerceIn(0, NotePalette.lastIndex)]
        .copy(alpha = if (dark) 0.34f else 0.46f)
    val contentColor = if (dark) Color.White else Color(0xFF14161B)

    Column(
        modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            // Même traitement que le "lazy scroll container" du catalogue Backdrop
            // (vibrancy + lens qui réfractent le fond), avec un peu plus de blur
            // et la teinte de couleur de la note en surface.
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(28f.dp) },
                effects = {
                    vibrancy()
                    blur(14f.dp.toPx())
                    lens(16f.dp.toPx(), 32f.dp.toPx())
                },
                onDrawSurface = { drawRect(cardTint) }
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 18f.dp, vertical = 16f.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = note.title.ifBlank { stringResource(R.string.untitled_note) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (note.pinned) {
                Spacer(Modifier.width(8f.dp))
                Icon(
                    imageVector = PinIcon,
                    contentDescription = stringResource(R.string.cd_pinned),
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(15f.dp)
                )
            }
        }
        if (note.content.isNotBlank()) {
            Spacer(Modifier.height(6f.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.75f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(10f.dp))
        Text(
            text = formatNoteDate(note.modifiedAt),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun EmptyState(showSearchHint: Boolean) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 72f.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (showSearchHint) Icons.Rounded.Search else com.satanas1275.notes.ui.icons.NoteGlyph,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
            modifier = Modifier.size(56f.dp)
        )
        Spacer(Modifier.height(16f.dp))
        Text(
            text = if (showSearchHint) stringResource(R.string.empty_no_results) else stringResource(R.string.empty_no_notes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        if (!showSearchHint) {
            Spacer(Modifier.height(6f.dp))
            Text(
                text = stringResource(R.string.empty_no_notes_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ListChrome(
    backdrop: Backdrop,
    state: com.satanas1275.notes.NotesUiState,
    viewModel: NotesViewModel,
    onCreateNote: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val dark = isSystemInDarkTheme()
    val textColor = if (dark) Color.White else Color(0xFF17181C)
    val tabs = remember {
        listOf(
            R.string.section_notes to Icons.AutoMirrored.Rounded.List,
            R.string.section_pinned to PinIcon
        )
    }
    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .safeContentPadding()
                .padding(horizontal = 20f.dp, vertical = 12f.dp),
            horizontalArrangement = Arrangement.spacedBy(12f.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassSearchBar(
                backdrop = backdrop,
                query = state.query,
                onQueryChange = viewModel::setQuery,
                modifier = Modifier.weight(1f)
            )
            GlassIconButton(
                imageVector = Icons.Rounded.Settings,
                contentDescription = stringResource(R.string.cd_settings),
                backdrop = backdrop,
                onClick = onOpenSettings
            )
        }
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .safeContentPadding()
                .padding(horizontal = 20f.dp, vertical = 16f.dp),
            horizontalArrangement = Arrangement.spacedBy(12f.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidBottomTabs(
                selectedTabIndex = { state.filter.ordinal },
                onTabSelected = { index -> viewModel.setFilter(NoteFilter.entries[index]) },
                backdrop = backdrop,
                tabsCount = tabs.size,
                modifier = Modifier.weight(1f),
                accentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, tab ->
                    val label = stringResource(tab.first)
                    LiquidBottomTab(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.setFilter(NoteFilter.entries[index])
                    }) {
                        Icon(
                            imageVector = tab.second,
                            contentDescription = label,
                            tint = textColor,
                            modifier = Modifier.size(24f.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor
                        )
                    }
                }
            }
            GlassSurface(
                backdrop = backdrop,
                modifier = Modifier.size(64f.dp),
                tint = MaterialTheme.colorScheme.primary,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCreateNote()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.cd_new_note),
                    tint = Color.White,
                    modifier = Modifier.size(26f.dp)
                )
            }
        }
    }
}

@Composable
private fun GlassSearchBar(
    backdrop: Backdrop,
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val textColor = if (dark) Color.White else Color(0xFF17181C)

    GlassSurface(backdrop = backdrop, modifier = modifier.height(52f.dp)) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18f.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.55f),
                modifier = Modifier.size(20f.dp)
            )
            Spacer(Modifier.width(10f.dp))
            Box(Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = textColor, fontSize = 16.sp),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.fillMaxWidth()
                )
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        color = textColor.copy(alpha = 0.45f),
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.cd_clear_search),
                    tint = textColor.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(18f.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onQueryChange("") }
                )
            }
        }
    }
}
