package com.propentatech.kumbaka.data.repository

import com.propentatech.kumbaka.data.database.TaskDao
import com.propentatech.kumbaka.data.database.TaskCompletionHistoryDao
import com.propentatech.kumbaka.data.model.Task
import com.propentatech.kumbaka.data.model.TaskCompletionHistory
import com.propentatech.kumbaka.data.model.TaskType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository pour gérer les tâches
 * Gère les opérations CRUD sur les tâches avec Room Database
 */
class TaskRepository(
    private val taskDao: TaskDao,
    private val historyDao: TaskCompletionHistoryDao
) {
    
    /**
     * Flow de toutes les tâches
     * S'actualise automatiquement quand la base de données change
     */
    val tasks: Flow<List<Task>> = taskDao.getAllTasks()

    /**
     * Récupère toutes les tâches (pour export)
     */
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    /**
     * Ajoute une nouvelle tâche
     */
    suspend fun addTask(task: Task) {
        taskDao.insertTask(task)
    }

    /**
     * Met à jour une tâche existante
     */
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    /**
     * Supprime une tâche et son historique
     */
    suspend fun deleteTask(taskId: String) {
        historyDao.deleteAllForTask(taskId)
        taskDao.deleteTaskById(taskId)
    }

    /**
     * Récupère une tâche par son ID
     */
    suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)
    }

    /**
     * Marque une tâche comme complétée ou non
     * Pour les tâches récurrentes, met à jour lastCompletedDate et enregistre dans l'historique
     */
    suspend fun toggleTaskCompletion(taskId: String) {
        val task = taskDao.getTaskById(taskId) ?: return
        val today = LocalDate.now()
        
        val updatedTask = when (task.type) {
            TaskType.DAILY, TaskType.PERIODIC -> {
                // Pour les tâches récurrentes, on met à jour lastCompletedDate
                if (task.lastCompletedDate == today) {
                    // Décocher : retirer la date de complétion et supprimer de l'historique
                    historyDao.deleteByTaskAndDate(taskId, today)
                    task.copy(lastCompletedDate = null)
                } else {
                    // Cocher : marquer comme complétée aujourd'hui et ajouter à l'historique
                    val history = TaskCompletionHistory(
                        taskId = taskId,
                        completionDate = today,
                        taskType = task.type
                    )
                    historyDao.insert(history)
                    task.copy(lastCompletedDate = today)
                }
            }
            TaskType.OCCASIONAL -> {
                // Pour les tâches occasionnelles, toggle isCompleted
                val newCompletedState = !task.isCompleted
                if (newCompletedState && task.specificDate != null) {
                    // Ajouter à l'historique si complétée
                    val history = TaskCompletionHistory(
                        taskId = taskId,
                        completionDate = task.specificDate,
                        taskType = task.type
                    )
                    historyDao.insert(history)
                } else if (!newCompletedState && task.specificDate != null) {
                    // Retirer de l'historique si décochée
                    historyDao.deleteByTaskAndDate(taskId, task.specificDate)
                }
                task.copy(isCompleted = newCompletedState)
            }
        }
        
        taskDao.updateTask(updatedTask)
    }
    
    /**
     * Supprime toutes les tâches
     */
    suspend fun deleteAllTasks() {
        taskDao.deleteAllTasks()
    }
    
    /**
     * Récupère l'historique des complétions entre deux dates
     */
    fun getCompletionHistory(startDate: LocalDate, endDate: LocalDate): Flow<List<TaskCompletionHistory>> {
        return historyDao.getCompletionsBetweenDates(startDate, endDate)
    }
    
    /**
     * Vérifie si une tâche a été complétée à une date donnée
     */
    suspend fun isTaskCompletedOnDate(taskId: String, date: LocalDate): Boolean {
        return historyDao.isTaskCompletedOnDate(taskId, date)
    }
    
    /**
     * Met à jour l'ordre d'affichage des tâches
     */
    suspend fun updateTasksOrder(tasks: List<Task>) {
        taskDao.updateTasks(tasks)
    }
}
