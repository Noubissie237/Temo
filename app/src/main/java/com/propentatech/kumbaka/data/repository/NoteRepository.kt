package com.propentatech.kumbaka.data.repository

import com.propentatech.kumbaka.data.model.Note
import com.propentatech.kumbaka.data.model.NoteType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository pour gérer les notes
 * Gère les opérations CRUD sur les notes
 */
class NoteRepository {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    /**
     * Ajoute une nouvelle note
     */
    fun addNote(note: Note) {
        _notes.value = _notes.value + note
    }

    /**
     * Met à jour une note existante
     */
    fun updateNote(note: Note) {
        _notes.value = _notes.value.map { 
            if (it.id == note.id) note else it 
        }
    }

    /**
     * Supprime une note
     */
    fun deleteNote(noteId: String) {
        _notes.value = _notes.value.filter { it.id != noteId }
    }

    /**
     * Récupère une note par son ID
     */
    fun getNoteById(noteId: String): Note? {
        return _notes.value.find { it.id == noteId }
    }

    /**
     * Filtre les notes par type
     */
    fun getNotesByType(type: NoteType?): List<Note> {
        return if (type == null) {
            _notes.value
        } else {
            _notes.value.filter { it.type == type }
        }
    }

    /**
     * Recherche des notes par titre ou contenu
     */
    fun searchNotes(query: String): List<Note> {
        return _notes.value.filter { note ->
            note.title.contains(query, ignoreCase = true) ||
            note.content.contains(query, ignoreCase = true)
        }
    }
}
