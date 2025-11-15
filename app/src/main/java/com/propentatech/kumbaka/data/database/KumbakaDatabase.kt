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
    version = 7,
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
         * Migration de la version 5 à 6 : Rendre updatedAt nullable pour les tâches
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Créer une nouvelle table temporaire avec updatedAt nullable
                db.execSQL("""
                    CREATE TABLE tasks_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        type TEXT NOT NULL,
                        specificDate TEXT,
                        selectedDays TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        lastCompletedDate TEXT,
                        displayOrder INTEGER NOT NULL,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT
                    )
                """)
                
                // Copier les données, en mettant updatedAt à NULL si elle est égale à createdAt
                db.execSQL("""
                    INSERT INTO tasks_new 
                    SELECT id, title, description, type, specificDate, selectedDays, 
                           priority, isCompleted, lastCompletedDate, displayOrder, 
                           createdAt, 
                           CASE WHEN updatedAt = createdAt THEN NULL ELSE updatedAt END
                    FROM tasks
                """)
                
                // Supprimer l'ancienne table
                db.execSQL("DROP TABLE tasks")
                
                // Renommer la nouvelle table
                db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
            }
        }
        
        /**
         * Migration de la version 6 à 7 : Rendre updatedAt nullable pour les notes
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Créer une nouvelle table temporaire avec updatedAt nullable
                db.execSQL("""
                    CREATE TABLE notes_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        links TEXT NOT NULL,
                        displayOrder INTEGER NOT NULL,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT
                    )
                """)
                
                // Copier les données, en mettant updatedAt à NULL si elle est égale à createdAt
                db.execSQL("""
                    INSERT INTO notes_new 
                    SELECT id, title, content, links, displayOrder, 
                           createdAt, 
                           CASE WHEN updatedAt = createdAt THEN NULL ELSE updatedAt END
                    FROM notes
                """)
                
                // Supprimer l'ancienne table
                db.execSQL("DROP TABLE notes")
                
                // Renommer la nouvelle table
                db.execSQL("ALTER TABLE notes_new RENAME TO notes")
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
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigration() // Pour le développement
                .build()
        }
    }
}
