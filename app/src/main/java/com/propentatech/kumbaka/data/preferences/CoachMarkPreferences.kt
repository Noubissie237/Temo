package com.propentatech.kumbaka.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Gestion des préférences pour les coach marks
 * Permet de suivre quels coach marks ont été vus par l'utilisateur
 */
private val Context.coachMarkDataStore: DataStore<Preferences> by preferencesDataStore(name = "coach_mark_preferences")

class CoachMarkPreferences(private val context: Context) {
    
    companion object {
        private val EVENTS_HISTORY_COACH_MARK_SEEN = booleanPreferencesKey("events_history_coach_mark_seen")
    }
    
    /**
     * Vérifie si le coach mark de l'historique des événements a été vu
     */
    val isEventsHistoryCoachMarkSeen: Flow<Boolean> = context.coachMarkDataStore.data
        .map { preferences ->
            preferences[EVENTS_HISTORY_COACH_MARK_SEEN] ?: false
        }
    
    /**
     * Marque le coach mark de l'historique des événements comme vu
     */
    suspend fun setEventsHistoryCoachMarkSeen() {
        context.coachMarkDataStore.edit { preferences ->
            preferences[EVENTS_HISTORY_COACH_MARK_SEEN] = true
        }
    }
    
    /**
     * Réinitialise le coach mark de l'historique (pour les tests)
     */
    suspend fun resetEventsHistoryCoachMark() {
        context.coachMarkDataStore.edit { preferences ->
            preferences[EVENTS_HISTORY_COACH_MARK_SEEN] = false
        }
    }
}
