package com.propentatech.kumbaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.data.model.Habit
import com.propentatech.kumbaka.data.model.HabitLog
import com.propentatech.kumbaka.data.model.MoodEntry
import com.propentatech.kumbaka.data.model.UserMood
import com.propentatech.kumbaka.data.repository.LifestyleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class LifestyleViewModel(
    private val repository: LifestyleRepository
) : ViewModel() {

    val habits = repository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayLogs = repository.getLogsForDate(LocalDate.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMood = repository.getMoodForDate(LocalDate.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            repository.insertHabit(habit)
        }
    }

    fun toggleHabitToday(habitId: String, status: com.propentatech.kumbaka.data.model.HabitStatus) {
        viewModelScope.launch {
            val log = HabitLog(
                habitId = habitId,
                date = LocalDate.now(),
                status = status
            )
            repository.logHabit(log)
        }
    }

    fun logTodayMood(mood: UserMood, note: String = "") {
        viewModelScope.launch {
            val currentMood = todayMood.value
            val entry = MoodEntry(
                id = currentMood?.id ?: java.util.UUID.randomUUID().toString(),
                date = LocalDate.now(),
                mood = mood,
                note = note
            )
            repository.logMood(entry)
        }
    }
}
