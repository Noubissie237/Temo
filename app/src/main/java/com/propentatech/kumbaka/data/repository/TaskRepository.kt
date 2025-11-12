package com.propentatech.kumbaka.data.repository

import com.propentatech.kumbaka.data.database.TaskDao
import com.propentatech.kumbaka.data.model.Task
import com.propentatech.kumbaka.data.model.TaskType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository pour gérer les tâches
 * Gère les opérations CRUD sur les tâches avec Room Database
 */
class TaskRepository(private val taskDao: TaskDao) {
    
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
     * Supprime une tâche
     */
    suspend fun deleteTask(taskId: String) {
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
     * Pour les tâches récurrentes, met à jour lastCompletedDate
     */
    suspend fun toggleTaskCompletion(taskId: String) {
        val task = taskDao.getTaskById(taskId) ?: return
        
        val updatedTask = when (task.type) {
            TaskType.DAILY, TaskType.PERIODIC -> {
                // Pour les tâches récurrentes, on met à jour lastCompletedDate
                if (task.lastCompletedDate == LocalDate.now()) {
                    // Décocher : retirer la date de complétion
                    task.copy(lastCompletedDate = null)
                } else {
                    // Cocher : marquer comme complétée aujourd'hui
                    task.copy(lastCompletedDate = LocalDate.now())
                }
            }
            TaskType.OCCASIONAL -> {
                // Pour les tâches occasionnelles, toggle isCompleted
                task.copy(isCompleted = !task.isCompleted)
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
}
