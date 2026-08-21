package com.satanas.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satanas.notes.data.Note
import com.satanas.notes.data.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class NoteFilter(val label: String) {
    ALL("Notes"),
    PINNED("Épinglées")
}

data class NotesUiState(
    val query: String = "",
    val filter: NoteFilter = NoteFilter.ALL,
    val pinned: List<Note> = emptyList(),
    val others: List<Note> = emptyList(),
    val totalCount: Int = 0
) {
    val visibleCount: Int get() = pinned.size + others.size
}

class NotesViewModel(private val repository: NotesRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(NoteFilter.ALL)

    val uiState: StateFlow<NotesUiState> =
        combine(repository.notes, query, filter) { notes, currentQuery, currentFilter ->
            val matches = if (currentQuery.isBlank()) {
                notes
            } else {
                notes.filter { note ->
                    note.title.contains(currentQuery, ignoreCase = true) ||
                        note.content.contains(currentQuery, ignoreCase = true)
                }
            }
            val filtered = when (currentFilter) {
                NoteFilter.ALL -> matches
                NoteFilter.PINNED -> matches.filter { it.pinned }
            }
            NotesUiState(
                query = currentQuery,
                filter = currentFilter,
                pinned = filtered.filter { it.pinned },
                others = filtered.filterNot { it.pinned },
                totalCount = notes.size
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun setQuery(value: String) = query.update { value }

    fun setFilter(value: NoteFilter) {
        filter.value = value
    }

    fun createNote(): String = repository.createNote()

    fun updateContent(id: String, title: String, content: String) =
        repository.updateContent(id, title, content)

    fun togglePinned(id: String) = repository.togglePinned(id)

    fun setColor(id: String, colorIndex: Int) = repository.setColor(id, colorIndex)

    fun delete(id: String) = repository.delete(id)
}
