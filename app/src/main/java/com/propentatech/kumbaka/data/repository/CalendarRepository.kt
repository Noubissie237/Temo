package com.propentatech.kumbaka.data.repository

import com.propentatech.kumbaka.data.database.DayNoteDao
import com.propentatech.kumbaka.data.database.EventDao
import com.propentatech.kumbaka.data.database.PlanningSessionDao
import com.propentatech.kumbaka.data.model.DayNote
import com.propentatech.kumbaka.data.model.Event
import com.propentatech.kumbaka.data.model.PlanningSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/**
 * Repository unifié pour le calendrier MyLive Omniscient.
 * Regroupe les événements, les sessions de planning et les notes personnalisées.
 */
class CalendarRepository(
    private val eventDao: EventDao,
    private val planningDao: PlanningSessionDao,
    private val dayNoteDao: DayNoteDao
) {
    /**
     * Retourne toutes les données consolidées pour le calendrier
     */
    fun getCalendarData(): Flow<CalendarData> {
        return combine(
            eventDao.getAllEvents(),
            planningDao.getAllSessions(),
            dayNoteDao.getAllDayNotes()
        ) { events, sessions, dayNotes ->
            CalendarData(events, sessions, dayNotes)
        }
    }

    suspend fun saveDayNote(dayNote: DayNote) {
        dayNoteDao.insertDayNote(dayNote)
    }

    suspend fun getDayNoteByDate(date: LocalDate): DayNote? {
        return dayNoteDao.getDayNoteByDate(date)
    }

    suspend fun deleteDayNote(dayNote: DayNote) {
        dayNoteDao.deleteDayNote(dayNote)
    }
}

data class CalendarData(
    val events: List<Event>,
    val planningSessions: List<PlanningSession>,
    val dayNotes: List<DayNote>
)
