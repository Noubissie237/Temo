package com.propentatech.kumbaka.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.propentatech.kumbaka.ui.screens.*
import com.propentatech.kumbaka.data.preferences.OnboardingPreferences
import kotlinx.coroutines.launch

/**
 * Graphe de navigation de l'application
 * Définit toutes les routes et transitions entre les écrans
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    securityViewModel: com.propentatech.kumbaka.ui.viewmodel.SecurityViewModel,
    themeViewModel: com.propentatech.kumbaka.ui.viewmodel.ThemeViewModel,
    startDestination: String = Screen.Home.route,
    paddingValues: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val onboardingPreferences = OnboardingPreferences(context)
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        // Écran d'onboarding strict
        composable(Screen.Onboarding.route) {
            PermissionsOnboardingScreen(
                onPermissionsGranted = {
                    coroutineScope.launch {
                        onboardingPreferences.setOnboardingCompleted()
                    }
                    navController.navigate(Screen.AuthSetup.route) {
                        popUpTo(Screen.Onboarding.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Écran de configuration initiale de la sécurité
        composable(Screen.AuthSetup.route) {
            AuthSetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.AuthSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // Écran de connexion PIN / Empreinte
        composable(Screen.Login.route) {
            LoginScreen(
                onAuthenticated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Écran d'accueil (Dashboard)
        composable(Screen.Home.route) {
            HomeScreen(
                securityViewModel = securityViewModel,
                themeViewModel = themeViewModel,
                onNavigateToTasks = { 
                    navController.navigate(Screen.Tasks.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToNotes = { 
                    navController.navigate(Screen.Notes.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToEvents = { 
                    navController.navigate(Screen.Events.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToFinance = {
                    navController.navigate(Screen.Finance.route)
                },
                onNavigateToReports = {
                    navController.navigate(Screen.Reports.route)
                },
                onNavigateToPlus = {
                    navController.navigate(Screen.Plus.route)
                },
                onNavigateToAlarms = {
                    navController.navigate(Screen.Alarms.route)
                },
                onNavigateToStopwatch = {
                    navController.navigate(Screen.Stopwatch.route)
                },
                onTaskClick = { taskId -> navController.navigate(Screen.TaskDetail.createRoute(taskId)) },
                onEventClick = { eventId -> navController.navigate(Screen.EventDetail.createRoute(eventId)) },
                onNoteClick = { noteId -> navController.navigate(Screen.NoteDetail.createRoute(noteId)) }
            )
        }

        // Écran des tâches
        composable(Screen.Tasks.route) {
            TasksScreen(
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onCreateTask = {
                    navController.navigate(Screen.TaskEditor.createRoute("new"))
                },
                onViewAllTasks = {
                    navController.navigate(Screen.AllTasks.route)
                }
            )
        }
        
        // Écran de toutes les tâches
        composable(Screen.AllTasks.route) {
            AllTasksScreen(
                onNavigateBack = { navController.popBackStack() },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
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

        // Écran de détails de tâche
        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.TaskEditor.createRoute(id)) },
                onDelete = { navController.popBackStack() }
            )
        }

        // Écran des notes
        composable(Screen.Notes.route) {
            NotesScreen(
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
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

        // Écran de détails de note
        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            NoteDetailScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.NoteEditor.createRoute(id)) },
                onDelete = { navController.popBackStack() }
            )
        }

        // Écran des événements
        composable(Screen.Events.route) {
            EventsScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
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
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
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

        // Écran de détails d'événement
        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDetailScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.EventEditor.createRoute(id)) },
                onDelete = { navController.popBackStack() }
            )
        }

        // Écran des paramètres
        composable(Screen.Settings.route) {
            SettingsScreen(
                securityViewModel = securityViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                }
            )
        }
        
        // Écran des statistiques
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // ===== MODULE FINANCE (Omniscient) =====
        composable(Screen.Finance.route) {
            val application = LocalContext.current.applicationContext as com.propentatech.kumbaka.KumbakaApplication
            val financeViewModel: com.propentatech.kumbaka.ui.viewmodel.FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return com.propentatech.kumbaka.ui.viewmodel.FinanceViewModel(application.financeRepository) as T
                    }
                }
            )
            FinanceScreen(
                viewModel = financeViewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddTransactionClick = {}
            )
        }
        
        // ===== MODULE RAPPORTS AVANCÉS =====
        composable(Screen.Reports.route) {
            val application = LocalContext.current.applicationContext as com.propentatech.kumbaka.KumbakaApplication
            val financeViewModel: com.propentatech.kumbaka.ui.viewmodel.FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return com.propentatech.kumbaka.ui.viewmodel.FinanceViewModel(application.financeRepository) as T
                    }
                }
            )
            ReportsScreen(
                viewModel = financeViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ===== MODULE PROJETS (Premium) =====
        composable(Screen.Projects.route) {
            val application = LocalContext.current.applicationContext as com.propentatech.kumbaka.KumbakaApplication
            val projectViewModel: com.propentatech.kumbaka.ui.viewmodel.ProjectViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return com.propentatech.kumbaka.ui.viewmodel.ProjectViewModel(application.projectRepository) as T
                    }
                }
            )
            ProjectsScreen(
                viewModel = projectViewModel,
                onNavigateToProjectDetail = { /* Direct to detail if needed */ }
            )
        }

        // ===== MODULE LIFESTYLE & HUMEUR =====
        composable(Screen.Lifestyle.route) {
            val application = LocalContext.current.applicationContext as com.propentatech.kumbaka.KumbakaApplication
            val lifestyleViewModel: com.propentatech.kumbaka.ui.viewmodel.LifestyleViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return com.propentatech.kumbaka.ui.viewmodel.LifestyleViewModel(application.lifestyleRepository) as T
                    }
                }
            )
            LifestyleScreen(
                viewModel = lifestyleViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ===== MODULE PLANNING =====
        composable(Screen.Planning.route) {
            PlanningScreen(
                onCreateSession = { navController.navigate(Screen.PlanningEditor.createRoute("new")) },
                onEditSession = { id -> navController.navigate(Screen.PlanningEditor.createRoute(id)) }
            )
        }

        composable(
            route = Screen.PlanningEditor.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")?.let { if (it == "new") null else it }
            PlanningEditorScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ===== ÉCRAN PLUS (Menu Secondaire MyLive) =====
        composable(Screen.Plus.route) {
            PlusScreen(
                securityViewModel = securityViewModel,
                onNavigateToNotes = { navController.navigate(Screen.Notes.route) },
                onNavigateToEvents = { navController.navigate(Screen.Events.route) },
                onNavigateToLifestyle = { navController.navigate(Screen.Lifestyle.route) },
                onNavigateToPlanning = { navController.navigate(Screen.Planning.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToAlarms = { navController.navigate(Screen.Alarms.route) },
                onNavigateToStopwatch = { navController.navigate(Screen.Stopwatch.route) },
                onNavigateToCalculator = { navController.navigate(Screen.Calculator.route) }
            )
        }

        // ===== ÉCRAN CALCULATRICE =====
        composable(Screen.Calculator.route) {
            CalculatorScreen(onBack = { navController.popBackStack() })
        }

        // ===== ÉCRAN ALARMES =====
        composable(Screen.Alarms.route) {
            AlarmScreen(onBack = { navController.popBackStack() })
        }

        // ===== ÉCRAN CHRONOMÈTRE =====
        composable(Screen.Stopwatch.route) {
            StopwatchScreen(onBack = { navController.popBackStack() })
        }
    }
}
