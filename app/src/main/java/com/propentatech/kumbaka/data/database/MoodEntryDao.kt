package com.propentatech.kumbaka.data.database

import androidx.room.*
import com.propentatech.kumbaka.data.model.MoodEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE date = :date LIMIT 1")
    fun getEntryForDate(date: LocalDate): Flow<MoodEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MoodEntry)
}
