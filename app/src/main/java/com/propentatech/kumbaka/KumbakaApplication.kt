package com.propentatech.kumbaka

import android.app.Application
import com.propentatech.kumbaka.data.MockData
import com.propentatech.kumbaka.data.database.KumbakaDatabase
import com.propentatech.kumbaka.data.repository.EventRepository
import com.propentatech.kumbaka.data.repository.NoteRepository
import com.propentatech.kumbaka.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Classe Application personnalisée pour Kumbaka
 * Initialise la base de données et charge les données mock au premier lancement
 */
class KumbakaApplication : Application() {
    
    // Scope pour les opérations asynchrones
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Base de données
    val database by lazy { KumbakaDatabase.getInstance(this) }
    
    // Repositories
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val eventRepository by lazy { EventRepository(database.eventDao()) }
    val noteRepository by lazy { NoteRepository(database.noteDao()) }

    override fun onCreate() {
        super.onCreate()
        
        // La base de données est maintenant vide au démarrage
        // Les utilisateurs créeront leurs propres tâches
    }
}
