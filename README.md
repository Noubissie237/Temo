# MyLive - Système Intégré de Productivité et Gestion Personnelle

MyLive est une solution logicielle Android haut de gamme conçue pour centraliser la gestion de vie, les finances, la productivité et le bien-être au sein d'une interface unique et cohérente. Développée avec Jetpack Compose, l'application respecte une charte graphique "Haut Standing" basée sur un contraste élégant de tons sombres et d'accents orange.

## Architecture et Technologies

L'application repose sur une architecture robuste et moderne :
- Langage : Kotlin
- Framework UI : Jetpack Compose (Material 3)
- Base de données : Room (Persistance locale sécurisée)
- Traitement asynchrone : Coroutines et StateFlow
- Injection de dépendances : Singleton et Factory patterns
- Planification : WorkManager et AlarmManager

## Modules Principaux

### 1. Tableau de Bord et IA Conseillère
Le centre de commande de l'application offre une vue d'emsemble des activités en cours. Il intègre un moteur de conseil intelligent (Advisor) qui analyse les données de l'utilisateur pour proposer des recommandations personnalisées sur la gestion du temps et des finances.

### 2. Gestion Financière Avancée
Ce module permet un suivi rigoureux du patrimoine :
- Enregistrement des transactions (Revenus et Dépenses) avec catégories personnalisables.
- Génération de rapports visuels détaillés via des graphiques circulaires et à barres.
- Gestion des contacts liés aux transactions pour un suivi relationnel des finances.

### 3. Planification et Calendrier
Un système de gestion temporelle complet :
- Calendrier interactif synchronisant événements, sessions de planning et notes journalières.
- Gestion d'événements avec compte à rebours et notifications de rappel.
- Prise de notes journalières pour documenter chaque étape de la journée.

### 4. Productivité et Projets
Outil de gestion de tâches professionnel :
- Suivi de projets complexes via des jalons (milestones) et barre de progression.
- Système de tâches multi-états (A faire, En cours, Terminé).
- Minuteur et chronomètre intégrés pour le suivi du temps de travail.

### 5. Bien-être et Lifestyle
Un module dédié au suivi des habitudes et de l'état émotionnel :
- Tracker d'habitudes avec logs quotidiens et statistiques de complétion.
- Journal d'humeur permettant de corréler le bien-être avec l'activité quotidienne.

### 6. Outils Experts : Calculatrice Scientifique
Une calculatrice de niveau ingénieur intégrée nativement :
- Fonctions trigonométriques, logarithmiques et exponentielles.
- Historique complet des calculs et gestion de la mémoire.
- Interface optimisée pour une visibilité maximale entre la saisie et les touches scientifiques.

### 7. Sécurité et Synchronisation Cloud
- Authentification protégée par biométrie (empreinte digitale) ou code PIN.
- Sauvegarde sécurisée sur Google Drive avec options de configuration avancées (SHA-1 et Web Client ID manuel) pour garantir la souveraineté des données.

## Installation et Déploiement

### Prérequis Techniques
- Android 7.0 (API 24) minimum.
- Support des services Google Play pour la synchronisation cloud.

### Procédure de Compilation
1. Importation du projet dans Android Studio.
2. Synchronisation de Gradle pour récupérer les dépendances.
3. Compilation via la commande ./gradlew assembleDebug.

## Développement et Maintenance

Le code source est organisé de manière modulaire :
- data : Entities, Daos, Repositories et Préférences.
- ui : Composables, Thèmes et ViewModels.
- notification : Logique des services d'alarme et récepteurs.

L'intégralité du code source est documentée en français pour faciliter l'évolution par l'équipe technique.
