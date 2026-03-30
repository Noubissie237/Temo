package com.propentatech.kumbaka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.ui.components.PremiumTopAppBar
import com.propentatech.kumbaka.ui.viewmodel.StopwatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen(
    viewModel: StopwatchViewModel = viewModel(),
    onBack: () -> Unit
) {
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val laps by viewModel.laps.collectAsState()

    Scaffold(
        topBar = {
            PremiumTopAppBar(
                title = { Text("Chronomètre", color = Color.White, fontWeight = FontWeight.Bold) },
                onNavigateBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Grand Affichage du Temps
            Text(
                text = viewModel.formatTime(elapsedTime),
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                color = if (isRunning) Color(0xFFFF6B00) else Color.White,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Boutons de Contrôle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bouton Reset
                IconButton(
                    onClick = { viewModel.reset() },
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                }

                // Bouton Play/Pause Principal
                Button(
                    onClick = { viewModel.toggle() },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) Color.Red.copy(alpha = 0.8f) else Color(0xFFFF6B00)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }

                // Bouton Lap
                IconButton(
                    onClick = { viewModel.lap() },
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Timer, contentDescription = "Lap", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Liste des Tours (Laps)
            if (laps.isNotEmpty()) {
                Text(
                    "Tours",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFF6B00),
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(laps.reversed()) { index, lapTime ->
                        LapItem(laps.size - index, viewModel.formatTime(lapTime))
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun LapItem(index: Int, time: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Tour $index", fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(time, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}
