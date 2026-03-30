package com.propentatech.kumbaka.data.database

import androidx.room.*
import com.propentatech.kumbaka.data.model.PlanningSession
import com.propentatech.kumbaka.data.model.PlanningType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PlanningSessionDao {

    @Query("SELECT * FROM planning_sessions ORDER BY date ASC, startTime ASC")
    fun getAllSessions(): Flow<List<PlanningSession>>

    @Query("SELECT * FROM planning_sessions WHERE date = :date ORDER BY startTime ASC")
    fun getSessionsByDate(date: String): Flow<List<PlanningSession>>

    @Query("SELECT * FROM planning_sessions WHERE type = :type ORDER BY date ASC, startTime ASC")
    fun getSessionsByType(type: String): Flow<List<PlanningSession>>

    @Query("SELECT * FROM planning_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): PlanningSession?

    @Query("SELECT * FROM planning_sessions WHERE isImportant = 1 ORDER BY date ASC")
    fun getImportantSessions(): Flow<List<PlanningSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PlanningSession)

    @Update
    suspend fun update(session: PlanningSession)

    @Query("DELETE FROM planning_sessions WHERE id = :id")
    suspend fun deleteById(id: String)
}
