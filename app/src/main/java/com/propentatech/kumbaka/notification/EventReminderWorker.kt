package com.propentatech.kumbaka.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.propentatech.kumbaka.data.database.KumbakaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker pour afficher une notification de rappel 1 jour avant un événement
 * Exécuté par WorkManager au moment planifié
 */
class EventReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Récupérer les données de l'événement depuis les paramètres
            val eventId = inputData.getString(KEY_EVENT_ID) ?: return@withContext Result.failure()
            val eventTitle = inputData.getString(KEY_EVENT_TITLE) ?: return@withContext Result.failure()
            val eventDescription = inputData.getString(KEY_EVENT_DESCRIPTION) ?: ""
            
            // Vérifier que l'événement existe toujours dans la base de données
            val database = KumbakaDatabase.getInstance(applicationContext)
            val event = database.eventDao().getEventById(eventId)
            
            if (event == null) {
                // L'événement a été supprimé, annuler la notification
                return@withContext Result.success()
            }
            
            // Afficher la notification
            NotificationHelper.showEventNotification(
                context = applicationContext,
                eventId = eventId,
                eventTitle = eventTitle,
                eventDescription = eventDescription,
                notificationType = NotificationType.DAY_BEFORE
            )
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
    
    companion object {
        const val KEY_EVENT_ID = "event_id"
        const val KEY_EVENT_TITLE = "event_title"
        const val KEY_EVENT_DESCRIPTION = "event_description"
        const val WORK_NAME_PREFIX = "event_reminder_day_before_"
    }
}
