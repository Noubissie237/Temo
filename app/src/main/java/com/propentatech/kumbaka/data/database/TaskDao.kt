package com.propentatech.kumbaka.data.database

import androidx.room.*
import com.propentatech.kumbaka.data.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object pour les tâches
 * Définit les opérations de base de données pour les tâches
 */
@Dao
interface TaskDao {
    
    /**
     * Récupère toutes les tâches
     * Retourne un Flow pour observer les changements en temps réel
     */
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>
    
    /**
     * Récupère une tâche par son ID
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): Task?
    
    /**
     * Insère une nouvelle tâche
     * Remplace si elle existe déjà
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)
    
    /**
     * Insère plusieurs tâches
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)
    
    /**
     * Met à jour une tâche existante
     */
    @Update
    suspend fun updateTask(task: Task)
    
    /**
     * Supprime une tâche
     */
    @Delete
    suspend fun deleteTask(task: Task)
    
    /**
     * Supprime une tâche par son ID
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
    
    /**
     * Supprime toutes les tâches
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    
    /**
     * Compte le nombre total de tâches
     */
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int
}
