package com.propentatech.kumbaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.data.model.PlanningSession
import com.propentatech.kumbaka.data.model.PlanningType
import com.propentatech.kumbaka.data.repository.PlanningRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class PlanningViewModel(private val repository: PlanningRepository) : ViewModel() {

    val allSessions: StateFlow<List<PlanningSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val importantSessions: StateFlow<List<PlanningSession>> = repository.importantSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getSessionsByDate(date: LocalDate): Flow<List<PlanningSession>> =
        repository.getSessionsByDate(date)

    fun getSessionsByType(type: PlanningType): Flow<List<PlanningSession>> =
        repository.getSessionsByType(type)

    fun addSession(session: PlanningSession) {
        viewModelScope.launch { repository.addSession(session) }
    }

    fun updateSession(session: PlanningSession) {
        viewModelScope.launch { repository.updateSession(session) }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch { repository.deleteSession(id) }
    }

    fun toggleComplete(session: PlanningSession) {
        viewModelScope.launch {
            repository.updateSession(session.copy(isCompleted = !session.isCompleted))
        }
    }
}

class PlanningViewModelFactory(private val repository: PlanningRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PlanningViewModel(repository) as T
    }
}
