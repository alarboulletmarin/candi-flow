# CandiFlow

<div align="center">
  <h3>Application de suivi de candidatures et de recrutement auto-hébergée</h3>
  <p>Simplifiez votre processus de candidature et de recrutement avec une solution complète et open source</p>
  
  [![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
  [![React Native](https://img.shields.io/badge/React%20Native-Expo-61dafb)](https://reactnative.dev/)
  [![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED)](https://www.docker.com/)
</div>

## 📋 Sommaire

- [À propos](#-à-propos)
- [Fonctionnalités](#-fonctionnalités)
- [Architecture](#-architecture)
- [Installation](#-installation)
- [Utilisation](#-utilisation)
- [Développement](#-développement)
- [Tests](#-tests)
- [Contribution](#-contribution)
- [Licence](#-licence)

## 🚀 À propos

CandiFlow est une application web auto-hébergée conçue pour simplifier le suivi des candidatures et le processus de recrutement. Elle offre deux perspectives distinctes :

- **Perspective Candidat** : Suivez vos candidatures avec une timeline détaillée, gérez vos documents et gardez une trace de toutes vos interactions avec les recruteurs.
- **Perspective Recruteur** : Gérez votre pipeline de recrutement, suivez les candidats pour chaque offre d'emploi et organisez efficacement le processus de sélection.

L'application est entièrement open source et gratuite, conçue pour être auto-hébergée via Docker.

## ✨ Fonctionnalités

### Pour les Candidats

- **Tableau de bord** : Vue d'ensemble de toutes vos candidatures
- **Gestion des candidatures** : Ajout, modification et suivi des candidatures
- **Timeline de statuts** : Visualisation chronologique des étapes de chaque candidature
- **Gestion documentaire** : Stockage organisé des CV, lettres de motivation et autres documents
- **Rappels** : Notifications pour les relances et les prochaines étapes
- **Exportation** : Export des données de candidature en différents formats

### Pour les Recruteurs

- **Gestion des offres d'emploi** : Création et suivi des offres
- **Pipeline Kanban** : Visualisation et gestion des candidats par étape de recrutement
- **Évaluation des candidats** : Système de notation et de commentaires
- **Statistiques** : Métriques sur le processus de recrutement
- **Collaboration** : Partage d'informations entre membres de l'équipe RH

## 🏗 Architecture

CandiFlow est construit avec une architecture moderne et modulaire :

### Backend (candiflow-api)

- **Framework** : Java Spring Boot
- **Base de données** : PostgreSQL
- **Documentation API** : OpenAPI/Swagger
- **Tests** : JUnit, Mockito
- **Qualité de code** : SonarQube, JaCoCo

### Frontend (candiflow-ui)

- **Framework** : React Native avec Expo (pour iOS, Android, Web)
- **Navigation** : React Navigation
- **Gestion d'état** : Context API
- **UI Components** : Composants personnalisés et bibliothèques React Native

### Déploiement

- **Conteneurisation** : Docker et Docker Compose
- **Configuration** : Variables d'environnement et fichiers de configuration

## 📦 Installation

### Prérequis

- Docker et Docker Compose
- Java 21 (pour le développement)
- Node.js 18+ (pour le développement)

### Installation rapide avec Docker

1. Clonez le dépôt :
   ```bash
   git clone https://github.com/votre-utilisateur/candi-flow.git
   cd candi-flow
   ```

2. Créez un fichier `.env` à partir du modèle :
   ```bash
   cp .env.example .env
   ```

3. Modifiez le fichier `.env` avec vos paramètres

4. Lancez l'application avec Docker Compose :
   ```bash
   docker-compose up -d
   ```

5. Accédez à l'application :
   - Backend API : http://localhost:8080
   - Frontend Web : http://localhost:8081

## 🖥 Utilisation

### Premier démarrage

1. Créez un compte utilisateur (candidat ou recruteur)
2. Connectez-vous à l'application
3. Suivez le guide de démarrage rapide qui s'affiche à la première connexion

### Guide du candidat

1. Ajoutez une nouvelle candidature depuis le tableau de bord
2. Renseignez les détails de l'entreprise et du poste
3. Suivez l'évolution de votre candidature en ajoutant des mises à jour de statut
4. Consultez la timeline pour visualiser l'historique complet

### Guide du recruteur

1. Créez une nouvelle offre d'emploi
2. Ajoutez des candidats à cette offre
3. Utilisez le tableau Kanban pour déplacer les candidats entre les différentes étapes
4. Ajoutez des notes et des évaluations pour chaque candidat

## 💻 Développement

### Configuration de l'environnement de développement

#### Backend (Spring Boot)

1. Installez Java 21 et Gradle
2. Importez le projet dans votre IDE (IntelliJ IDEA recommandé)
3. Configurez une base de données PostgreSQL locale ou utilisez Docker
4. Exécutez l'application :
   ```bash
   cd candiflow-api
   ./gradlew bootRun
   ```

#### Frontend (React Native/Expo)

1. Installez Node.js et npm/yarn
2. Installez les dépendances :
   ```bash
   cd candiflow-ui
   npm install
   ```
3. Lancez l'application en mode développement :
   ```bash
   npm start
   ```

### Structure du projet

```
candi-flow/
├── candiflow-api/                # Backend Spring Boot
│   ├── src/                      # Code source Java
│   │   ├── main/
│   │   │   ├── java/com/candiflow/api/
│   │   │   │   ├── config/       # Configuration
│   │   │   │   ├── controller/   # Contrôleurs REST
│   │   │   │   ├── dto/          # Objets de transfert de données
│   │   │   │   ├── exception/    # Gestion des exceptions
│   │   │   │   ├── model/        # Entités et modèles
│   │   │   │   ├── repository/   # Accès aux données
│   │   │   │   ├── security/     # Configuration de sécurité
│   │   │   │   ├── service/      # Logique métier
│   │   │   ├── resources/        # Fichiers de configuration
│   │   ├── test/                 # Tests unitaires et d'intégration
│   ├── build.gradle              # Configuration Gradle
├── candiflow-ui/                 # Frontend React Native
│   ├── app/                      # Routes et pages
│   ├── src/
│   │   ├── components/           # Composants React
│   │   ├── services/             # Services API
│   │   ├── store/                # Gestion d'état
│   │   ├── types/                # Types TypeScript
│   │   ├── utils/                # Utilitaires
│   ├── package.json              # Dépendances npm
├── docker-compose.yml            # Configuration Docker Compose
├── documentations/               # Documentation du projet
└── README.md                     # Ce fichier
```

## 🧪 Tests

### Backend

Exécutez les tests unitaires et d'intégration :

```bash
cd candiflow-api
./gradlew test
```

Pour générer un rapport de couverture de code avec JaCoCo :

```bash
./gradlew jacocoTestReport
```

### Frontend

Exécutez les tests React Native :

```bash
cd candiflow-ui
npm test
```

## 🔍 Analyse de code

Le projet est configuré avec SonarQube pour l'analyse de la qualité du code. Pour exécuter une analyse :

```bash
./run-sonar-analysis.sh
```

## 👥 Contribution

Les contributions sont les bienvenues ! Voici comment vous pouvez contribuer :

1. Forkez le projet
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/amazing-feature`)
3. Committez vos changements (`git commit -m 'feat: add amazing feature'`)
4. Poussez vers la branche (`git push origin feature/amazing-feature`)
5. Ouvrez une Pull Request

Veuillez respecter les conventions de commit et les standards de code du projet.

## 📄 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

<div align="center">
  <p>Développé avec ❤️ pour simplifier le processus de candidature et de recrutement</p>
</div>