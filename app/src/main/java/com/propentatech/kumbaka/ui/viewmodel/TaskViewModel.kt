package com.propentatech.kumbaka.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.data.model.Task
import com.propentatech.kumbaka.data.repository.TaskRepository
import com.propentatech.kumbaka.notification.ActionNotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository,
    private val context: Context? = null
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.tasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.addTask(task)
            context?.let { ActionNotificationHelper.onTaskCreated(it, task.title) }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch { repository.deleteTask(taskId) }
    }

    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            repository.cycleTaskState(taskId)
            // Notify when completed
            tasks.value.find { it.id == taskId }?.let { task ->
                if (task.state == com.propentatech.kumbaka.data.model.TaskState.IN_PROGRESS) {
                    context?.let { ctx -> ActionNotificationHelper.onTaskCompleted(ctx, task.title) }
                }
            }
        }
    }

    fun getTaskById(taskId: String): Flow<Task?> = flow {
        try { emit(repository.getTaskById(taskId)) } catch (e: Exception) { emit(null) }
    }

    fun updateTasksOrder(tasks: List<Task>) {
        viewModelScope.launch {
            repository.updateTasksOrder(tasks.mapIndexed { index, task -> task.copy(displayOrder = index) })
        }
    }
}
