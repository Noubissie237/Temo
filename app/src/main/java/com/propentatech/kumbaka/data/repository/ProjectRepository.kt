package com.propentatech.kumbaka.data.repository

import com.propentatech.kumbaka.data.database.MilestoneDao
import com.propentatech.kumbaka.data.database.ProjectDao
import com.propentatech.kumbaka.data.model.Milestone
import com.propentatech.kumbaka.data.model.Project
import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val milestoneDao: MilestoneDao
) {

    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()

    fun getMilestonesForProject(projectId: String): Flow<List<Milestone>> = 
        milestoneDao.getMilestonesForProject(projectId)

    suspend fun insertProject(project: Project) = projectDao.insertProject(project)

    suspend fun updateProject(project: Project) = projectDao.updateProject(project)

    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)

    suspend fun insertMilestone(milestone: Milestone) = milestoneDao.insertMilestone(milestone)

    suspend fun updateMilestone(milestone: Milestone) = milestoneDao.updateMilestone(milestone)

    suspend fun deleteMilestone(milestone: Milestone) = milestoneDao.deleteMilestone(milestone)
}
