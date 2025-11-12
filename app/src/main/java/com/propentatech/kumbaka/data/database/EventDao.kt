package com.propentatech.kumbaka.data.database

import androidx.room.*
import com.propentatech.kumbaka.data.model.Event
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object pour les événements
 * Définit les opérations de base de données pour les événements
 */
@Dao
interface EventDao {
    
    /**
     * Récupère tous les événements triés par date
     */
    @Query("SELECT * FROM events ORDER BY date ASC, time ASC")
    fun getAllEvents(): Flow<List<Event>>
    
    /**
     * Récupère un événement par son ID
     */
    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): Event?
    
    /**
     * Récupère les événements à venir (date >= aujourd'hui)
     */
    @Query("SELECT * FROM events WHERE date >= :today ORDER BY date ASC, time ASC")
    fun getUpcomingEvents(today: String): Flow<List<Event>>
    
    /**
     * Insère un nouvel événement
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)
    
    /**
     * Insère plusieurs événements
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<Event>)
    
    /**
     * Met à jour un événement existant
     */
    @Update
    suspend fun updateEvent(event: Event)
    
    /**
     * Supprime un événement
     */
    @Delete
    suspend fun deleteEvent(event: Event)
    
    /**
     * Supprime un événement par son ID
     */
    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: String)
    
    /**
     * Supprime tous les événements
     */
    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
    
    /**
     * Compte le nombre total d'événements
     */
    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventCount(): Int
}
