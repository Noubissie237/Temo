package com.propentatech.kumbaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.data.model.Event
import com.propentatech.kumbaka.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/**
 * ViewModel pour la gestion des événements
 * Gère l'état et la logique métier des événements et du calendrier
 */
class EventViewModel(
    private val repository: EventRepository
) : ViewModel() {

    val events: StateFlow<List<Event>> = repository.events
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    /**
     * Ajoute un nouvel événement
     */
    fun addEvent(event: Event) {
        viewModelScope.launch {
            repository.addEvent(event)
        }
    }

    /**
     * Met à jour un événement
     */
    fun updateEvent(event: Event) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    /**
     * Supprime un événement
     */
    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
        }
    }

    /**
     * Récupère un événement par son ID
     */
    suspend fun getEventById(eventId: String): Event? {
        return repository.getEventById(eventId)
    }

    /**
     * Sélectionne une date
     */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /**
     * Change le mois affiché
     */
    fun changeMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
    }

    /**
     * Mois suivant
     */
    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    /**
     * Mois précédent
     */
    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    /**
     * Récupère les événements à venir
     */
    fun getUpcomingEvents(): StateFlow<List<Event>> {
        return repository.getUpcomingEvents()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}
