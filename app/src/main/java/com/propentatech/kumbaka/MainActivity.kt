package com.propentatech.kumbaka

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.propentatech.kumbaka.data.preferences.OnboardingPreferences
import com.propentatech.kumbaka.data.preferences.ThemePreferences
import com.propentatech.kumbaka.ui.navigation.NavGraph
import com.propentatech.kumbaka.ui.navigation.Screen
import com.propentatech.kumbaka.ui.theme.MyLiveTheme
import com.propentatech.kumbaka.ui.viewmodel.ThemeViewModel
import com.propentatech.kumbaka.ui.viewmodel.ThemeViewModelFactory
import kotlinx.coroutines.launch

/**
 * Activité principale de l'application MyLive
 * Gère la navigation et l'affichage de la barre de navigation en bas
 */
class MainActivity : ComponentActivity() {
    private lateinit var securityViewModel: com.propentatech.kumbaka.ui.viewmodel.SecurityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val application = applicationContext as KumbakaApplication
        securityViewModel = com.propentatech.kumbaka.ui.viewmodel.SecurityViewModelFactory(application.securityPreferences)
            .create(com.propentatech.kumbaka.ui.viewmodel.SecurityViewModel::class.java)

        setContent {
            MyLiveApp(securityViewModel)
        }
    }

    override fun onStop() {
        super.onStop()
        securityViewModel.lock()
    }
}

/**
 * Composable principal de l'application
 * Configure la navigation et la barre de navigation en bas
 */
@Composable
fun MyLiveApp(securityViewModel: com.propentatech.kumbaka.ui.viewmodel.SecurityViewModel) {
    // Récupérer le ThemeViewModel
    val context = LocalContext.current
    val application = context.applicationContext as KumbakaApplication
    val themeViewModel: ThemeViewModel = viewModel(
        factory = ThemeViewModelFactory(application.themePreferences)
    )
    
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    
    // Demander la permission de notification au démarrage (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                scope.launch {
                    application.eventRepository.rescheduleAllNotifications()
                }
            }
        }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    // Observer l'état du mode sombre
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    
    // Observer l'état de l'authentification
    val authState by securityViewModel.authState.collectAsState()
    
    // Vérifier si l'onboarding a été complété
    val onboardingPreferences = remember { OnboardingPreferences(context) }
    val isOnboardingCompleted by onboardingPreferences.isOnboardingCompleted.collectAsState(initial = null)
    
    // Déterminer la destination de départ complexe
    val startDestination = when {
        isOnboardingCompleted == false -> Screen.Onboarding.route
        isOnboardingCompleted == true -> {
            when (authState) {
                com.propentatech.kumbaka.ui.viewmodel.AuthState.SETUP_REQUIRED -> Screen.AuthSetup.route
                com.propentatech.kumbaka.ui.viewmodel.AuthState.LOCKED -> Screen.Login.route
                com.propentatech.kumbaka.ui.viewmodel.AuthState.AUTHENTICATED -> Screen.Home.route
                else -> null
            }
        }
        else -> null 
    }
    
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
 
    // Afficher la barre de navigation si authentifié
    // On la cache explicitement sur l'Onboarding et le Login
    // Pour tout le reste, on la FORCE pour éviter les disparitions
    // Afficher la barre de navigation sur les écrans principaux
    // Afficher la barre de navigation sur les écrans principaux (Liste d'inclusion)
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Notes.route,
        Screen.Calendar.route,
        Screen.Finance.route,
        Screen.Events.route,
        Screen.Plus.route,
        Screen.Alarms.route,
        Screen.Stopwatch.route,
        Screen.Calculator.route,
        Screen.Tasks.route,
        Screen.Settings.route
    )
    
    // Log de diagnostic pour le débogage (Android Log)
    android.util.Log.d("MyLiveUI", "Route: $currentRoute | Auth: $authState | ShowBar: $showBottomBar")

    MyLiveTheme(darkTheme = isDarkMode) {
        startDestination?.let { destination ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Le contenu principal (NavHost)
                NavGraph(
                    navController = navController,
                    securityViewModel = securityViewModel,
                    themeViewModel = themeViewModel,
                    startDestination = destination,
                    // On garde un padding en bas pour ne pas que le contenu soit SOUS la barre si elle est affichée
                    paddingValues = if (showBottomBar) PaddingValues(bottom = 80.dp) else PaddingValues(0.dp)
                )

                // Barre de navigation forcée en superposition
                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        // Design Premium MyLive : Arrondi, Orange & Noir
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 12.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        ) {
                            com.propentatech.kumbaka.ui.navigation.bottomNavItems.forEach { item ->
                                val isSelected = currentRoute == item.screen.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        if (currentRoute != item.screen.route) {
                                            navController.navigate(item.screen.route) {
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        val icon = when (item.icon) {
                                            "home" -> Icons.Default.Home
                                            "edit" -> Icons.Default.Edit
                                            "calendar_month" -> Icons.Default.CalendarMonth
                                            "account_balance" -> Icons.Default.AccountBalance
                                            "event" -> Icons.Default.Event
                                            "more_horiz" -> Icons.Default.MoreHoriz
                                            else -> Icons.Default.HelpCenter
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                                    alwaysShowLabel = false,
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
                }
            }
        }
    }
}