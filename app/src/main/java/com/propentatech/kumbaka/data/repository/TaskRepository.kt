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
    private val historyDao: TaskCompletionHistoryDao,
    private val context: android.content.Context
) {
    private val alarmScheduler by lazy { com.propentatech.kumbaka.notification.TaskAlarmScheduler(context) }
    
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
        alarmScheduler.scheduleTaskAlarms(task)
    }

    /**
     * Met à jour une tâche existante
     */
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
        alarmScheduler.scheduleTaskAlarms(task)
    }

    /**
     * Supprime une tâche et son historique
     */
    suspend fun deleteTask(taskId: String) {
        historyDao.deleteAllForTask(taskId)
        taskDao.deleteTaskById(taskId)
        alarmScheduler.cancelTaskAlarms(taskId)
    }

    /**
     * Récupère une tâche par son ID
     */
    suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)
    }

    /**
     * Fait défiler l'état de la tâche : TODO -> IN_PROGRESS -> DONE -> TODO
     */
    suspend fun cycleTaskState(taskId: String) {
        val task = taskDao.getTaskById(taskId) ?: return
        val today = LocalDate.now()
        
        val newState = when (task.state) {
            com.propentatech.kumbaka.data.model.TaskState.TODO -> com.propentatech.kumbaka.data.model.TaskState.IN_PROGRESS
            com.propentatech.kumbaka.data.model.TaskState.IN_PROGRESS -> com.propentatech.kumbaka.data.model.TaskState.DONE
            com.propentatech.kumbaka.data.model.TaskState.DONE -> com.propentatech.kumbaka.data.model.TaskState.TODO
            com.propentatech.kumbaka.data.model.TaskState.MISSED -> com.propentatech.kumbaka.data.model.TaskState.TODO
        }
        
        val newIsCompleted = newState == com.propentatech.kumbaka.data.model.TaskState.DONE
        
        val updatedTask = when (task.type) {
            TaskType.DAILY, TaskType.PERIODIC -> {
                if (!newIsCompleted && task.lastCompletedDate == today) {
                    historyDao.deleteByTaskAndDate(taskId, today)
                    task.copy(state = newState, isCompleted = false, lastCompletedDate = null)
                } else if (newIsCompleted) {
                    val history = TaskCompletionHistory(taskId = taskId, completionDate = today, taskType = task.type)
                    historyDao.insert(history)
                    task.copy(state = newState, isCompleted = true, lastCompletedDate = today)
                } else {
                    task.copy(state = newState, isCompleted = newIsCompleted)
                }
            }
            TaskType.OCCASIONAL -> {
                if (newIsCompleted && task.specificDate != null) {
                    val history = TaskCompletionHistory(taskId = taskId, completionDate = task.specificDate, taskType = task.type)
                    historyDao.insert(history)
                } else if (!newIsCompleted && task.state == com.propentatech.kumbaka.data.model.TaskState.DONE && task.specificDate != null) {
                    historyDao.deleteByTaskAndDate(taskId, task.specificDate)
                }
                task.copy(state = newState, isCompleted = newIsCompleted)
            }
        }
        
        taskDao.updateTask(updatedTask)
        alarmScheduler.scheduleTaskAlarms(updatedTask)
    }
    
    /**
     * Supprime toutes les tâches et leur historique
     */
    suspend fun deleteAllTasks() {
        historyDao.deleteAllHistory()
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
    
    /**
     * Récupère tout l'historique des complétions (pour export)
     */
    fun getAllTaskCompletionHistory(): Flow<List<TaskCompletionHistory>> {
        return historyDao.getAllHistory()
    }
    
    /**
     * Ajoute un enregistrement d'historique (pour import)
     */
    suspend fun addTaskCompletionHistory(history: TaskCompletionHistory) {
        historyDao.insert(history)
    }
}
