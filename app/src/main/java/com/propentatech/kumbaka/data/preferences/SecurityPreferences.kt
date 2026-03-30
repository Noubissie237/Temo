package com.propentatech.kumbaka.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestionnaire de stockage sécurisé pour les identifiants utilisateur (PIN, Nome)
 */
class SecurityPreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "security_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun setUsername(username: String) {
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(): String? = prefs.getString(KEY_PIN, null)

    fun isSecurityEnabled(): Boolean {
        return !getPin().isNullOrEmpty()
    }

    fun setLastLoginTime(time: Long) {
        prefs.edit().putLong(KEY_LAST_LOGIN, time).apply()
    }

    fun getLastLoginTime(): Long = prefs.getLong(KEY_LAST_LOGIN, 0L)

    fun setFingerprintEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FINGERPRINT_ENABLED, enabled).apply()
    }

    fun isFingerprintEnabled(): Boolean = prefs.getBoolean(KEY_FINGERPRINT_ENABLED, false)

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_PIN = "pin"
        private const val KEY_LAST_LOGIN = "last_login"
        private const val KEY_FINGERPRINT_ENABLED = "fingerprint_enabled"
    }
}
