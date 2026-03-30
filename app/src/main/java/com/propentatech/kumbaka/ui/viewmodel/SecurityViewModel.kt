package com.propentatech.kumbaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.propentatech.kumbaka.data.preferences.SecurityPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * États d'authentification de l'application
 */
enum class AuthState {
    UNKNOWN,
    SETUP_REQUIRED, // Premier lancement, définir PIN
    LOCKED,         // PIN défini, doit se connecter
    AUTHENTICATED   // Accès autorisé
}

class SecurityViewModel(private val prefs: SecurityPreferences) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.UNKNOWN)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _username = MutableStateFlow(prefs.getUsername() ?: "")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _pinBuffer = MutableStateFlow("")
    val pinBuffer: StateFlow<String> = _pinBuffer.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val SESSION_TIMEOUT_MS = 60 * 60 * 1000L // 1 heure

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        if (!prefs.isSecurityEnabled()) {
            _authState.value = AuthState.SETUP_REQUIRED
        } else {
            // Sécurité stricte : toujours demander le PIN si non déjà authentifié
            if (_authState.value != AuthState.AUTHENTICATED) {
                _authState.value = AuthState.LOCKED
            }
        }
    }

    fun lock() {
        if (prefs.isSecurityEnabled()) {
            _authState.value = AuthState.LOCKED
        }
    }

    fun onPinDigit(digit: String) {
        if (_pinBuffer.value.length < 4) {
            _pinBuffer.value += digit
            if (_pinBuffer.value.length == 4) {
                verifyPin(_pinBuffer.value)
            }
        }
    }

    fun onPinDelete() {
        if (_pinBuffer.value.isNotEmpty()) {
            _pinBuffer.value = _pinBuffer.value.dropLast(1)
        }
    }

    private fun verifyPin(pin: String) {
        val savedPin = prefs.getPin()
        if (pin == savedPin) {
            login()
        } else {
            _error.value = "Code PIN incorrect"
            _pinBuffer.value = ""
        }
    }

    fun login() {
        prefs.setLastLoginTime(System.currentTimeMillis())
        _authState.value = AuthState.AUTHENTICATED
        _pinBuffer.value = ""
        _error.value = null
    }

    fun setupSecurity(username: String, pin: String) {
        viewModelScope.launch {
            prefs.setUsername(username)
            prefs.setPin(pin)
            prefs.setLastLoginTime(System.currentTimeMillis())
            _authState.value = AuthState.AUTHENTICATED
        }
    }

    fun isFingerprintEnabled(): Boolean = prefs.isFingerprintEnabled()

    fun setFingerprintEnabled(enabled: Boolean) {
        prefs.setFingerprintEnabled(enabled)
    }

    fun logout() {
        prefs.setLastLoginTime(0L)
        _authState.value = AuthState.LOCKED
    }

    fun updateUsername(newName: String) {
        prefs.setUsername(newName)
        _username.value = newName
    }

    fun updatePin(newPin: String) {
        prefs.setPin(newPin)
    }
}

class SecurityViewModelFactory(private val prefs: SecurityPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecurityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SecurityViewModel(prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
