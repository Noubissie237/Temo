package com.propentatech.kumbaka.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.propentatech.kumbaka.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Données pour une page d'onboarding
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector? = null,
    val drawableRes: Int? = null,
    val gradient: List<Color>
)

/**
 * Écran d'onboarding pour présenter l'application
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    
    val pages = listOf(
        OnboardingPage(
            title = "Bienvenue sur Temo",
            description = "Votre assistant personnel pour organiser vos tâches, notes et événements en toute simplicité.",
            drawableRes = R.drawable.logo,
            gradient = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary
            )
        ),
        OnboardingPage(
            title = "Gérez vos tâches",
            description = "Créez des tâches quotidiennes, périodiques ou occasionnelles. Suivez votre progression et ne manquez plus rien.",
            icon = Icons.Default.TaskAlt,
            gradient = listOf(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary
            )
        ),
        OnboardingPage(
            title = "Prenez des notes",
            description = "Capturez vos idées rapidement avec des notes enrichies de liens.",
            icon = Icons.Default.Edit,
            gradient = listOf(
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.primary
            )
        ),
        OnboardingPage(
            title = "Planifiez vos événements",
            description = "Créez et suivez vos événements importants avec un compte à rebours et des rappels.",
            icon = Icons.Default.DateRange,
            gradient = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.tertiary
            )
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Pager avec les pages d'onboarding
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }
            
            // Indicateurs et boutons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Indicateurs de page
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    activeColor = MaterialTheme.colorScheme.primary,
                    inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    indicatorWidth = 12.dp,
                    indicatorHeight = 12.dp,
                    spacing = 8.dp
                )
                
                // Boutons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bouton Passer
                    if (pagerState.currentPage < pages.size - 1) {
                        TextButton(
                            onClick = onFinish,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        ) {
                            Text("Passer", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    
                    // Bouton Suivant / Commencer
                    Button(
                        onClick = {
                            if (pagerState.currentPage < pages.size - 1) {
                                // Page suivante
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                // Terminer l'onboarding
                                onFinish()
                            }
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .widthIn(min = 140.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (pagerState.currentPage < pages.size - 1) "Suivant" else "Commencer",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (pagerState.currentPage < pages.size - 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Contenu d'une page d'onboarding
 */
@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icône ou logo avec dégradé
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = page.gradient
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                page.drawableRes != null -> {
                    // Afficher le logo depuis les ressources drawable
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = page.drawableRes),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp)
                    )
                }
                page.icon != null -> {
                    // Afficher l'icône vectorielle
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Titre
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight.times(1.5f)
        )
    }
}
