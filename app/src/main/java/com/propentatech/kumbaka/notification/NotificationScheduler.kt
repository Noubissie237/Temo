package com.propentatech.kumbaka.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.propentatech.kumbaka.data.model.Event
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Gestionnaire de planification des notifications pour les événements
 * Utilise WorkManager pour planifier les rappels
 */
class NotificationScheduler(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Planifie toutes les notifications pour un événement
     * - Notification J-1 à 9h00
     * - Notification 5 minutes avant (si l'événement a une heure)
     */
    fun scheduleEventNotifications(event: Event) {
        // Annuler les anciennes notifications si elles existent
        cancelEventNotifications(event.id)
        
        // Planifier la notification J-1
        scheduleDayBeforeNotification(event)
        
        // Planifier la notification 5 minutes avant (si l'événement a une heure)
        if (event.time != null) {
            scheduleFiveMinutesBeforeNotification(event)
        }
    }
    
    /**
     * Planifie une notification 1 jour avant l'événement à 9h00
     */
    private fun scheduleDayBeforeNotification(event: Event) {
        // Calculer le moment de la notification : J-1 à 9h00
        val notificationDateTime = event.date.minusDays(1).atTime(9, 0)
        val now = LocalDateTime.now()
        
        // Ne pas planifier si la date est déjà passée
        if (notificationDateTime.isBefore(now)) {
            return
        }
        
        // Calculer le délai en millisecondes
        val delay = calculateDelay(notificationDateTime)
        
        // Créer les données pour le Worker
        val inputData = Data.Builder()
            .putString(EventReminderWorker.KEY_EVENT_ID, event.id)
            .putString(EventReminderWorker.KEY_EVENT_TITLE, event.title)
            .putString(EventReminderWorker.KEY_EVENT_DESCRIPTION, event.description)
            .build()
        
        // Créer la requête de travail
        val workRequest = OneTimeWorkRequestBuilder<EventReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(TAG_EVENT_NOTIFICATION)
            .addTag(event.id)
            .build()
        
        // Planifier le travail avec un nom unique
        val workName = EventReminderWorker.WORK_NAME_PREFIX + event.id
        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Planifie une notification 5 minutes avant l'événement
     */
    private fun scheduleFiveMinutesBeforeNotification(event: Event) {
        val eventTime = event.time ?: return
        
        // Calculer le moment de la notification : 5 minutes avant l'heure de l'événement
        val notificationDateTime = event.date.atTime(eventTime).minusMinutes(5)
        val now = LocalDateTime.now()
        
        // Ne pas planifier si la date est déjà passée
        if (notificationDateTime.isBefore(now)) {
            return
        }
        
        // Calculer le délai en millisecondes
        val delay = calculateDelay(notificationDateTime)
        
        // Créer les données pour le Worker
        val inputData = Data.Builder()
            .putString(EventImmediateReminderWorker.KEY_EVENT_ID, event.id)
            .putString(EventImmediateReminderWorker.KEY_EVENT_TITLE, event.title)
            .putString(EventImmediateReminderWorker.KEY_EVENT_DESCRIPTION, event.description)
            .build()
        
        // Créer la requête de travail
        val workRequest = OneTimeWorkRequestBuilder<EventImmediateReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(TAG_EVENT_NOTIFICATION)
            .addTag(event.id)
            .build()
        
        // Planifier le travail avec un nom unique
        val workName = EventImmediateReminderWorker.WORK_NAME_PREFIX + event.id
        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Annule toutes les notifications planifiées pour un événement
     */
    fun cancelEventNotifications(eventId: String) {
        // Annuler les travaux WorkManager
        workManager.cancelUniqueWork(EventReminderWorker.WORK_NAME_PREFIX + eventId)
        workManager.cancelUniqueWork(EventImmediateReminderWorker.WORK_NAME_PREFIX + eventId)
        
        // Annuler les notifications affichées
        NotificationHelper.cancelEventNotifications(context, eventId)
    }
    
    /**
     * Annule toutes les notifications d'événements
     */
    fun cancelAllEventNotifications() {
        workManager.cancelAllWorkByTag(TAG_EVENT_NOTIFICATION)
    }
    
    /**
     * Replanifie toutes les notifications pour une liste d'événements
     * Utile après un import de données ou au démarrage de l'app
     */
    fun rescheduleAllNotifications(events: List<Event>) {
        // Annuler toutes les notifications existantes
        cancelAllEventNotifications()
        
        // Replanifier pour chaque événement à venir
        val today = LocalDate.now()
        events.filter { it.date >= today }.forEach { event ->
            scheduleEventNotifications(event)
        }
    }
    
    /**
     * Calcule le délai en millisecondes entre maintenant et une date/heure cible
     */
    private fun calculateDelay(targetDateTime: LocalDateTime): Long {
        val now = LocalDateTime.now()
        val targetMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val nowMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return maxOf(0, targetMillis - nowMillis)
    }
    
    companion object {
        private const val TAG_EVENT_NOTIFICATION = "event_notification"
    }
}
