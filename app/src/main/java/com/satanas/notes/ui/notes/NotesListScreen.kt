package com.satanas.notes.ui.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.satanas.notes.NotesViewModel
import com.satanas.notes.NoteFilter
import com.satanas.notes.data.Note
import com.satanas.notes.ui.glass.GlassSurface
import com.satanas.notes.ui.icons.PinIcon
import com.satanas.notes.ui.theme.NotePalette
import com.satanas.notes.ui.utils.formatNoteDate

@Composable
fun NotesListContent(
    state: com.satanas.notes.NotesUiState,
    viewModel: NotesViewModel,
    onOpenNote: (String) -> Unit
) {
    val dark = isSystemInDarkTheme()
    val safePadding = WindowInsets.safeDrawing.asPaddingValues()
    var pendingDelete by remember { mutableStateOf<Note?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20f.dp,
            end = 20f.dp,
            top = safePadding.calculateTopPadding() + 76f.dp,
            bottom = safePadding.calculateBottomPadding() + 112f.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12f.dp)
    ) {
        item(key = "header") {
            Column(Modifier.padding(bottom = 8f.dp)) {
                Text(
                    text = "Mes notes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4f.dp))
                Text(
                    text = "${state.totalCount} note${if (state.totalCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        val searching = state.query.isNotBlank()
        if (state.pinned.isNotEmpty() && !searching) {
            item(key = "section-pinned") { SectionHeader("Épinglées") }
            items(state.pinned, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    dark = dark,
                    onClick = { onOpenNote(note.id) },
                    onLongClick = { pendingDelete = note }
                )
            }
        }
        if (state.others.isNotEmpty()) {
            if (!searching && state.pinned.isNotEmpty()) {
                item(key = "section-others") { SectionHeader("Notes") }
            }
            items(state.others, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    dark = dark,
                    onClick = { onOpenNote(note.id) },
                    onLongClick = { pendingDelete = note }
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

    pendingDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Supprimer la note ?") },
            text = {
                Text("« ${note.title.ifBlank { "Sans titre" }} » sera définitivement supprimée.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(note.id)
                    pendingDelete = null
                }) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Annuler")
                }
            }
        )
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
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "cardPress"
    )
    val background = NotePalette[note.colorIndex.coerceIn(0, NotePalette.lastIndex)]
        .copy(alpha = if (dark) 0.34f else 0.46f)
    val contentColor = if (dark) Color.White else Color(0xFF14161B)

    Column(
        Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(background, RoundedCornerShape(28f.dp))
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
                text = note.title.ifBlank { "Sans titre" },
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
                    contentDescription = "Épinglée",
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
            imageVector = if (showSearchHint) Icons.Rounded.Search else com.satanas.notes.ui.icons.NoteGlyph,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
            modifier = Modifier.size(56f.dp)
        )
        Spacer(Modifier.height(16f.dp))
        Text(
            text = if (showSearchHint) "Aucun résultat" else "Aucune note",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        if (!showSearchHint) {
            Spacer(Modifier.height(6f.dp))
            Text(
                text = "Appuyez sur « Nouvelle » pour en créer une.",
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
    state: com.satanas.notes.NotesUiState,
    viewModel: NotesViewModel,
    onCreateNote: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        GlassSearchBar(
            backdrop = backdrop,
            query = state.query,
            onQueryChange = viewModel::setQuery,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .safeContentPadding()
                .padding(horizontal = 20f.dp, vertical = 12f.dp)
        )
        GlassNavBar(
            backdrop = backdrop,
            selectedIndex = state.filter.ordinal,
            onSelectFilter = viewModel::setFilter,
            onCreateNote = onCreateNote,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .safeContentPadding()
                .padding(horizontal = 24f.dp, vertical = 16f.dp)
        )
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
                        text = "Rechercher une note",
                        color = textColor.copy(alpha = 0.45f),
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Effacer",
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

private data class NavItem(
    val icon: ImageVector,
    val label: String,
    val isAction: Boolean = false
)

@Composable
private fun GlassNavBar(
    backdrop: Backdrop,
    selectedIndex: Int,
    onSelectFilter: (NoteFilter) -> Unit,
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val accent = MaterialTheme.colorScheme.primary
    val textColor = if (dark) Color.White else Color(0xFF17181C)
    val items = remember {
        listOf(
            NavItem(Icons.AutoMirrored.Rounded.List, "Notes"),
            NavItem(PinIcon, "Épinglées"),
            NavItem(Icons.Rounded.Add, "Nouvelle", isAction = true)
        )
    }
    val actionIndex = items.lastIndex
    val selectionProgress by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f),
        label = "navSelection"
    )

    GlassSurface(
        backdrop = backdrop,
        modifier = modifier.height(64f.dp),
        overlay = {
            val slot = size.width / items.size
            val pad = 4f.dp.toPx()
            val pillHeight = size.height - pad * 2
            val radius = androidx.compose.ui.geometry.CornerRadius(pillHeight / 2f)

            drawRoundRect(
                color = accent.copy(alpha = 0.85f),
                topLeft = androidx.compose.ui.geometry.Offset(slot * actionIndex + pad, pad),
                size = androidx.compose.ui.geometry.Size(slot - pad * 2, pillHeight),
                cornerRadius = radius
            )
            if (selectedIndex != actionIndex) {
                drawRoundRect(
                    color = Color.White.copy(alpha = if (dark) 0.14f else 0.65f),
                    topLeft = androidx.compose.ui.geometry.Offset(selectionProgress * slot + pad, pad),
                    size = androidx.compose.ui.geometry.Size(slot - pad * 2, pillHeight),
                    cornerRadius = radius
                )
            }
        }
    ) {
        Row(Modifier.fillMaxSize()) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex && !item.isAction
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (item.isAction) onCreateNote() else onSelectFilter(NoteFilter.entries[index])
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = when {
                            item.isAction -> Color.White
                            selected -> accent
                            else -> textColor.copy(alpha = 0.7f)
                        },
                        modifier = Modifier.size(22f.dp)
                    )
                    Spacer(Modifier.height(2f.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            item.isAction -> Color.White
                            selected -> textColor
                            else -> textColor.copy(alpha = 0.6f)
                        }
                    )
                }
            }
        }
    }
}
