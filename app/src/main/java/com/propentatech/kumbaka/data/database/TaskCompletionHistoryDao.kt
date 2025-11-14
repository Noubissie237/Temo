package com.propentatech.kumbaka.data.database

import androidx.room.*
import com.propentatech.kumbaka.data.model.TaskCompletionHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * DAO pour l'historique des complétions de tâches
 */
@Dao
interface TaskCompletionHistoryDao {
    
    /**
     * Insère un enregistrement de complétion
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: TaskCompletionHistory)
    
    /**
     * Supprime un enregistrement de complétion
     */
    @Delete
    suspend fun delete(history: TaskCompletionHistory)
    
    /**
     * Supprime tous les enregistrements d'une tâche pour une date donnée
     */
    @Query("DELETE FROM task_completion_history WHERE taskId = :taskId AND completionDate = :date")
    suspend fun deleteByTaskAndDate(taskId: String, date: LocalDate)
    
    /**
     * Récupère tous les enregistrements de complétion d'une tâche
     */
    @Query("SELECT * FROM task_completion_history WHERE taskId = :taskId ORDER BY completionDate DESC")
    fun getHistoryForTask(taskId: String): Flow<List<TaskCompletionHistory>>
    
    /**
     * Récupère les complétions pour une date spécifique
     */
    @Query("SELECT * FROM task_completion_history WHERE completionDate = :date")
    fun getCompletionsForDate(date: LocalDate): Flow<List<TaskCompletionHistory>>
    
    /**
     * Récupère les complétions entre deux dates
     */
    @Query("SELECT * FROM task_completion_history WHERE completionDate BETWEEN :startDate AND :endDate ORDER BY completionDate DESC")
    fun getCompletionsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<TaskCompletionHistory>>
    
    /**
     * Vérifie si une tâche a été complétée à une date donnée
     */
    @Query("SELECT EXISTS(SELECT 1 FROM task_completion_history WHERE taskId = :taskId AND completionDate = :date)")
    suspend fun isTaskCompletedOnDate(taskId: String, date: LocalDate): Boolean
    
    /**
     * Supprime les enregistrements plus anciens qu'une certaine date (nettoyage)
     */
    @Query("DELETE FROM task_completion_history WHERE completionDate < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: LocalDate)
    
    /**
     * Supprime tous les enregistrements d'une tâche (quand la tâche est supprimée)
     */
    @Query("DELETE FROM task_completion_history WHERE taskId = :taskId")
    suspend fun deleteAllForTask(taskId: String)
}
