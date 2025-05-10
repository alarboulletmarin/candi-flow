**Version :** 1.2
**Date :** 26 Avril 2025
**Statut :** Draft Finalisé

---

## 1. Introduction

### 1.1 Objectif du Document

Ce document définit les exigences fonctionnelles et non-fonctionnelles pour la première version (MVP - Minimum Viable Product) de l'application **CandiFlow**. Il sert de référence pour le développement, les tests et la validation du produit final, qui comprendra une application frontend (iOS, Android, Web via Expo) et une API backend auto-hébergée.

### 1.2 Périmètre du Produit (Scope)

CandiFlow est une solution logicielle composée de :

1. Une **API Backend** (développée en Java Spring Boot) avec une base de données PostgreSQL, destinée à être **exclusivement auto-hébergée** par l'utilisateur via Docker.
2. Une **Application Frontend** (développée en React Native avec Expo) ciblant **iOS, Android et le Web**, qui communique avec l'API backend auto-hébergée.

Elle vise à fournir :

- Aux **Candidats** : Un outil intuitif et détaillé pour suivre leurs processus de candidature sur mobile ou web.
- Aux **Recruteurs/Équipes RH** : Un outil interne simple (via l'app frontend) pour gérer leur pipeline de candidats pour des offres spécifiques.

L'ensemble de la solution sera **gratuit** et **open source**. Ce document couvre les exigences pour la version MVP.

### 1.3 Définitions, Acronymes et Abréviations

- **SRS :** Software Requirements Specification (Cahier des Exigences)
- **MVP :** Minimum Viable Product
- **UI :** User Interface (Interface Utilisateur)
- **UX :** User Experience (Expérience Utilisateur)
- **CRUD :** Create, Read, Update, Delete
- **API :** Application Programming Interface
- **REST :** Representational State Transfer
- **JWT :** JSON Web Token
- **RBAC :** Role-Based Access Control
- **Kanban :** Méthode visuelle de gestion de flux
- **Timeline / Frise Chronologique :** Vue temporelle des étapes
- **Self-hosted :** Application (partie backend) hébergée par l'utilisateur final.
- **RN :** React Native
- **Expo :** Framework et plateforme pour le développement React Native.
- **JPA :** Jakarta Persistence API (utilisé via Spring Data JPA).
- **JDK :** Java Development Kit

### 1.4 Références

- Discussions et spécifications projet antérieures.
- Documentation React Native : https://reactnative.dev/docs/getting-started
- Documentation Expo : https://docs.expo.dev/
- Documentation Spring Boot : https://spring.io/projects/spring-boot
- Documentation Spring Data JPA : https://spring.io/projects/spring-data-jpa
- Documentation Spring Security : https://spring.io/projects/spring-security
- Documentation React Navigation : https://reactnavigation.org/
- Documentation PostgreSQL : https://www.postgresql.org/docs/
- Documentation Docker : https://docs.docker.com/

### 1.5 Vue d'Ensemble du Document

- Section 1 : Introduction
- Section 2 : Description Générale
- Section 3 : Exigences Spécifiques (Fonctionnelles, UI, API, Non-Fonctionnelles)
- Section 4 : Annexes (Optionnel)

---

## 2. Description Générale

### 2.1 Perspective du Produit

CandiFlow est un système **client-serveur**. Le **serveur** (API Java Spring Boot + DB PostgreSQL) est **auto-hébergé** par l'utilisateur via Docker. Le **client** (Application React Native/Expo) est installé sur les appareils de l'utilisateur (iOS, Android) ou accédé via le web (Expo Web) et communique avec l'API backend via des requêtes HTTP RESTful JSON sur le réseau (local ou internet, selon la configuration de l'hébergement backend).

### 2.2 Fonctions du Produit (Résumé)

- Gestion de comptes utilisateurs avec rôles (Candidat / Recruteur) via API sécurisée.
- **Pour les Candidats (via app RN) :** Création, visualisation, mise à jour et suivi détaillé (timeline) des candidatures ; gestion des documents et notes associés ; planification de relances.
- **Pour les Recruteurs (via app RN) :** Création et gestion d'offres d'emploi ; ajout et suivi de candidats par offre via un pipeline visuel (Kanban) ; gestion de notes (horodatées/attribuées) et documents par candidat/offre.

### 2.3 Caractéristiques des Utilisateurs

- **Candidat :** Chercheur d'emploi souhaitant un outil moderne et privé pour organiser son suivi sur mobile et/ou web. Doit pouvoir installer une app mobile ou accéder à une web app, et configurer l'URL de l'API backend auto-hébergée. Une certaine aisance technique est implicite pour la partie configuration/hébergement backend.
- **Recruteur / Équipe RH :** Professionnel(s) utilisant l'outil en interne (via instance auto-hébergée par leur organisation). A besoin d'un outil simple et visuel pour le suivi de pipeline sur mobile/web.

### 2.4 Contraintes

- **Déploiement Backend :** Exclusivement auto-hébergé via Docker (API + DB).
- **Distribution Frontend :** Via build natif (.apk/.ipa) ou web (Expo Web). L'utilisateur final configure l'URL de l'API backend.
- **Coût :** Application 100% gratuite et open source.
- **Technologie Imposée :**
    - Frontend: React Native (TypeScript) avec Expo.
    - Backend: Java (JDK 21 LTS recommandé) / Spring Boot (v3.x stable).
    - Accès Données: Spring Data JPA (Hibernate).
    - Authentification: Spring Security avec JWT.
    - Base de Données: PostgreSQL (v16+ recommandé).
    - Conteneurisation: Docker / Docker Compose.
    - Build Backend: Gradle (recommandé) ou Maven.
- **Sécurité Backend :** La sécurité de l'environnement d'hébergement Docker est sous responsabilité utilisateur. L'API doit implémenter les bonnes pratiques de sécurité (HTTPS recommandé, validation entrées, protection via Spring Security).
- **Plateformes Frontend :** Versions récentes d'iOS, Android, et navigateurs web modernes (pour Expo Web).

### 2.5 Hypothèses et Dépendances

- L'utilisateur (ou son organisation) dispose de l'infrastructure et des compétences pour déployer et maintenir les conteneurs Docker (API + DB).
- L'utilisateur est responsable des sauvegardes de sa base de données PostgreSQL.
- Le bon fonctionnement dépend de la connectivité réseau entre l'application RN et l'API backend auto-hébergée.
- Dépendance envers les écosystèmes React Native/Expo et Java/Spring Boot/JPA.

---

## 3. Exigences Spécifiques

### 3.1 Exigences Fonctionnelles (MVP)

*(Le QUOI fonctionnel est similaire, mais formulé dans un contexte Client <-> API)*

### EF-AUTH : Authentification et Gestion de Compte

- **EF-AUTH-01 :** L'API backend doit fournir un endpoint `/api/auth/register` pour créer un compte local (Email/Password hashé). Doit assigner un rôle ('CANDIDATE' ou 'RECRUITER').
- **EF-AUTH-02 :** L'API backend doit fournir un endpoint `/api/auth/login` qui valide les identifiants et retourne un JWT (contenant `userId`, `role`, expiration) en cas de succès.
- **EF-AUTH-03 :** L'application RN doit permettre à l'utilisateur de s'inscrire et se connecter via des écrans dédiés, en appelant les endpoints API correspondants.
- **EF-AUTH-04 :** L'application RN doit stocker le JWT reçu de manière sécurisée (ex: Expo SecureStore).
- **EF-AUTH-05 :** L'application RN doit inclure le JWT dans les headers (`Authorization: Bearer <token>`) des requêtes vers les endpoints API protégés.
- **EF-AUTH-06 :** L'API backend doit protéger tous les endpoints (sauf login/register) en validant le JWT et en vérifiant les rôles si nécessaire via Spring Security.

### EF-COMMON : Fonctionnalités Communes (dans l'App RN)

- **EF-COMMON-01 :** Une page/vue d'accueil doit être affichée avant connexion (peut être l'écran de login/register).
- **EF-COMMON-02 :** Une navigation principale (ex: Tab Navigator en bas, ou Drawer) doit permettre d'accéder aux différentes sections après connexion, en fonction du rôle.

### EF-CANDIDATE : Vue Candidat (dans l'App RN)

- **EF-CANDIDATE-01 (Dashboard) :** Écran affichant la liste des candidatures de l'utilisateur (requête GET `/api/applications`).
    - **(UX - voir SRS v1.1)** : Inclut Badges statut colorés, indicateur de relance. Tri/Filtre côté client pour la réactivité.
- **EF-CANDIDATE-02 (Ajout) :** Écran/Modal avec formulaire pour ajouter une candidature (requête POST `/api/applications`).
    - **(UX - voir SRS v1.1)** : Inclut pré-remplissage date/statut, tentative de récupération titre via URL (via un endpoint API dédié `/api/utils/fetch-title?url=...`). Validation via `react-hook-form`/`zod`.
- **EF-CANDIDATE-03 (Détail) :** Écran affichant les détails d'une candidature (requête GET `/api/applications/{id}`).
    - **(UX - voir SRS v1.1 - Timeline) :** Affichage de la frise chronologique (requête GET `/api/applications/{id}/status-updates`). Composant `StatusTimeline` avec icônes, dates relatives/absolues, notes.
    - **(UX - voir SRS v1.1 - Ajout Étape) :** Bouton/Modal pour ajouter une étape (requête POST `/api/applications/{id}/status-updates`).
    - **(UX - voir SRS v1.1 - Notes) :** Section Notes générales (modification via requête PUT `/api/applications/{id}`).
    - **(UX - voir SRS v1.1 - Documents) :** Section Documents (requêtes POST/GET/DELETE vers `/api/applications/{id}/documents`, gestion de l'upload vers l'API backend).
    - **(UX - voir SRS v1.1 - Relance) :** Champ date/note (modification via requête PUT `/api/applications/{id}`).
    - **(UX - voir SRS v1.1 - Copie facile)** Boutons de copie.

### EF-RECRUITER : Vue Recruteur (dans l'App RN)

- **EF-RECRUITER-01 (Gestion Offres) :** Écran listant les offres (requête GET `/api/openings`). Permet création (POST `/api/openings`) et archivage (PUT `/api/openings/{id}`).
    - **(UX - voir SRS v1.1)** : Affichage avec compteurs de candidats, statut visuel, filtres.
- **EF-RECRUITER-02 (Ajout Candidat) :** Fonctionnalité pour ajouter un `OpeningApplicant` à une offre (requête POST `/api/openings/{id}/applicants`).
    - **(UX - voir SRS v1.1)** : Inclut upload CV/LM, vérification simple de doublon email (logique dans l'API backend).
- **EF-RECRUITER-03 (Pipeline Kanban) :** Écran affichant les candidats d'une offre en Kanban (requête GET `/api/openings/{id}/applicants`).
    - **(UX - voir SRS v1.1)** : Colonnes de statut, cartes candidats, Drag-and-Drop pour changer le statut (déclenche requête PUT `/api/openings/{openingId}/applicants/{applicantId}/status`).
    - **(UX - voir SRS v1.1)** : Accès aux détails candidat en cliquant sur une carte.
- **EF-RECRUITER-04 (Notes Recruteur) :** Fonctionnalité pour ajouter/voir des notes sur un `OpeningApplicant` (requêtes POST/GET vers `/api/applicants/{id}/notes`).
    - **(UX - voir SRS v1.1)** : Notes horodatées et indiquant l'auteur (géré par l'API backend).

### 3.2 Exigences d'Interface Utilisateur (UI) - React Native

- **UI-RN-01 :** Interface **épurée, moderne, intuitive**, suivant les conventions de design **iOS et Android** (peut utiliser des composants adaptatifs ou un design custom cohérent).
- **UI-RN-02 :** Application **réactive et fluide** sur les appareils mobiles cibles. Adaptation aux différentes tailles d'écrans mobiles/tablettes. La version Expo Web doit être fonctionnelle.
- **UI-RN-03 :** Navigation claire et standard via **React Navigation** (Tabs, Stack...).
- **UI-RN-04 :** Feedback utilisateur clair (indicateurs de chargement natifs, toasts/alertes natives...).
- **UI-RN-05 :** Utilisation cohérente d'icônes adaptées au mobile.

### 3.3 Exigences d'Interfaces Externes / API Backend

- **API-01 :** Le backend Spring Boot doit exposer une **API RESTful** sécurisée (via JWT) et documentée (ex: via Springdoc OpenAPI/Swagger) pour toutes les fonctionnalités requises par le frontend RN.
- **API-02 :** L'API doit utiliser des DTOs (Data Transfer Objects) et la validation (Bean Validation) pour les entrées/sorties.
- **API-03 :** L'API doit gérer le contrôle d'accès basé sur les rôles pour ses endpoints.
- **API-04 :** L'API doit retourner des codes de statut HTTP et des messages d'erreur standardisés.

### 3.4 Exigences Non-Fonctionnelles (Mise à jour)

### 3.4.1 Performance (ENF-PERF)

- **ENF-PERF-01 :** L'UI React Native doit rester fluide (idéalement 60 FPS) lors des interactions et animations. Démarrage rapide de l'app.
- **ENF-PERF-02 :** L'API backend Spring Boot doit répondre rapidement (< 500ms P95 pour les requêtes typiques).
- **ENF-PERF-03 :** L'utilisation des ressources (CPU, RAM, Batterie) par l'app RN doit être optimisée. La consommation du backend Spring Boot doit être raisonnable pour l'auto-hébergement.

### 3.4.2 Sécurité (ENF-SECU)

- **ENF-SECU-01 :** Authentification backend robuste via Spring Security (hashage BCrypt, gestion et validation JWT).
- **ENF-SECU-02 :** Stockage sécurisé du JWT sur l'appareil mobile (Expo SecureStore). Transmission sécurisée (HTTPS requis pour l'API).
- **ENF-SECU-03 :** Protection de l'API via Spring Security (CSRF si cookies utilisés, XSS via échappement des sorties, configuration CORS...).
- **ENF-SECU-04 :** RBAC appliqué sur l'API backend.
- **ENF-SECU-05 :** Responsabilité utilisateur pour la sécurité de l'instance auto-hébergée.

### 3.4.3 Fiabilité (ENF-FIAB)

- **ENF-FIAB-01 :** Gestion des erreurs réseau et API dans l'app RN.
- **ENF-FIAB-02 :** Gestion des erreurs et transactions dans l'API backend.
- **ENF-FIAB-03 :** Persistance et cohérence des données via PostgreSQL et JPA.

### 3.4.4 Utilisabilité (ENF-USAB)

- **ENF-USAB-01 :** Prise en main facile et intuitive de l'app RN.
- **ENF-USAB-02 :** Workflows efficaces sur mobile/web.

### 3.4.5 Maintenabilité (ENF-MAINT)

- **ENF-MAINT-01 :** Code source TypeScript (RN) et Java (Spring Boot) propre et suivant les conventions. Utilisation des outils de build (Expo CLI, Gradle/Maven).
- **ENF-MAINT-02 :** Architecture modulaire (composants RN, packages/modules Spring Boot).
- **ENF-MAINT-03 :** Commentaires clairs (TSDoc/JSDoc, JavaDoc).
- **ENF-MAINT-04 :** Stratégie de tests (Unitaires: Jest/RTL pour RN, JUnit/Mockito pour Java ; Intégration API ; E2E : Maestro/Detox/Appium pour mobile, Playwright/Cypress pour Expo Web).

### 3.4.6 Portabilité / Déployabilité (ENF-PORT)

- **ENF-PORT-01 :** Backend (API+DB) déployable via Docker Compose.
- **ENF-PORT-02 :** Frontend buildable pour iOS, Android (via Expo EAS ou manuellement) et Web (via Expo Web).
- **ENF-PORT-03 :** Configuration via variables d'environnement (backend) et mécanisme de configuration dans l'app RN pour l'URL de l'API.

---

## 4. Annexes (Optionnel)

---
