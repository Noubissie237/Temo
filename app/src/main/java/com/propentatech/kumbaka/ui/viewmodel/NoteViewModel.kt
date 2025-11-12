package com.propentatech.kumbaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.data.model.Note
import com.propentatech.kumbaka.data.model.NoteType
import com.propentatech.kumbaka.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel pour la gestion des notes
 * Gère l'état et la logique métier des notes
 */
class NoteViewModel(
    private val repository: NoteRepository = NoteRepository()
) : ViewModel() {

    val notes: StateFlow<List<Note>> = repository.notes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedNoteType = MutableStateFlow<NoteType?>(null)
    val selectedNoteType: StateFlow<NoteType?> = _selectedNoteType.asStateFlow()

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
     * Récupère une note par son ID
     */
    fun getNoteById(noteId: String): Note? {
        return repository.getNoteById(noteId)
    }

    /**
     * Filtre les notes par type
     */
    fun filterNotesByType(type: NoteType?) {
        _selectedNoteType.value = type
    }

    /**
     * Récupère les notes filtrées
     */
    fun getFilteredNotes(): List<Note> {
        return repository.getNotesByType(_selectedNoteType.value)
    }

    /**
     * Recherche des notes
     */
    fun searchNotes(query: String): List<Note> {
        return repository.searchNotes(query)
    }
}
