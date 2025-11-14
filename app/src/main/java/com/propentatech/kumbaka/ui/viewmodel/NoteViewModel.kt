package com.propentatech.kumbaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.data.model.Note
import com.propentatech.kumbaka.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel pour la gestion des notes
 * Gère l'état et la logique métier des notes
 */
class NoteViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    val notes: StateFlow<List<Note>> = repository.notes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Ajoute une nouvelle note
     */
    fun addNote(note: Note) {
        viewModelScope.launch {
            repository.addNote(note)
        }
    }

    /**
     * Met à jour une note
     */
    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    /**
     * Supprime une note
     */
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    /**
     * Récupère une note par son ID comme Flow
     */
    fun getNoteById(noteId: String): Flow<Note?> = flow {
        try {
            val note = repository.getNoteById(noteId)
            emit(note)
        } catch (e: Exception) {
            emit(null)
        }
    }

    /**
     * Recherche des notes
     */
    fun searchNotes(query: String): StateFlow<List<Note>> {
        _searchQuery.value = query
        return if (query.isBlank()) {
            notes
        } else {
            repository.searchNotes(query)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        }
    }
    
    /**
     * Met à jour l'ordre d'affichage des notes après un drag & drop
     */
    fun updateNotesOrder(notes: List<Note>) {
        viewModelScope.launch {
            val updatedNotes = notes.mapIndexed { index, note ->
                note.copy(displayOrder = index)
            }
            repository.updateNotesOrder(updatedNotes)
        }
    }
}
