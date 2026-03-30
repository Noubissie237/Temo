package com.propentatech.kumbaka.data.repository

import com.propentatech.kumbaka.data.database.PlanningSessionDao
import com.propentatech.kumbaka.data.model.PlanningSession
import com.propentatech.kumbaka.data.model.PlanningType
import com.propentatech.kumbaka.notification.PlanningAlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class PlanningRepository(
    private val dao: PlanningSessionDao,
    private val context: android.content.Context
) {
    private val alarmScheduler by lazy { PlanningAlarmScheduler(context) }

    val allSessions: Flow<List<PlanningSession>> = dao.getAllSessions()

    fun getSessionsByDate(date: LocalDate): Flow<List<PlanningSession>> =
        dao.getSessionsByDate(date.toString())

    fun getSessionsByType(type: PlanningType): Flow<List<PlanningSession>> =
        dao.getSessionsByType(type.name)

    val importantSessions: Flow<List<PlanningSession>> = dao.getImportantSessions()

    suspend fun addSession(session: PlanningSession) {
        dao.insert(session)
        alarmScheduler.scheduleAlarms(session)
    }

    suspend fun updateSession(session: PlanningSession) {
        dao.update(session)
        alarmScheduler.scheduleAlarms(session)
    }

    suspend fun deleteSession(id: String) {
        alarmScheduler.cancelAlarms(id)
        dao.deleteById(id)
    }

    suspend fun getSessionById(id: String): PlanningSession? = dao.getSessionById(id)
}
