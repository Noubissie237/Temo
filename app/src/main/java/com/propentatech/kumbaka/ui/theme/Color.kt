package com.propentatech.kumbaka.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// PALETTE KUMBAKA — ORANGE PREMIUM
// Mode Clair : Orange + Blanc | Mode Sombre : Orange + Noir
// ============================================================

// === ORANGE (couleur signature) ===
val OrangePrimary       = Color(0xFFFF6B00)  // Orange vif principal
val Orange              = OrangePrimary      // Alias pour compatibilité
val OrangeLight         = Color(0xFFFF8C42)  // Orange clair (hover/secondary)
val OrangeDark          = Color(0xFFE85D00)  // Orange foncé (pressed)
val OrangeGlow          = Color(0xFFFF6B00).copy(alpha = 0.15f) // Halo orange

// === MODE CLAIR (White + Orange) ===
val BackgroundLight     = Color(0xFFFFFFFF)  // Fond blanc pur
val SurfaceLight        = Color(0xFFFFF8F4)  // Surface très légèrement chaude
val SurfaceVariantLight = Color(0xFFFFEEE0)  // Cartes légèrement orangées
val TextPrimary         = Color(0xFF1A1A1A)  // Texte principal presque noir
val TextSecondary       = Color(0xFF6B6B6B)  // Texte secondaire gris

// === MODE SOMBRE (Black + Orange) ===
val BackgroundDark      = Color(0xFF0A0A0A)  // Noir profond
val SurfaceDark         = Color(0xFF1A1A1A)  // Surface grise très foncée
val SurfaceVariantDark  = Color(0xFF242424)  // Cartes légèrement éclairées
val TextPrimaryDark     = Color(0xFFFFFFFF)  // Blanc pur
val TextSecondaryDark   = Color(0xFFB0B0B0)  // Gris clair

// === COULEURS SÉMANTIQUES (MyLive Strict) ===
val SuccessGreen   = OrangePrimary
val ErrorRed       = Color.Black
val WarningYellow  = OrangeLight
val InfoBlue       = Color.Gray

// === TÂCHES : Priorités (MyLive Strict) ===
val PriorityHigh   = Color.Black      // Très sérieux / Premium
val PriorityMedium = OrangePrimary    // Action requise
val PriorityLow    = Color(0xFFB0B0B0) // Moins urgent (Gris)

// === Gradients fréquents ===
val GradientOrangeStart = Color(0xFFFF6B00)
val GradientOrangeEnd   = Color(0xFFFF3D00)

// Conservés pour compatibilité avec du code existant :
val PrimaryBlue         = OrangePrimary
val SecondaryPurple     = OrangeLight
val PrimaryBlueDark     = OrangePrimary
val SecondaryPurpleDark = OrangeLight
val AccentLightBlue     = OrangeGlow
val AccentLightPurple   = SurfaceVariantLight