package com.propentatech.kumbaka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.propentatech.kumbaka.ui.components.BottomNavBar
import com.propentatech.kumbaka.ui.navigation.NavGraph
import com.propentatech.kumbaka.ui.navigation.Screen
import com.propentatech.kumbaka.ui.theme.KumbakaTheme

/**
 * Activité principale de l'application Kumbaka
 * Gère la navigation et l'affichage de la barre de navigation en bas
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KumbakaTheme {
                KumbakaApp()
            }
        }
    }
}

/**
 * Composable principal de l'application
 * Configure la navigation et la barre de navigation en bas
 */
@Composable
fun KumbakaApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Liste des routes qui affichent la barre de navigation en bas
    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.Tasks.route,
        Screen.Notes.route,
        Screen.Events.route,
        Screen.Settings.route
    )

    // Afficher la barre de navigation seulement sur les écrans principaux
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            startDestination = Screen.Home.route,
            paddingValues = paddingValues
        )
    }
}