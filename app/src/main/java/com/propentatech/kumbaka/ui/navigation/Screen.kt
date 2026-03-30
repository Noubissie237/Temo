package com.propentatech.kumbaka.ui.navigation

/**
 * Définition des écrans de l'application
 * Utilisé pour la navigation entre les différentes vues
 */
sealed class Screen(val route: String) {
    // Écran d'onboarding
    object Onboarding : Screen("onboarding")
    object AuthSetup : Screen("auth_setup")
    object Login : Screen("login")
    
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
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object NoteEditor : Screen("note_editor/{noteId}") {
        fun createRoute(noteId: String = "new") = "note_editor/$noteId"
    }
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: String) = "note_detail/$noteId"
    }
    object EventEditor : Screen("event_editor/{eventId}") {
        fun createRoute(eventId: String = "new") = "event_editor/$eventId"
    }
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object Calendar : Screen("calendar")
    object Statistics : Screen("statistics")
    object AllTasks : Screen("all_tasks")
    object Finance : Screen("finance")
    object Reports : Screen("reports")
    object Projects : Screen("projects")
    object Lifestyle : Screen("lifestyle")
    object Planning : Screen("planning")
    object PlanningEditor : Screen("planning_editor/{sessionId}") {
        fun createRoute(sessionId: String = "new") = "planning_editor/$sessionId"
    }
    object Plus : Screen("plus")
    object Alarms : Screen("alarms")
    object Stopwatch : Screen("stopwatch")
    object Calculator : Screen("calculator")
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
 * Liste des items de la navigation en bas (MyLive structure)
 */
val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", "home"),
    BottomNavItem(Screen.Notes, "Notes", "edit"),
    BottomNavItem(Screen.Calendar, "Calendar", "calendar_month"),
    BottomNavItem(Screen.Finance, "Finance", "account_balance"),
    BottomNavItem(Screen.Events, "Events", "event"),
    BottomNavItem(Screen.Plus, "Plus", "more_horiz")
)
