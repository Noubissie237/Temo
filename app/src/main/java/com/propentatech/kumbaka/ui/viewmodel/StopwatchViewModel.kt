package com.propentatech.kumbaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StopwatchViewModel : ViewModel() {
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _laps = MutableStateFlow<List<Long>>(emptyList())
    val laps: StateFlow<List<Long>> = _laps

    private var timerJob: Job? = null

    fun toggle() {
        if (_isRunning.value) {
            pause()
        } else {
            start()
        }
    }

    private fun start() {
        _isRunning.value = true
        timerJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (_isRunning.value) {
                delay(10)
                val now = System.currentTimeMillis()
                _elapsedTime.value += (now - lastTime)
                lastTime = now
            }
        }
    }

    fun pause() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun lap() {
        if (_elapsedTime.value > 0) {
            _laps.value = _laps.value + _elapsedTime.value
        }
    }

    fun reset() {
        pause()
        _elapsedTime.value = 0L
        _laps.value = emptyList()
    }

    fun formatTime(timeMs: Long): String {
        val hundredths = (timeMs / 10) % 100
        val seconds = (timeMs / 1000) % 60
        val minutes = (timeMs / 60000) % 60
        val hours = (timeMs / 3600000)
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, hundredths)
        } else {
            String.format("%02d:%02d.%02d", minutes, seconds, hundredths)
        }
    }
}
