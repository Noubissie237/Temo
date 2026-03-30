package com.propentatech.kumbaka.data.cloud

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class GoogleDriveManager(private val context: Context) {
    private val driveScopes = listOf(DriveScopes.DRIVE_FILE)

    /**
     * Retourne l'intention de connexion Google
     * @param webClientId Optionnel, permet de forcer l'identifiant client si google-services.json est absent
     */
    fun getSignInIntent(webClientId: String? = null): Intent {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE))
        
        if (!webClientId.isNullOrBlank()) {
            builder.requestIdToken(webClientId)
        }
            
        val gso = builder.build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    /**
     * Récupère le service Drive pour un compte donné
     */
    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(context, driveScopes)
        credential.selectedAccount = account.account
        
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("MyLive").build()
    }

    /**
     * Recherche ou crée le dossier "MyLive_Backups"
     */
    private suspend fun getOrCreateBackupFolder(service: Drive): String = withContext(Dispatchers.IO) {
        val query = "name = 'MyLive_Backups' and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
        val result = service.files().list().setQ(query).setSpaces("drive").execute()
        
        val folderId = if (result.files.isNotEmpty()) {
            result.files[0].id
        } else {
            val folderMetadata = com.google.api.services.drive.model.File().apply {
                name = "MyLive_Backups"
                mimeType = "application/vnd.google-apps.folder"
            }
            service.files().create(folderMetadata).setFields("id").execute().id
        }
        folderId
    }

    /**
     * Télécharge un fichier vers Google Drive
     */
    suspend fun uploadBackup(localFile: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: return@withContext Result.failure(Exception("Non connecté à Google"))
            
            val service = getDriveService(account)
            val folderId = getOrCreateBackupFolder(service)
            
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = "mylive_backup_${System.currentTimeMillis()}.db"
                parents = listOf(folderId)
            }
            
            val mediaContent = FileContent("application/x-sqlite3", localFile)
            val uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
            
            Result.success(uploadedFile.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
