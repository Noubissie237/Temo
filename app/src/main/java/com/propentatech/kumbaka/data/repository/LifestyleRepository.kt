package com.propentatech.kumbaka.data.repository

import com.propentatech.kumbaka.data.database.HabitDao
import com.propentatech.kumbaka.data.database.HabitLogDao
import com.propentatech.kumbaka.data.database.MoodEntryDao
import com.propentatech.kumbaka.data.model.Habit
import com.propentatech.kumbaka.data.model.HabitLog
import com.propentatech.kumbaka.data.model.MoodEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class LifestyleRepository(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val moodEntryDao: MoodEntryDao
) {
    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    fun getLogsForDate(date: LocalDate): Flow<List<HabitLog>> = habitLogDao.getLogsForDate(date)

    fun getAllMoodEntries(): Flow<List<MoodEntry>> = moodEntryDao.getAllEntries()

    fun getMoodForDate(date: LocalDate): Flow<MoodEntry?> = moodEntryDao.getEntryForDate(date)

    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)
    
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    suspend fun logHabit(log: HabitLog) = habitLogDao.insertLog(log)

    suspend fun logMood(entry: MoodEntry) = moodEntryDao.insertEntry(entry)
}
