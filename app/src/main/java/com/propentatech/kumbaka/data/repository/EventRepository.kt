package com.propentatech.kumbaka.data.repository

import com.propentatech.kumbaka.data.database.EventDao
import com.propentatech.kumbaka.data.model.Event
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository pour gérer les événements
 * Gère les opérations CRUD sur les événements avec Room Database
 */
class EventRepository(private val eventDao: EventDao) {
    
    /**
     * Flow de tous les événements
     */
    val events: Flow<List<Event>> = eventDao.getAllEvents()

    /**
     * Récupère tous les événements (pour export)
     */
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    /**
     * Ajoute un nouvel événement
     */
    suspend fun addEvent(event: Event) {
        eventDao.insertEvent(event)
    }

    /**
     * Met à jour un événement existant
     */
    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    /**
     * Supprime un événement
     */
    suspend fun deleteEvent(eventId: String) {
        eventDao.deleteEventById(eventId)
    }

    /**
     * Récupère un événement par son ID
     */
    suspend fun getEventById(eventId: String): Event? {
        return eventDao.getEventById(eventId)
    }

    /**
     * Récupère les événements à venir
     */
    fun getUpcomingEvents(): Flow<List<Event>> {
        val today = LocalDate.now().toString()
        return eventDao.getUpcomingEvents(today)
    }
    
    /**
     * Supprime tous les événements
     */
    suspend fun deleteAllEvents() {
        eventDao.deleteAllEvents()
    }
}
