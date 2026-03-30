package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.propentatech.kumbaka.ui.components.PremiumTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlusScreen(
    securityViewModel: com.propentatech.kumbaka.ui.viewmodel.SecurityViewModel,
    onNavigateToNotes: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToLifestyle: () -> Unit,
    onNavigateToPlanning: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToAlarms: () -> Unit = {},
    onNavigateToStopwatch: () -> Unit = {},
    onNavigateToCalculator: () -> Unit = {}
) {
    val items = listOf(
        PlusItem("Habitudes", "Lifestyle & Mood", Icons.Default.Favorite, onNavigateToLifestyle),
        PlusItem("Planning", "Études & Révisions", Icons.Default.EventNote, onNavigateToPlanning),
        PlusItem("Alarmes", "Système & Musique", Icons.Default.NotificationsActive, onNavigateToAlarms),
        PlusItem("Chronomètre", "Temps & Tâches", Icons.Default.Timer, onNavigateToStopwatch),
        PlusItem("Calculatrice", "Dépenses & Budgets", Icons.Default.Calculate, onNavigateToCalculator),
        PlusItem("Projets", "Objectifs & Jalons", Icons.Default.RocketLaunch, onNavigateToPlanning),
        PlusItem("Profil", "Code PIN & Sécurité", Icons.Default.AccountCircle, onNavigateToSettings),
        PlusItem("Paramètres", "Compte & App", Icons.Default.Settings, onNavigateToSettings)
    )

    Scaffold(
        topBar = {
            PremiumTopAppBar(
                title = { Text("MyLive Plus", fontWeight = FontWeight.Black, color = Color.White, style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Modules Complémentaires",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B00),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items) { item ->
                    PlusCard(item)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Branding Footer
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("MyLive Assistant Omniscient v1.2", color = Color.Gray, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PlusCard(item: PlusItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { item.onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF6B00).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = Color(0xFFFF6B00), modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(item.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

data class PlusItem(val title: String, val subtitle: String, val icon: ImageVector, val onClick: () -> Unit)
