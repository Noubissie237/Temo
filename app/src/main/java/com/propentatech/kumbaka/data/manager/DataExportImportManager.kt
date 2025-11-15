package com.propentatech.kumbaka.data.manager

import android.content.Context
import com.propentatech.kumbaka.data.model.Event
import com.propentatech.kumbaka.data.model.Note
import com.propentatech.kumbaka.data.model.Task
import com.propentatech.kumbaka.data.model.TaskCompletionHistory
import com.propentatech.kumbaka.data.repository.EventRepository
import com.propentatech.kumbaka.data.repository.NoteRepository
import com.propentatech.kumbaka.data.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Données à exporter/importer
 */
data class ExportData(
    val includeTasks: Boolean = false,
    val includeNotes: Boolean = false,
    val includeEvents: Boolean = false
)

/**
 * Structure du fichier JSON d'export
 */
@Serializable
data class BackupData(
    val version: String,
    val exportDate: String,
    val tasks: List<Task>? = null,
    val notes: List<Note>? = null,
    val events: List<Event>? = null,
    val taskCompletionHistory: List<TaskCompletionHistory>? = null
)

/**
 * Gestionnaire d'export et d'import des données
 */
class DataExportImportManager(
    private val context: Context,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository,
    private val eventRepository: EventRepository
) {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Exporter les données sélectionnées vers un OutputStream
     */
    suspend fun exportData(
        outputStream: OutputStream,
        exportData: ExportData
    ): Result<String> {
        return try {
            val tasks = if (exportData.includeTasks) {
                taskRepository.getAllTasks().first()
            } else null
            
            val notes = if (exportData.includeNotes) {
                noteRepository.getAllNotes().first()
            } else null
            
            val events = if (exportData.includeEvents) {
                eventRepository.getAllEvents().first()
            } else null
            
            // Exporter l'historique des tâches si les tâches sont exportées
            val taskHistory = if (exportData.includeTasks) {
                taskRepository.getAllTaskCompletionHistory().first()
            } else null
            
            val backupData = BackupData(
                version = "1.0.0",
                exportDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                tasks = tasks,
                notes = notes,
                events = events,
                taskCompletionHistory = taskHistory
            )
            
            val jsonString = json.encodeToString(backupData)
            outputStream.write(jsonString.toByteArray())
            outputStream.close()
            
            val itemCount = (tasks?.size ?: 0) + (notes?.size ?: 0) + (events?.size ?: 0)
            Result.success("$itemCount élément(s) exporté(s) avec succès")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Importer les données depuis un InputStream
     */
    suspend fun importData(
        inputStream: InputStream,
        importData: ExportData
    ): Result<String> {
        return try {
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val backupData = json.decodeFromString<BackupData>(jsonString)
            
            var importedCount = 0
            
            // Importer les tâches si sélectionnées
            if (importData.includeTasks && backupData.tasks != null) {
                backupData.tasks.forEach { task ->
                    taskRepository.addTask(task)
                    importedCount++
                }
            }
            
            // Importer les notes si sélectionnées
            if (importData.includeNotes && backupData.notes != null) {
                backupData.notes.forEach { note ->
                    noteRepository.addNote(note)
                    importedCount++
                }
            }
            
            // Importer les événements si sélectionnés
            if (importData.includeEvents && backupData.events != null) {
                backupData.events.forEach { event ->
                    eventRepository.addEvent(event)
                    importedCount++
                }
            }
            
            // Importer l'historique des tâches si les tâches sont importées
            if (importData.includeTasks && backupData.taskCompletionHistory != null) {
                backupData.taskCompletionHistory.forEach { history ->
                    taskRepository.addTaskCompletionHistory(history)
                }
            }
            
            Result.success("$importedCount élément(s) importé(s) avec succès")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Analyser un fichier d'import pour voir ce qu'il contient
     */
    suspend fun analyzeImportFile(inputStream: InputStream): BackupData? {
        return try {
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            json.decodeFromString<BackupData>(jsonString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Générer un nom de fichier pour l'export
     */
    fun generateExportFileName(): String {
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
        return "temo_backup_$date.json"
    }
}
