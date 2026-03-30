package com.propentatech.kumbaka

import android.app.Application
import com.propentatech.kumbaka.data.MockData
import com.propentatech.kumbaka.data.database.KumbakaDatabase
import com.propentatech.kumbaka.data.manager.DataExportImportManager
import com.propentatech.kumbaka.data.manager.TaskResetManager
import com.propentatech.kumbaka.data.preferences.ThemePreferences
import com.propentatech.kumbaka.data.repository.EventRepository
import com.propentatech.kumbaka.data.repository.NoteRepository
import com.propentatech.kumbaka.data.repository.TaskRepository
import com.propentatech.kumbaka.notification.NotificationHelper
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.propentatech.kumbaka.advisor.AdvisorBrainWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Classe Application personnalisée pour Kumbaka
 * Initialise la base de données et charge les données mock au premier lancement
 */
class KumbakaApplication : Application() {
    
    // Scope pour les opérations asynchrones
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Base de données
    val database by lazy { KumbakaDatabase.getInstance(this) }
    
    // Préférences
    val themePreferences by lazy { ThemePreferences(this) }
    val securityPreferences by lazy { com.propentatech.kumbaka.data.preferences.SecurityPreferences(this) }
    
    // Repositories
    val taskRepository by lazy { TaskRepository(database.taskDao(), database.taskCompletionHistoryDao(), this) }
    val eventRepository by lazy { EventRepository(database.eventDao(), this) }
    val noteRepository by lazy { NoteRepository(database.noteDao()) }
    val financeRepository by lazy { com.propentatech.kumbaka.data.repository.FinanceRepository(database.transactionDao(), database.transactionCategoryDao()) }
    val lifestyleRepository by lazy { com.propentatech.kumbaka.data.repository.LifestyleRepository(database.habitDao(), database.habitLogDao(), database.moodEntryDao()) }
    val projectRepository by lazy { com.propentatech.kumbaka.data.repository.ProjectRepository(database.projectDao(), database.milestoneDao()) }
    val planningRepository by lazy { com.propentatech.kumbaka.data.repository.PlanningRepository(database.planningSessionDao(), this) }
    val calendarRepository by lazy { com.propentatech.kumbaka.data.repository.CalendarRepository(database.eventDao(), database.planningSessionDao(), database.dayNoteDao()) }
    val cloudPreferences by lazy { com.propentatech.kumbaka.data.preferences.CloudPreferences(this) }
    val googleDriveManager by lazy { com.propentatech.kumbaka.data.cloud.GoogleDriveManager(this) }
    
    // Data Manager
    val dataExportImportManager by lazy { 
        DataExportImportManager(this, taskRepository, noteRepository, eventRepository) 
    }
    
    // Task Reset Manager
    val taskResetManager by lazy {
        TaskResetManager(this, taskRepository)
    }
    
    // Event Cleanup Manager
    val eventCleanupManager by lazy {
        com.propentatech.kumbaka.data.manager.EventCleanupManager(this, eventRepository)
    }

    override fun onCreate() {
        super.onCreate()
        
        // Créer les canaux de notification
        NotificationHelper.createNotificationChannels(this)
        com.propentatech.kumbaka.advisor.AdvisorDispatcher.createChannel(this)
        
        // Planifier le conseiller expert (AdvisorBrainWorker) une fois par jour
        val advisorWorkRequest = PeriodicWorkRequestBuilder<AdvisorBrainWorker>(
            24, TimeUnit.HOURS
        ).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            AdvisorBrainWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            advisorWorkRequest
        )
        
        // Vérifier et réinitialiser les tâches au démarrage

        applicationScope.launch {
            taskResetManager.checkAndResetTasks()
            
            // Replanifier toutes les notifications au démarrage
            eventRepository.rescheduleAllNotifications()
            
            // Nettoyer les anciennes notifications si nécessaire
            eventCleanupManager.checkAndCleanupIfNeeded()
        }
    }
}
