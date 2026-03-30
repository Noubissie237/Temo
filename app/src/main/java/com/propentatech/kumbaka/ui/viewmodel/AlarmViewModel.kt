package com.propentatech.kumbaka.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.KumbakaApplication
import com.propentatech.kumbaka.data.model.Alarm
import com.propentatech.kumbaka.notification.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val alarmDao = (application as KumbakaApplication).database.alarmDao()
    private val scheduler = AlarmScheduler(application)

    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmDao.insertAlarm(alarm)
            scheduler.schedule(alarm)
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmDao.updateAlarm(alarm)
            if (alarm.isEnabled) {
                scheduler.schedule(alarm)
            } else {
                scheduler.cancel(alarm)
            }
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            alarmDao.updateAlarm(updated)
            if (updated.isEnabled) {
                scheduler.schedule(updated)
            } else {
                scheduler.cancel(updated)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            scheduler.cancel(alarm)
            alarmDao.deleteAlarm(alarm)
        }
    }
}
