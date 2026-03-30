package com.propentatech.kumbaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.data.database.AdvisorLogDao
import com.propentatech.kumbaka.data.model.AdvisorLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdvisorViewModel(private val advisorLogDao: AdvisorLogDao) : ViewModel() {
    private val _latestLog = MutableStateFlow<AdvisorLog?>(null)
    val latestLog: StateFlow<AdvisorLog?> = _latestLog.asStateFlow()

    init {
        viewModelScope.launch {
            // Observer le dernier log via getAllLogs
            advisorLogDao.getAllLogs().collectLatest { logs ->
                _latestLog.value = logs.firstOrNull()
            }
        }
    }
}

class AdvisorViewModelFactory(private val advisorLogDao: AdvisorLogDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdvisorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdvisorViewModel(advisorLogDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
