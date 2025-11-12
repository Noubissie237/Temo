package com.propentatech.kumbaka.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.propentatech.kumbaka.ui.navigation.Screen

/**
 * Barre de navigation en bas de l'écran
 * Permet de naviguer entre les écrans principaux de l'application
 */
@Composable
fun BottomNavBar(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Liste des items de navigation
    val items = listOf(
        BottomNavItem(Screen.Home, "Accueil", Icons.Default.Home),
        BottomNavItem(Screen.Tasks, "Tâches", Icons.Default.CheckCircle),
        BottomNavItem(Screen.Notes, "Notes", Icons.Default.Edit),
        BottomNavItem(Screen.Events, "Événements", Icons.Default.DateRange),
        BottomNavItem(Screen.Settings, "Paramètres", Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            // Pop jusqu'à la destination de départ (Home)
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            // Éviter les doublons
                            launchSingleTop = true
                            // Restaurer l'état
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * Item de la barre de navigation
 */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)
