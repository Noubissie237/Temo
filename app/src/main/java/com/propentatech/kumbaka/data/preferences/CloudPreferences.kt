package com.propentatech.kumbaka.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CloudPreferences(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "cloud_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isCloudBackupEnabled(): Boolean = prefs.getBoolean("cloud_backup_enabled", false)
    
    fun setCloudBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("cloud_backup_enabled", enabled).apply()
    }

    fun getLastBackupTime(): Long = prefs.getLong("last_backup_time", 0L)
    
    fun setLastBackupTime(time: Long) {
        prefs.edit().putLong("last_backup_time", time).apply()
    }

    fun getGoogleAccountName(): String? = prefs.getString("google_account_name", null)
    
    fun setGoogleAccountName(name: String?) {
        prefs.edit().putString("google_account_name", name).apply()
    }

    fun getGoogleWebClientId(): String? = prefs.getString("google_web_client_id", null)
    
    fun setGoogleWebClientId(id: String?) {
        prefs.edit().putString("google_web_client_id", id).apply()
    }
}
