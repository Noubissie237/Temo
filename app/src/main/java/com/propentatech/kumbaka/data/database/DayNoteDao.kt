package com.propentatech.kumbaka.data.database

import androidx.room.*
import com.propentatech.kumbaka.data.model.DayNote
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DayNoteDao {
    @Query("SELECT * FROM day_notes")
    fun getAllDayNotes(): Flow<List<DayNote>>

    @Query("SELECT * FROM day_notes WHERE date = :date LIMIT 1")
    suspend fun getDayNoteByDate(date: LocalDate): DayNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayNote(dayNote: DayNote)

    @Delete
    suspend fun deleteDayNote(dayNote: DayNote)
}
