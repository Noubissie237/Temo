package com.propentatech.kumbaka.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PremiumTopAppBar(
    title: @Composable () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212))
                    )
                )
        ) {
            Column {
                // Espace pour la status bar si le content padding l'exige
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    Box(modifier = Modifier.weight(1f)) {
                        title()
                    }
                    
                    actions()
                }
            }
            
            // Asymmetric Joyful Accent (Bandeau Vibrant)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(100.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color(0xFFFF6B00))
                        )
                    )
            )
        }
    }
}
