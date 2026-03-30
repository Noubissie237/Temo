package com.propentatech.kumbaka.data.database

import androidx.room.*
import com.propentatech.kumbaka.data.model.AdvisorLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AdvisorLogDao {
    @Query("SELECT * FROM advisor_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<AdvisorLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AdvisorLog)
}
