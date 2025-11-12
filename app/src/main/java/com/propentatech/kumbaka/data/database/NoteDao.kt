package com.propentatech.kumbaka.data.database

import androidx.room.*
import com.propentatech.kumbaka.data.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les opérations sur les notes
 */
@Dao
interface NoteDao {
    
    // Lecture
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): Note?
    
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchNotes(query: String): Flow<List<Note>>
    
    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNoteCount(): Int
    
    // Écriture
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<Note>)
    
    @Update
    suspend fun updateNote(note: Note)
    
    // Suppression
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)
    
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}
