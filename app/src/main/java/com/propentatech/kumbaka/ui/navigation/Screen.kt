package com.propentatech.kumbaka.ui.navigation

/**
 * Définition des écrans de l'application
 * Utilisé pour la navigation entre les différentes vues
 */
sealed class Screen(val route: String) {
    // Écrans principaux avec navigation en bas
    object Home : Screen("home")
    object Tasks : Screen("tasks")
    object Notes : Screen("notes")
    object Events : Screen("events")
    object Settings : Screen("settings")
    
    // Écrans secondaires
    object TaskEditor : Screen("task_editor/{taskId}") {
        fun createRoute(taskId: String = "new") = "task_editor/$taskId"
    }
    object NoteEditor : Screen("note_editor/{noteId}") {
        fun createRoute(noteId: String = "new") = "note_editor/$noteId"
    }
    object EventEditor : Screen("event_editor/{eventId}") {
        fun createRoute(eventId: String = "new") = "event_editor/$eventId"
    }
    object Calendar : Screen("calendar")
}

/**
 * Items de la barre de navigation en bas
 */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: String // Nom de l'icône Material
)

/**
 * Liste des items de la navigation en bas
 */
val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Accueil", "home"),
    BottomNavItem(Screen.Tasks, "Tâches", "check_circle"),
    BottomNavItem(Screen.Notes, "Notes", "description"),
    BottomNavItem(Screen.Events, "Événements", "event"),
    BottomNavItem(Screen.Settings, "Paramètres", "settings")
)
