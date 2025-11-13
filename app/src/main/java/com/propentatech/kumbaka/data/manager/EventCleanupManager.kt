package com.propentatech.kumbaka.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.propentatech.kumbaka.data.repository.EventRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Gestionnaire de nettoyage des événements passés
 * Gère la suppression automatique et manuelle des anciens événements
 */
class EventCleanupManager(
    private val context: Context,
    private val eventRepository: EventRepository
) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "event_cleanup_prefs")
    
    companion object {
        private val LAST_CLEANUP_DATE = longPreferencesKey("last_cleanup_date")
        private const val CLEANUP_INTERVAL_DAYS = 7L // Vérifier tous les 7 jours
    }
    
    /**
     * Vérifie si un nettoyage automatique des notifications est nécessaire
     * Appelé au démarrage de l'application
     * NOTE: Ne supprime PAS les événements, annule seulement leurs notifications
     */
    suspend fun checkAndCleanupIfNeeded() {
        val preferences = context.dataStore.data.first()
        val lastCleanupMillis = preferences[LAST_CLEANUP_DATE] ?: 0L
        val lastCleanupDate = if (lastCleanupMillis > 0) {
            java.time.Instant.ofEpochMilli(lastCleanupMillis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
        } else {
            LocalDate.MIN
        }
        
        val today = LocalDate.now()
        val daysSinceLastCleanup = java.time.temporal.ChronoUnit.DAYS.between(lastCleanupDate, today)
        
        // Nettoyer les notifications si plus de CLEANUP_INTERVAL_DAYS jours depuis le dernier nettoyage
        if (daysSinceLastCleanup >= CLEANUP_INTERVAL_DAYS) {
            cleanupOldNotifications()
            updateLastCleanupDate()
        }
    }
    
    /**
     * Nettoie les notifications des événements passés
     * Annule les notifications WorkManager qui ne sont plus nécessaires
     */
    private suspend fun cleanupOldNotifications() {
        // Récupérer tous les événements passés
        val today = LocalDate.now()
        val allEvents = eventRepository.getAllEventsSync()
        val pastEvents = allEvents.filter { it.date < today }
        
        // Annuler les notifications pour chaque événement passé
        pastEvents.forEach { event ->
            eventRepository.cancelEventNotifications(event.id)
        }
    }
    
    /**
     * Supprime tous les événements passés
     * @return Nombre d'événements supprimés
     */
    suspend fun deleteAllPastEvents(): Int {
        val today = LocalDate.now()
        return eventRepository.deletePastEvents(today.toString())
    }
    
    /**
     * Supprime les événements passés de plus de X jours
     * @param days Nombre de jours
     * @return Nombre d'événements supprimés
     */
    suspend fun deletePastEventsOlderThan(days: Int): Int {
        val cutoffDate = LocalDate.now().minusDays(days.toLong())
        return eventRepository.deletePastEventsBefore(cutoffDate.toString())
    }
    
    /**
     * Compte le nombre d'événements passés
     */
    suspend fun countPastEvents(): Int {
        val today = LocalDate.now()
        return eventRepository.countPastEvents(today.toString())
    }
    
    /**
     * Compte le nombre d'événements passés de plus de X jours
     */
    suspend fun countPastEventsOlderThan(days: Int): Int {
        val cutoffDate = LocalDate.now().minusDays(days.toLong())
        return eventRepository.countPastEventsBefore(cutoffDate.toString())
    }
    
    /**
     * Met à jour la date du dernier nettoyage
     */
    private suspend fun updateLastCleanupDate() {
        context.dataStore.edit { preferences ->
            preferences[LAST_CLEANUP_DATE] = System.currentTimeMillis()
        }
    }
}

/**
 * Options de nettoyage des événements passés
 */
enum class CleanupOption(val days: Int, val label: String) {
    ALL(0, "Tous les événements passés"),
    OLDER_THAN_30_DAYS(30, "Plus de 30 jours"),
    OLDER_THAN_90_DAYS(90, "Plus de 90 jours"),
    OLDER_THAN_180_DAYS(180, "Plus de 6 mois")
}
