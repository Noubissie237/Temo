package com.propentatech.kumbaka.data.repository

import android.content.Context
import com.propentatech.kumbaka.data.database.EventDao
import com.propentatech.kumbaka.data.model.Event
import com.propentatech.kumbaka.notification.NotificationScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository pour gérer les événements
 * Gère les opérations CRUD sur les événements avec Room Database
 * Planifie automatiquement les notifications pour les événements
 */
class EventRepository(
    private val eventDao: EventDao,
    private val context: Context
) {
    
    private val notificationScheduler by lazy { NotificationScheduler(context) }
    
    /**
     * Flow de tous les événements
     */
    val events: Flow<List<Event>> = eventDao.getAllEvents()

    /**
     * Récupère tous les événements (pour export)
     */
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    /**
     * Ajoute un nouvel événement et planifie ses notifications
     */
    suspend fun addEvent(event: Event) {
        eventDao.insertEvent(event)
        // Planifier les notifications pour le nouvel événement
        notificationScheduler.scheduleEventNotifications(event)
    }

    /**
     * Met à jour un événement existant et replanifie ses notifications
     */
    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
        // Replanifier les notifications avec les nouvelles données
        notificationScheduler.scheduleEventNotifications(event)
    }

    /**
     * Supprime un événement et annule ses notifications
     */
    suspend fun deleteEvent(eventId: String) {
        eventDao.deleteEventById(eventId)
        // Annuler toutes les notifications pour cet événement
        notificationScheduler.cancelEventNotifications(eventId)
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
     * Supprime tous les événements et annule toutes les notifications
     */
    suspend fun deleteAllEvents() {
        eventDao.deleteAllEvents()
        // Annuler toutes les notifications d'événements
        notificationScheduler.cancelAllEventNotifications()
    }
    
    /**
     * Replanifie toutes les notifications pour tous les événements
     * Utile après un import de données ou au démarrage de l'app
     */
    suspend fun rescheduleAllNotifications() {
        val allEvents = eventDao.getAllEventsSync()
        notificationScheduler.rescheduleAllNotifications(allEvents)
    }
}
