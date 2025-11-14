package com.propentatech.kumbaka.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 5,
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
         * Migration de la version 4 à 5 : Ajout de la colonne displayOrder
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ajouter displayOrder aux tâches
                db.execSQL("ALTER TABLE tasks ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0")
                
                // Ajouter displayOrder aux notes
                db.execSQL("ALTER TABLE notes ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0")
                
                // Ajouter displayOrder aux événements
                db.execSQL("ALTER TABLE events ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0")
            }
        }
        
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
                .addMigrations(MIGRATION_4_5)
                .fallbackToDestructiveMigration() // Pour le développement
                .build()
        }
    }
}
