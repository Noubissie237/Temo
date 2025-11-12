package com.propentatech.kumbaka.data

import com.propentatech.kumbaka.data.model.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Données mock pour tester l'application
 * Contient des exemples de tâches, notes et événements
 */
object MockData {

    /**
     * Tâches d'exemple - DÉSACTIVÉ
     * Les tâches sont maintenant gérées par la base de données Room
     */
    val mockTasks = emptyList<Task>()

    /**
     * Notes d'exemple - DÉSACTIVÉ
     * Les notes sont maintenant gérées par la base de données Room
     */
    val mockNotes = emptyList<Note>()
    
    /*
    val mockNotesOLD = listOf(
        Note(
            id = "note1",
            title = "Idées pour le projet X",
            content = "Penser à intégrer la nouvelle API de paiement et revoir le design du dashboard...",
            type = NoteType.TEXT,
            updatedAt = LocalDateTime.now().minusDays(1)
        ),
        Note(
            id = "note2",
            title = "Liste de courses",
            content = "Lait, Oeufs, Pain, Fromage, Tomates, Avocats, Poulet...",
            type = NoteType.TEXT,
            updatedAt = LocalDateTime.now().minusHours(10)
        ),
        Note(
            id = "note3",
            title = "Résumé de la réunion Q3",
            content = "Points abordés : avancement du Q3, budget Q4,...",
            type = NoteType.TEXT,
            updatedAt = LocalDateTime.now().minusDays(2)
        ),
        Note(
            id = "note4",
            title = "Lien à consulter",
            content = "Ne pas oublier de lire l'article sur le design system partagé par...",
            type = NoteType.LINK,
            links = listOf("https://example.com/design-system"),
            updatedAt = LocalDateTime.now().minusHours(5)
        ),
        Note(
            id = "note5",
            title = "Inspiration design",
            content = "Collection de palettes de couleurs et de typographies pour la nouvelle application...",
            type = NoteType.IMAGE,
            imageUrl = "gradient_placeholder",
            updatedAt = LocalDateTime.now().minusDays(3)
        ),
        Note(
            id = "note6",
            title = "Tâches du projet",
            content = "Checklist pour le projet",
            type = NoteType.CHECKLIST,
            checklistItems = listOf(
                ChecklistItem(text = "Finaliser le design de l'application", isChecked = true),
                ChecklistItem(text = "Préparer la présentation pour lundi", isChecked = false),
                ChecklistItem(text = "Contacter le client pour un retour", isChecked = false)
            ),
            updatedAt = LocalDateTime.now().minusHours(14)
        ),
        Note(
            id = "note7",
            title = "Consulter la documentation du projet",
            content = "Documentation technique à lire",
            type = NoteType.LINK,
            links = listOf("https://docs.example.com/project"),
            updatedAt = LocalDateTime.now().minusDays(1)
        )
    )
    */

    /**
     * Événements d'exemple - DÉSACTIVÉ
     * Les événements sont maintenant gérés par la base de données Room
     */
    val mockEvents = emptyList<Event>()
    
    /*
    val mockEventsOLD = listOf(
        Event(
            id = "event1",
            title = "Réunion de projet",
            description = "Discussion projet Alpha",
            date = LocalDate.now(),
            time = LocalTime.of(10, 0),
            location = "Salle de conférence A"
        ),
        Event(
            id = "event2",
            title = "Dentiste",
            description = "Contrôle annuel",
            date = LocalDate.now().plusDays(1),
            time = LocalTime.of(14, 30),
            location = "Cabinet Dr. Martin"
        ),
        Event(
            id = "event3",
            title = "Anniversaire de Marie",
            description = "Ne pas oublier le cadeau !",
            date = LocalDate.now().plusDays(3),
            time = null,
            location = ""
        ),
        Event(
            id = "event4",
            title = "Réunion d'équipe Sync",
            description = "Discussion projet Alpha",
            date = LocalDate.now(),
            time = LocalTime.of(10, 0),
            location = "Visioconférence"
        ),
        Event(
            id = "event5",
            title = "Finaliser le rapport Q3",
            description = "À rendre avant la fin de journée",
            date = LocalDate.now(),
            time = LocalTime.of(14, 0),
            location = ""
        ),
        Event(
            id = "event6",
            title = "Déjeuner avec Chloé",
            description = "Restaurant \"Le Bistrot\"",
            date = LocalDate.now(),
            time = LocalTime.of(12, 30),
            location = "Le Bistrot"
        ),
        Event(
            id = "event7",
            title = "Réunion d'équipe hebdomadaire",
            description = "Point hebdomadaire",
            date = LocalDate.now().plusDays(7),
            time = LocalTime.of(10, 0),
            location = "Salle B"
        )
    )
    */
}
