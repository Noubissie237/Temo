package com.propentatech.kumbaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.data.model.DayNote
import com.propentatech.kumbaka.data.repository.CalendarData
import com.propentatech.kumbaka.data.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendarViewModel(private val repository: CalendarRepository) : ViewModel() {

    // Date actuelle pour la navigation (Initiale = Aujourd'hui)
    private val _selectedMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val selectedMonth: StateFlow<LocalDate> = _selectedMonth.asStateFlow()

    // Vue annuelle ou mensuelle
    private val _isYearlyView = MutableStateFlow(false)
    val isYearlyView: StateFlow<Boolean> = _isYearlyView.asStateFlow()

    // Données consolidées
    val calendarData: StateFlow<CalendarData> = repository.getCalendarData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CalendarData(emptyList(), emptyList(), emptyList())
        )

    fun nextPage() {
        if (_isYearlyView.value) {
            _selectedMonth.value = _selectedMonth.value.plusYears(1)
        } else {
            _selectedMonth.value = _selectedMonth.value.plusMonths(1)
        }
    }

    fun prevPage() {
        if (_isYearlyView.value) {
            _selectedMonth.value = _selectedMonth.value.minusYears(1)
        } else {
            _selectedMonth.value = _selectedMonth.value.minusMonths(1)
        }
    }

    fun setYear(year: Int) {
        _selectedMonth.value = _selectedMonth.value.withYear(year)
    }

    fun setMonth(month: Int) {
        _selectedMonth.value = _selectedMonth.value.withMonth(month)
    }

    fun toggleViewMode() {
        _isYearlyView.value = !_isYearlyView.value
    }

    fun saveDayNote(date: LocalDate, content: String, colorHex: String?, isImportant: Boolean = false) {
        viewModelScope.launch {
            repository.saveDayNote(DayNote(date, content, colorHex, isImportant))
        }
    }

    fun deleteDayNote(dayNote: DayNote) {
        viewModelScope.launch {
            repository.deleteDayNote(dayNote)
        }
    }
}

class CalendarViewModelFactory(private val repository: CalendarRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
