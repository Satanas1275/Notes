package com.satanas1275.notes

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.satanas1275.notes.data.Note
import com.satanas1275.notes.data.NotesRepository
import com.satanas1275.notes.ui.components.AppBackground
import com.satanas1275.notes.ui.editor.ColorPickerChrome
import com.satanas1275.notes.ui.editor.EditorChrome
import com.satanas1275.notes.ui.editor.NoteEditorContent
import com.satanas1275.notes.ui.glass.GlassConfirmDialog
import com.satanas1275.notes.ui.notes.ListChrome
import com.satanas1275.notes.ui.notes.NotesListContent
import com.satanas1275.notes.ui.settings.SettingsChrome
import com.satanas1275.notes.ui.settings.SettingsContent
import com.satanas1275.notes.ui.settings.rememberImagePickerLauncher
import com.satanas1275.notes.ui.theme.DarkBase
import com.satanas1275.notes.ui.theme.LightBase

private enum class Screen { LIST, EDITOR, SETTINGS }

private fun Screen.depth() = if (this == Screen.LIST) 0 else 1

@Composable
fun NotesApp(
    viewModel: NotesViewModel = viewModel { NotesViewModel(NotesRepository) },
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val settings by settingsViewModel.uiState.collectAsState()

    var editorNoteId by rememberSaveable { mutableStateOf<String?>(null) }
    var closeRequestToken by remember { mutableIntStateOf(0) }
    var lastOpenedId by remember { mutableStateOf<String?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    val screen = when {
        editorNoteId != null -> Screen.EDITOR
        showSettings -> Screen.SETTINGS
        else -> Screen.LIST
    }

    // État des dialogs de confirmation, remonté ici pour que la carte en verre
    // puisse réfracter le `backdrop` partagé (un vrai Dialog() système ouvrirait
    // une fenêtre séparée qui ne peut pas voir ce qu'il y a derrière).
    var pendingDeleteNote by remember { mutableStateOf<Note?>(null) }
    var showEditorDeleteConfirm by remember { mutableStateOf(false) }
    var showResetAllConfirm by remember { mutableStateOf(false) }

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
    // Backdrop séparé, ne capturant QUE le fond (dégradé/couleur/image) — pas
    // la liste ni les cartes. Les NoteCard réfractent celui-ci : si elles
    // réfractaient `backdrop` (qui capture aussi la liste elle-même), une carte
    // essaierait de se lire pendant qu'elle est en train d'être dessinée
    // (référence circulaire) → crash au moment d'afficher une note dans la liste.
    val meshBackdrop = rememberLayerBackdrop {
        drawRect(baseColor)
        drawContent()
    }

    val imagePicker = rememberImagePickerLauncher { uri ->
        settingsViewModel.pickBackgroundImage(uri)
    }

    // Transition "push/pop" : on avance (liste → note / liste → réglages), le
    // nouvel écran entre depuis la droite et l'ancien recule légèrement vers
    // la gauche (profondeur). Au retour, c'est l'inverse : l'écran quitté
    // glisse entièrement vers la DROITE (sensation naturelle de "retour en
    // arrière"), et l'écran précédent réapparaît depuis la gauche.
    val transitionSpec: AnimatedContentTransitionScope<Screen>.() -> ContentTransform = {
        val forward = targetState.depth() > initialState.depth()
        if (forward) {
            (slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { it } + fadeIn()) togetherWith
                (slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { -it / 6 } + fadeOut())
        } else {
            (slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { -it / 6 } + fadeIn()) togetherWith
                (slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { it } + fadeOut())
        }
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
            Box(Modifier.fillMaxSize().layerBackdrop(meshBackdrop)) {
                AppBackground(
                    style = settings.backgroundStyle,
                    colorIndex = settings.backgroundColorIndex,
                    hasCustomImage = settings.hasCustomBackgroundImage,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AnimatedContent(
                targetState = screen,
                transitionSpec = transitionSpec,
                label = "screenContent"
            ) { current ->
                when (current) {
                    Screen.EDITOR -> {
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
                    }
                    Screen.SETTINGS -> {
                        SettingsContent(
                            backdrop = meshBackdrop,
                            settings = settings,
                            onSetBackgroundStyle = settingsViewModel::setBackgroundStyle,
                            onSetBackgroundColorIndex = settingsViewModel::setBackgroundColorIndex,
                            onPickBackgroundImage = {
                                imagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onClearBackgroundImage = settingsViewModel::clearBackgroundImage,
                            onSetCloudSyncEnabled = settingsViewModel::setCloudSyncEnabled,
                            onSetLanguage = { tag ->
                                settingsViewModel.setLanguageTag(tag)
                                AppCompatDelegate.setApplicationLocales(
                                    if (tag == null) {
                                        LocaleListCompat.getEmptyLocaleList()
                                    } else {
                                        LocaleListCompat.forLanguageTags(tag)
                                    }
                                )
                            },
                            onRequestResetAllNotes = { showResetAllConfirm = true }
                        )
                    }
                    Screen.LIST -> {
                        NotesListContent(
                            backdrop = meshBackdrop,
                            state = state,
                            onOpenNote = { editorNoteId = it },
                            onRequestDelete = { pendingDeleteNote = it }
                        )
                    }
                }
            }
        }

        // Chrome en verre : dessiné au-dessus, réfracte le backdrop.
        AnimatedContent(
            targetState = screen,
            transitionSpec = transitionSpec,
            label = "screenChrome"
        ) { current ->
            when (current) {
                Screen.EDITOR -> {
                    val noteId = editorNoteId ?: lastOpenedId
                    val note = noteId?.let { findNote(state, it) }
                    Box(Modifier.fillMaxSize()) {
                        EditorChrome(
                            backdrop = backdrop,
                            note = note,
                            viewModel = viewModel,
                            onBack = { closeRequestToken++ },
                            onRequestDelete = { showEditorDeleteConfirm = true },
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
                }
                Screen.SETTINGS -> {
                    SettingsChrome(
                        backdrop = backdrop,
                        onBack = { showSettings = false }
                    )
                }
                Screen.LIST -> {
                    ListChrome(
                        backdrop = backdrop,
                        state = state,
                        viewModel = viewModel,
                        onCreateNote = { editorNoteId = viewModel.createNote() },
                        onOpenSettings = { showSettings = true }
                    )
                }
            }
        }

        // Dialogs en verre : toujours au-dessus de tout le reste.
        pendingDeleteNote?.let { note ->
            GlassConfirmDialog(
                backdrop = backdrop,
                title = "Supprimer la note ?",
                message = "« ${note.title.ifBlank { "Sans titre" }} » sera définitivement supprimée.",
                confirmLabel = "Supprimer",
                onConfirm = {
                    viewModel.delete(note.id)
                    pendingDeleteNote = null
                },
                onDismiss = { pendingDeleteNote = null }
            )
        }
        if (showEditorDeleteConfirm) {
            GlassConfirmDialog(
                backdrop = backdrop,
                title = "Supprimer la note ?",
                message = "Cette action est irréversible.",
                confirmLabel = "Supprimer",
                onConfirm = {
                    val noteId = editorNoteId ?: lastOpenedId
                    noteId?.let { viewModel.delete(it) }
                    showEditorDeleteConfirm = false
                    closeRequestToken++
                },
                onDismiss = { showEditorDeleteConfirm = false }
            )
        }
        if (showResetAllConfirm) {
            GlassConfirmDialog(
                backdrop = backdrop,
                title = "Réinitialiser toutes les notes ?",
                message = "Toutes vos notes seront définitivement supprimées. Cette action est irréversible.",
                confirmLabel = "Réinitialiser",
                onConfirm = {
                    viewModel.deleteAll()
                    showResetAllConfirm = false
                },
                onDismiss = { showResetAllConfirm = false }
            )
        }
    }
}

private fun findNote(state: NotesUiState, id: String) =
    (state.pinned + state.others).firstOrNull { it.id == id }
