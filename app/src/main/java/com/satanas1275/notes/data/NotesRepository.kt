package com.satanas1275.notes.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "notes")

private val NOTES_KEY = stringPreferencesKey("notes_json")

object NotesRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    @Volatile
    private var initialized = false

    private lateinit var appContext: Context

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            initialized = true
            appContext = context.applicationContext
            scope.launch {
                appContext.dataStore.data.collect { preferences ->
                    _notes.value = Note.listFromJson(preferences[NOTES_KEY])
                        .sortedWith(
                            compareByDescending<Note> { it.pinned }
                                .thenByDescending { it.modifiedAt }
                        )
                }
            }
        }
    }

    fun createNote(): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        update { notes ->
            notes + Note(id = id, title = "", content = "", createdAt = now, modifiedAt = now)
        }
        return id
    }

    fun updateContent(id: String, title: String, content: String) {
        update { notes ->
            notes.map { note ->
                if (note.id == id && (note.title != title || note.content != content)) {
                    note.copy(title = title, content = content, modifiedAt = System.currentTimeMillis())
                } else {
                    note
                }
            }
        }
    }

    fun togglePinned(id: String) {
        update { notes ->
            notes.map { note ->
                if (note.id == id) {
                    note.copy(pinned = !note.pinned, modifiedAt = System.currentTimeMillis())
                } else {
                    note
                }
            }
        }
    }

    fun setColor(id: String, colorIndex: Int) {
        update { notes ->
            notes.map { note -> if (note.id == id) note.copy(colorIndex = colorIndex) else note }
        }
    }

    fun delete(id: String) {
        update { notes -> notes.filterNot { it.id == id } }
    }

    private fun update(transform: (List<Note>) -> List<Note>) {
        if (!initialized) return
        scope.launch {
            appContext.dataStore.edit { preferences ->
                val current = Note.listFromJson(preferences[NOTES_KEY])
                preferences[NOTES_KEY] = Note.listToJson(transform(current))
            }
        }
    }
}
