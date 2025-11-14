package com.propentatech.kumbaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.data.model.Task
import com.propentatech.kumbaka.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel pour la gestion des tâches
 * Gère l'état et la logique métier des tâches
 */
class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.tasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Ajoute une nouvelle tâche
     */
    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.addTask(task)
        }
    }

    /**
     * Met à jour une tâche
     */
    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    /**
     * Supprime une tâche
     */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    /**
     * Bascule le statut de complétion d'une tâche
     */
    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(taskId)
        }
    }


    /**
     * Récupère une tâche par son ID comme Flow
     */
    fun getTaskById(taskId: String): Flow<Task?> = flow {
        try {
            val task = repository.getTaskById(taskId)
            emit(task)
        } catch (e: Exception) {
            emit(null)
        }
    }
    
    /**
     * Met à jour l'ordre d'affichage des tâches après un drag & drop
     */
    fun updateTasksOrder(tasks: List<Task>) {
        viewModelScope.launch {
            val updatedTasks = tasks.mapIndexed { index, task ->
                task.copy(displayOrder = index)
            }
            repository.updateTasksOrder(updatedTasks)
        }
    }
}
