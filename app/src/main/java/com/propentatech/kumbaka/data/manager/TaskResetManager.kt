package com.propentatech.kumbaka.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.propentatech.kumbaka.data.model.TaskType
import com.propentatech.kumbaka.data.repository.TaskRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Gestionnaire de réinitialisation des tâches
 * Gère la réinitialisation automatique des tâches à minuit
 */
class TaskResetManager(
    private val context: Context,
    private val taskRepository: TaskRepository
) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "task_reset")
        private val LAST_RESET_DATE = stringPreferencesKey("last_reset_date")
    }
    
    /**
     * Vérifie si une réinitialisation est nécessaire et l'effectue
     */
    suspend fun checkAndResetTasks() {
        val today = LocalDate.now().toString()
        val lastResetDate = getLastResetDate()
        
        // Si la dernière réinitialisation n'est pas aujourd'hui
        if (lastResetDate != today) {
            resetTasks()
            saveLastResetDate(today)
        }
    }
    
    /**
     * Réinitialise les tâches selon leur type
     */
    private suspend fun resetTasks() {
        val tasks = taskRepository.getAllTasks().first()
        val today = LocalDate.now()
        
        tasks.forEach { task ->
            when (task.type) {
                TaskType.DAILY, TaskType.PERIODIC -> {
                    // Réinitialiser lastCompletedDate si ce n'est pas aujourd'hui
                    if (task.lastCompletedDate != null && task.lastCompletedDate != today) {
                        val updatedTask = task.copy(lastCompletedDate = null)
                        taskRepository.updateTask(updatedTask)
                    }
                }
                TaskType.OCCASIONAL -> {
                    // Supprimer les tâches occasionnelles passées et complétées
                    if (task.specificDate != null && 
                        task.specificDate.isBefore(today) && 
                        task.isCompleted) {
                        taskRepository.deleteTask(task.id)
                    }
                }
            }
        }
    }
    
    /**
     * Récupère la date de la dernière réinitialisation
     */
    private suspend fun getLastResetDate(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[LAST_RESET_DATE]
    }
    
    /**
     * Sauvegarde la date de la dernière réinitialisation
     */
    private suspend fun saveLastResetDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_RESET_DATE] = date
        }
    }
}
