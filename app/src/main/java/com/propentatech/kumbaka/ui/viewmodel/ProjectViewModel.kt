package com.propentatech.kumbaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.data.model.Milestone
import com.propentatech.kumbaka.data.model.Project
import com.propentatech.kumbaka.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectViewModel(
    private val repository: ProjectRepository
) : ViewModel() {

    val projects = repository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentMilestones = MutableStateFlow<List<Milestone>>(emptyList())
    val currentMilestones: StateFlow<List<Milestone>> = _currentMilestones

    fun addProject(project: Project) {
        viewModelScope.launch {
            repository.insertProject(project)
        }
    }

    fun loadMilestones(projectId: String) {
        viewModelScope.launch {
            repository.getMilestonesForProject(projectId).collect {
                _currentMilestones.value = it
            }
        }
    }

    fun addMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repository.insertMilestone(milestone)
        }
    }

    fun toggleMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repository.updateMilestone(milestone.copy(isCompleted = !milestone.isCompleted))
        }
    }
}
