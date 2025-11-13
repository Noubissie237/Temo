package com.propentatech.kumbaka.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * Coach Mark composable pour guider l'utilisateur
 * Affiche une overlay avec un spotlight sur l'élément cible et un message explicatif
 */
@Composable
fun CoachMark(
    visible: Boolean,
    targetBounds: Rect?,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible || targetBounds == null) return

    val density = LocalDensity.current
    
    // Animation d'apparition
    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "coach_mark_alpha"
    )
    
    // Animation du spotlight (pulsation)
    val infiniteTransition = rememberInfiniteTransition(label = "spotlight_pulse")
    val spotlightScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spotlight_scale"
    )

    if (animatedAlpha > 0f) {
        Popup(
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = onDismiss
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            ) {
                // Overlay sombre avec découpe pour le spotlight
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = animatedAlpha }
                ) {
                    // Fond sombre
                    drawRect(
                        color = Color.Black.copy(alpha = 0.7f),
                        size = size
                    )
                    
                    // Découpe circulaire pour le spotlight
                    val spotlightRadius = with(density) {
                        maxOf(targetBounds.width, targetBounds.height) / 2 + 24.dp.toPx()
                    } * spotlightScale
                    
                    val spotlightCenter = Offset(
                        x = targetBounds.center.x,
                        y = targetBounds.center.y
                    )
                    
                    // Dessiner le cercle de spotlight (transparent)
                    drawCircle(
                        color = Color.Transparent,
                        radius = spotlightRadius,
                        center = spotlightCenter,
                        blendMode = BlendMode.Clear
                    )
                    
                    // Cercle de bordure pour l'effet visuel
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = spotlightRadius,
                        center = spotlightCenter,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }
                
                // Message explicatif positionné sous l'élément cible
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    val maxHeightPx = with(density) { maxHeight.toPx() }
                    val messageYPosition = with(density) {
                        (targetBounds.bottom + 40.dp.toPx()).coerceAtMost(
                            maxHeightPx - 200.dp.toPx()
                        )
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = with(density) { messageYPosition.toDp() })
                            .graphicsLayer { alpha = animatedAlpha },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Compris !")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modificateur pour capturer la position et la taille d'un composable
 * Utilisé pour obtenir les bounds de l'élément cible du coach mark
 */
@Composable
fun Modifier.coachMarkTarget(
    onBoundsCalculated: (Rect) -> Unit
): Modifier {
    return this.onGloballyPositioned { coordinates ->
        val position = coordinates.positionInRoot()
        val size = coordinates.size
        
        val bounds = Rect(
            left = position.x,
            top = position.y,
            right = position.x + size.width,
            bottom = position.y + size.height
        )
        
        onBoundsCalculated(bounds)
    }
}
