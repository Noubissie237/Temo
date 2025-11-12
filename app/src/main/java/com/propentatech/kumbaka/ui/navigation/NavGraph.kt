package com.propentatech.kumbaka.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.propentatech.kumbaka.ui.screens.*

/**
 * Graphe de navigation de l'application
 * Définit toutes les routes et transitions entre les écrans
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    paddingValues: PaddingValues = PaddingValues()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        // Écran d'accueil (Dashboard)
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTasks = { 
                    navController.navigate(Screen.Tasks.route) {
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToNotes = { 
                    navController.navigate(Screen.Notes.route) {
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToEvents = { 
                    navController.navigate(Screen.Events.route) {
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteEditor.createRoute(noteId))
                }
            )
        }

        // Écran des tâches
        composable(Screen.Tasks.route) {
            TasksScreen(
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskEditor.createRoute(taskId))
                },
                onCreateTask = {
                    navController.navigate(Screen.TaskEditor.createRoute("new"))
                }
            )
        }

        // Écran d'édition de tâche
        composable(
            route = Screen.TaskEditor.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskEditorScreen(
                taskId = if (taskId == "new") null else taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Écran des notes
        composable(Screen.Notes.route) {
            NotesScreen(
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteEditor.createRoute(noteId))
                },
                onCreateNote = {
                    navController.navigate(Screen.NoteEditor.createRoute("new"))
                }
            )
        }

        // Écran d'édition de note
        composable(
            route = Screen.NoteEditor.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteEditorScreen(
                noteId = if (noteId == "new") null else noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Écran des événements
        composable(Screen.Events.route) {
            EventsScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventEditor.createRoute(eventId))
                },
                onCreateEvent = {
                    navController.navigate(Screen.EventEditor.createRoute("new"))
                },
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route)
                }
            )
        }

        // Écran du calendrier
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onCreateEvent = {
                    navController.navigate(Screen.EventEditor.createRoute("new"))
                },
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventEditor.createRoute(eventId))
                }
            )
        }

        // Écran d'édition d'événement
        composable(
            route = Screen.EventEditor.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            EventEditorScreen(
                eventId = if (eventId == "new") null else eventId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Écran des paramètres
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
