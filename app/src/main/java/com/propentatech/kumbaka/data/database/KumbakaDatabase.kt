package com.propentatech.kumbaka.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.propentatech.kumbaka.data.model.Event
import com.propentatech.kumbaka.data.model.Note
import com.propentatech.kumbaka.data.model.Task
import com.propentatech.kumbaka.data.model.TaskCompletionHistory

/**
 * Base de données Room pour l'application Kumbaka
 * Contient les tables pour les tâches, événements et notes
 */
@Database(
    entities = [Task::class, Event::class, Note::class, TaskCompletionHistory::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KumbakaDatabase : RoomDatabase() {
    
    /**
     * DAO pour les tâches
     */
    abstract fun taskDao(): TaskDao
    
    /**
     * DAO pour les événements
     */
    abstract fun eventDao(): EventDao
    
    /**
     * DAO pour les notes
     */
    abstract fun noteDao(): NoteDao
    
    /**
     * DAO pour l'historique des complétions de tâches
     */
    abstract fun taskCompletionHistoryDao(): TaskCompletionHistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: KumbakaDatabase? = null
        
        /**
         * Récupère l'instance singleton de la base de données
         */
        fun getInstance(context: Context): KumbakaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        /**
         * Construit la base de données
         */
        private fun buildDatabase(context: Context): KumbakaDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                KumbakaDatabase::class.java,
                "kumbaka_database"
            )
                .fallbackToDestructiveMigration() // Pour le développement
                .build()
        }
    }
}
