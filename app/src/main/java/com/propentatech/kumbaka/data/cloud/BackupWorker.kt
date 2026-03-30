package com.propentatech.kumbaka.data.cloud

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.propentatech.kumbaka.KumbakaApplication
import java.io.File

class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as KumbakaApplication
        val cloudPrefs = application.cloudPreferences
        val driveManager = application.googleDriveManager

        if (!cloudPrefs.isCloudBackupEnabled()) {
            return Result.success()
        }

        return try {
            // Chemin de la base de données Room
            val dbFile = applicationContext.getDatabasePath("kumbaka_database")
            if (dbFile.exists()) {
                val uploadResult = driveManager.uploadBackup(dbFile)
                if (uploadResult.isSuccess) {
                    cloudPrefs.setLastBackupTime(System.currentTimeMillis())
                    Result.success()
                } else {
                    Result.retry()
                }
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
