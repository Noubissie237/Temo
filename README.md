# Temo - Application de Gestion de Tâches et Notes

Temo est une application Android moderne développée avec **Jetpack Compose** qui aide les utilisateurs à gérer leurs tâches, prendre des notes et planifier des événements personnels.

## 📱 Fonctionnalités

### 1. Gestion des Tâches
- ✅ Création, modification et suppression de tâches
- ⏰ Définition d'échéances avec rappels
- 🎯 Niveaux de priorité (Haute, Moyenne, Basse)
- ✓ Marquage des tâches comme complétées
- 📋 Organisation par date (Aujourd'hui, Demain, Terminées)

### 2. Prise de Notes
- 📝 Notes texte enrichies
- ☑️ Checklists interactives
- 🖼️ Support d'images
- 🔗 Liens et références
- 🏷️ Filtrage par type de note
- 🔍 Recherche dans les notes

### 3. Planification d'Événements
- 📅 Calendrier mensuel interactif
- 🎉 Création d'événements avec date et heure
- ⏳ Compte à rebours (J-xx)
- 🔗 Liaison de tâches aux événements
- 📍 Localisation des événements

### 4. Tableau de Bord
- 👀 Vue d'ensemble des tâches du jour
- 📆 Événements à venir
- 📄 Notes récentes
- 🚀 Accès rapide à toutes les fonctionnalités

### 5. Paramètres
- 🌓 Mode sombre/clair
- 🎨 Personnalisation des couleurs
- 🔔 Gestion des notifications
- 💾 Import/Export des données

## 🏗️ Architecture

L'application suit l'architecture **MVVM (Model-View-ViewModel)** recommandée par Google :

```
app/
├── data/
│   ├── model/          # Modèles de données (Task, Note, Event)
│   ├── repository/     # Repositories pour la gestion des données
│   └── MockData.kt     # Données de test
├── ui/
│   ├── screens/        # Écrans de l'application
│   ├── components/     # Composants réutilisables
│   ├── navigation/     # Configuration de la navigation
│   ├── viewmodel/      # ViewModels pour la logique métier
│   └── theme/          # Thème et couleurs
└── MainActivity.kt     # Point d'entrée de l'application
```

## 🎨 Design

L'interface utilisateur est basée sur les maquettes fournies dans le dossier `Temo_design/` :

- **Couleur primaire** : Bleu (#2196F3)
- **Couleur secondaire** : Violet (#673AB7)
- **Mode clair** : Fond blanc/gris clair
- **Mode sombre** : Fond noir/gris foncé
- **Material Design 3** : Composants modernes et accessibles

## 🛠️ Technologies Utilisées

- **Kotlin** : Langage de programmation
- **Jetpack Compose** : Framework UI moderne
- **Material Design 3** : Système de design
- **Navigation Compose** : Navigation entre écrans
- **ViewModel** : Gestion de l'état
- **StateFlow** : Flux de données réactifs
- **Coroutines** : Programmation asynchrone

## 📦 Dépendances

```kotlin
// Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose")

// Navigation
implementation("androidx.navigation:navigation-compose:2.8.0")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
```

## 🚀 Installation

1. Clonez le repository
2. Ouvrez le projet dans Android Studio
3. Synchronisez les dépendances Gradle
4. Lancez l'application sur un émulateur ou un appareil physique

### Prérequis
- Android Studio Hedgehog ou supérieur
- JDK 11 ou supérieur
- Android SDK 24 (Android 7.0) minimum
- Android SDK 36 (cible)

## 📱 Écrans Principaux

1. **Accueil** : Dashboard avec résumé des tâches, événements et notes
2. **Tâches** : Liste des tâches avec filtres et gestion
3. **Notes** : Bibliothèque de notes avec filtres par type
4. **Événements** : Liste et calendrier des événements
5. **Paramètres** : Configuration de l'application

## 🔄 Navigation

L'application utilise une **BottomNavigationBar** pour naviguer entre les 5 écrans principaux :
- 🏠 Accueil
- ✓ Tâches
- 📝 Notes
- 📅 Événements
- ⚙️ Paramètres

## 📝 Données Mock

L'application inclut des données de test pour faciliter le développement et les tests :
- 7 tâches d'exemple
- 7 notes de différents types
- 7 événements planifiés

## 🔮 Améliorations Futures

- [ ] Persistance des données (Room Database)
- [ ] Synchronisation cloud
- [ ] Notifications push
- [ ] Widgets pour l'écran d'accueil
- [ ] Partage de notes et tâches
- [ ] Mode hors ligne complet
- [ ] Thèmes personnalisés
- [ ] Export PDF des notes

## 👨‍💻 Développement

### Structure du Code

- **Modèles** : Classes de données immuables avec `data class`
- **Repositories** : Gestion centralisée des données avec `StateFlow`
- **ViewModels** : Logique métier séparée de l'UI
- **Composables** : Fonctions UI réutilisables et modulaires
- **Navigation** : Routes définies avec Navigation Compose

### Conventions de Code

- Commentaires en français pour la documentation
- Nommage descriptif des fonctions et variables
- Séparation claire des responsabilités
- Composables purs et testables


## 🤝 Contribution

Pour contribuer au projet :
1. Créez une branche pour votre fonctionnalité
2. Commitez vos changements
3. Créez une Pull Request

---

**Version** : 1.0.0  
**Dernière mise à jour** : Novembre 2024
