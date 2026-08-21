package com.satanas1275.notes.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.satanas1275.notes.NotesViewModel
import com.satanas1275.notes.data.Note
import com.satanas1275.notes.ui.glass.GlassIconButton
import com.satanas1275.notes.ui.glass.GlassSurface
import com.satanas1275.notes.ui.icons.PinIcon
import com.satanas1275.notes.ui.theme.NotePalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

@Composable
fun NoteEditorContent(
    noteId: String,
    note: Note?,
    viewModel: NotesViewModel,
    closeRequestToken: Int,
    onClosed: () -> Unit
) {
    var title by rememberSaveable(noteId) { mutableStateOf(note?.title.orEmpty()) }
    var content by rememberSaveable(noteId) { mutableStateOf(note?.content.orEmpty()) }
    val latest by rememberUpdatedState(title to content)
    var handledCloseToken by remember(noteId) { mutableIntStateOf(closeRequestToken) }

    val dark = isSystemInDarkTheme()
    val textColor = if (dark) Color.White else Color(0xFF14161B)
    val accent = MaterialTheme.colorScheme.primary
    val focusTitle = remember { FocusRequester() }
    val focusBody = remember { FocusRequester() }

    fun handleClose() {
        val (currentTitle, currentContent) = latest
        if (currentTitle.isBlank() && currentContent.isBlank()) {
            viewModel.delete(noteId)
        } else {
            viewModel.updateContent(noteId, currentTitle, currentContent)
        }
        onClosed()
    }

    BackHandler { handleClose() }

    LaunchedEffect(closeRequestToken) {
        if (closeRequestToken != handledCloseToken) {
            handledCloseToken = closeRequestToken
            handleClose()
        }
    }

    LaunchedEffect(noteId) {
        snapshotFlow { title to content }
            .drop(1)
            .collectLatest {
                delay(400)
                viewModel.updateContent(noteId, title, content)
            }
    }

    LaunchedEffect(noteId) {
        if (note == null || note.isEmpty) {
            focusTitle.requestFocus()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 24f.dp)
    ) {
        Spacer(
            Modifier.height(
                WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding() + 76f.dp
            )
        )
        BasicTextField(
            value = title,
            onValueChange = { title = it },
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                lineHeight = 36.sp
            ),
            cursorBrush = SolidColor(accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusBody.requestFocus() }),
            decorationBox = { innerTextField ->
                Box {
                    if (title.isEmpty()) {
                        Text(
                            text = "Titre",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.3f)
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusTitle)
        )
        Spacer(Modifier.height(14f.dp))
        BasicTextField(
            value = content,
            onValueChange = { content = it },
            textStyle = TextStyle(
                fontSize = 17.sp,
                lineHeight = 25.sp,
                color = textColor
            ),
            cursorBrush = SolidColor(accent),
            decorationBox = { innerTextField ->
                Box {
                    if (content.isEmpty()) {
                        Text(
                            text = "Commencez à écrire…",
                            fontSize = 17.sp,
                            color = textColor.copy(alpha = 0.3f)
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .focusRequester(focusBody)
        )
        Spacer(
            Modifier.height(
                WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() + 96f.dp
            )
        )
    }
}

@Composable
fun EditorChrome(
    backdrop: Backdrop,
    note: Note?,
    viewModel: NotesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier
            .fillMaxWidth()
            .safeContentPadding()
            .padding(horizontal = 20f.dp, vertical = 12f.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Retour",
            backdrop = backdrop,
            onClick = onBack
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10f.dp)) {
            GlassIconButton(
                imageVector = PinIcon,
                contentDescription = if (note?.pinned == true) "Désépingler" else "Épingler",
                backdrop = backdrop,
                iconTint = if (note?.pinned == true) MaterialTheme.colorScheme.primary else null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    note?.let { viewModel.togglePinned(it.id) }
                }
            )
            GlassIconButton(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "Supprimer",
                backdrop = backdrop,
                onClick = { showDeleteConfirm = true }
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer la note ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    note?.let { viewModel.delete(it.id) }
                    onBack()
                }) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun ColorPickerChrome(
    backdrop: Backdrop,
    selectedColorIndex: Int,
    onSelectColor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val dark = isSystemInDarkTheme()

    Box(
        modifier
            .fillMaxWidth()
            .safeContentPadding()
            .padding(horizontal = 24f.dp, vertical = 16f.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassSurface(backdrop = backdrop, modifier = Modifier.height(56f.dp)) {
            Row(
                Modifier.padding(horizontal = 16f.dp),
                horizontalArrangement = Arrangement.spacedBy(12f.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NotePalette.forEachIndexed { index, color ->
                    val selected = index == selectedColorIndex
                    Box(
                        Modifier
                            .size(26f.dp)
                            .background(color, CircleShape)
                            .border(
                                width = if (selected) 2.5f.dp else 0.5f.dp,
                                color = if (selected) {
                                    if (dark) Color.White else Color(0xFF17181C)
                                } else {
                                    Color.Transparent
                                },
                                shape = CircleShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectColor(index)
                            }
                    )
                }
            }
        }
    }
}
