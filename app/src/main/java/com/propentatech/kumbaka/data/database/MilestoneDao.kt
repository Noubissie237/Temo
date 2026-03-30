package com.propentatech.kumbaka.data.database

import androidx.room.*
import com.propentatech.kumbaka.data.model.Milestone
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones WHERE projectId = :projectId ORDER BY displayOrder ASC")
    fun getMilestonesForProject(projectId: String): Flow<List<Milestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: Milestone)

    @Update
    suspend fun updateMilestone(milestone: Milestone)

    @Delete
    suspend fun deleteMilestone(milestone: Milestone)
}
