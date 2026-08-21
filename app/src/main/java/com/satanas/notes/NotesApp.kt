package com.satanas.notes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.satanas.notes.data.NotesRepository
import com.satanas.notes.ui.components.MeshBackground
import com.satanas.notes.ui.editor.ColorPickerChrome
import com.satanas.notes.ui.editor.EditorChrome
import com.satanas.notes.ui.editor.NoteEditorContent
import com.satanas.notes.ui.notes.ListChrome
import com.satanas.notes.ui.notes.NotesListContent
import com.satanas.notes.ui.theme.DarkBase
import com.satanas.notes.ui.theme.LightBase

@Composable
fun NotesApp(viewModel: NotesViewModel = viewModel { NotesViewModel(NotesRepository) }) {
    val state by viewModel.uiState.collectAsState()
    var editorNoteId by rememberSaveable { mutableStateOf<String?>(null) }
    var closeRequestToken by remember { mutableIntStateOf(0) }
    var lastOpenedId by remember { mutableStateOf<String?>(null) }
    val inEditor = editorNoteId != null

    SideEffect {
        if (editorNoteId != null) {
            lastOpenedId = editorNoteId
        }
    }

    val dark = isSystemInDarkTheme()
    val baseColor = if (dark) DarkBase else LightBase

    val backdrop = rememberLayerBackdrop {
        drawRect(baseColor)
        drawContent()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        // Contenu capturé par le backdrop : fond + notes (réfracté par le verre).
        Box(
            Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {
            MeshBackground(Modifier.fillMaxSize())
            AnimatedContent(
                targetState = inEditor,
                transitionSpec = {
                    (slideInHorizontally { it / 5 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 6 } + fadeOut())
                },
                label = "screenContent"
            ) { editing ->
                if (editing) {
                    val noteId = editorNoteId ?: lastOpenedId
                    if (noteId != null) {
                        NoteEditorContent(
                            noteId = noteId,
                            note = findNote(state, noteId),
                            viewModel = viewModel,
                            closeRequestToken = closeRequestToken,
                            onClosed = { editorNoteId = null }
                        )
                    }
                } else {
                    NotesListContent(
                        state = state,
                        viewModel = viewModel,
                        onOpenNote = { editorNoteId = it }
                    )
                }
            }
        }

        // Chrome en verre : dessiné au-dessus, réfracte le backdrop.
        AnimatedContent(
            targetState = inEditor,
            transitionSpec = {
                (slideInHorizontally { it / 5 } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 6 } + fadeOut())
            },
            label = "screenChrome"
        ) { editing ->
            if (editing) {
                val noteId = editorNoteId ?: lastOpenedId
                val note = noteId?.let { findNote(state, it) }
                Box(Modifier.fillMaxSize()) {
                    EditorChrome(
                        backdrop = backdrop,
                        note = note,
                        viewModel = viewModel,
                        onBack = { closeRequestToken++ },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                    ColorPickerChrome(
                        backdrop = backdrop,
                        selectedColorIndex = note?.colorIndex ?: 0,
                        onSelectColor = { index ->
                            noteId?.let { viewModel.setColor(it, index) }
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            } else {
                ListChrome(
                    backdrop = backdrop,
                    state = state,
                    viewModel = viewModel,
                    onCreateNote = { editorNoteId = viewModel.createNote() }
                )
            }
        }
    }
}

private fun findNote(state: NotesUiState, id: String) =
    (state.pinned + state.others).firstOrNull { it.id == id }
