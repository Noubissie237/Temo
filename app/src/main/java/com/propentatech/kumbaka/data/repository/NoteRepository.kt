package com.propentatech.kumbaka.data.repository

import com.propentatech.kumbaka.data.database.NoteDao
import com.propentatech.kumbaka.data.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour gérer les notes
 * Gère les opérations CRUD sur les notes avec Room Database
 */
class NoteRepository(private val noteDao: NoteDao) {
    
    /**
     * Flow de toutes les notes
     */
    val notes: Flow<List<Note>> = noteDao.getAllNotes()

    /**
     * Ajoute une nouvelle note
     */
    suspend fun addNote(note: Note) {
        noteDao.insertNote(note)
    }

    /**
     * Met à jour une note existante
     */
    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    /**
     * Supprime une note
     */
    suspend fun deleteNote(noteId: String) {
        noteDao.deleteNoteById(noteId)
    }

    /**
     * Récupère une note par son ID
     */
    suspend fun getNoteById(noteId: String): Note? {
        return noteDao.getNoteById(noteId)
    }

    /**
     * Recherche des notes par titre ou contenu
     */
    fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query)
    }
}
