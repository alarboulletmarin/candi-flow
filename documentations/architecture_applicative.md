## 1. Introduction & Objectifs Architecturaux

Ce document décrit l'architecture logicielle proposée pour **CandiFlow**, une application web auto-hébergée de suivi de candidatures pour candidats et recruteurs internes.

Les principaux objectifs architecturaux sont :

- **Modularité :** Découpler les différentes parties de l'application (UI, logique métier, accès aux données) pour faciliter la maintenance, les tests et l'évolution.
- **Robustesse :** Utiliser des technologies et des pratiques qui favorisent la fiabilité et la sécurité (typage statique, validation, gestion des erreurs).
- **Évolutivité :** Concevoir une structure qui puisse accueillir de nouvelles fonctionnalités sans nécessiter de refontes majeures.
- **Maintenabilité :** Produire un code propre, bien organisé et documenté (via le code lui-même et potentiellement JSDoc/TSDoc).
- **Expérience Développeur :** Utiliser des outils modernes et efficaces (Next.js, Prisma, TypeScript, Docker).

## 2. Stack Technologique (Rappel)

- **Framework Full-Stack :** Next.js 14+ (App Router, TypeScript)
- **Styling :** Tailwind CSS
- **Base de Données :** PostgreSQL
- **ORM :** Prisma
- **Authentification :** NextAuth.js
- **Conteneurisation :** Docker & Docker Compose

## 3. Architecture de Haut Niveau

CandiFlow adopte une architecture **full-stack monolithique** (au sens où le frontend et le backend sont gérés au sein du même projet Next.js), mais avec une **forte séparation logique des couches** :

```
+---------------------+      +--------------------------+      +-----------------+      +-----------------+
| Navigateur Client   |<---->|    Next.js Frontend      |<---->| Next.js Backend |<---->|   Prisma ORM    |<---->|   PostgreSQL    |
| (React Components)  |      | (Server/Client Components)|      | (API Routes /     |      | (Data Access)   |      | (Database)      |
| (Tailwind CSS)      |      | (Routing - App Router)  |      |  Server Actions)  |      +-----------------+      +-----------------+
+---------------------+      +--------------------------+      | (Business Logic)  |
                                                             | (Validation - Zod)|
                                                             | (Auth - NextAuth) |
                                                             +-------------------+

```

- **Frontend (Client & Server Components) :** Géré par Next.js App Router. Rend l'interface utilisateur, gère l'état local de l'UI, et interagit avec le backend via Server Actions ou API Routes.
- **Backend (API Routes / Server Actions) :** Logique métier, validation des entrées, orchestration des opérations (ex: créer une candidature et sa première étape de statut), gestion de l'authentification/autorisation, communication avec la base de données via Prisma.
- **Data Layer (Prisma) :** ORM qui abstrait les interactions avec la base de données PostgreSQL, gère le schéma et les migrations.
- **Database (PostgreSQL) :** Stockage persistant des données.

## 4. Architecture Frontend (Next.js App Router)

- **Structure des Dossiers :** Organisation claire pour la maintenabilité :
    - `app/` : Cœur de l'application avec les routes, layouts et pages (convention Next.js App Router).
        - `app/api/` : Pour les API Routes si nécessaires (alternative aux Server Actions).
        - `app/(auth)/` : Groupe de routes pour les pages d'authentification (login, signup).
        - `app/(app)/` : Groupe de routes pour l'application principale après connexion, utilisant `layout.tsx` pour la structure commune (sidebar, header...).
            - `app/(app)/dashboard/` : Dashboard principal (liste des candidatures/offres).
            - `app/(app)/applications/[id]/` : Route dynamique pour la vue détail d'une candidature.
            - `app/(app)/openings/[id]/` : Route dynamique pour la vue détail d'une offre (recruteur).
            - ... autres routes ...
    - `components/` : Composants React réutilisables (classés par fonctionnalité ou par type - UI, Forms, etc.).
        - `components/ui/` : Composants UI de base (Button, Card, Input, Modal...). Potentiellement issus/basés sur **Shadcn/ui** pour accélérer et assurer la cohérence/accessibilité.
        - `components/features/` : Composants plus spécifiques à une fonctionnalité (ex: `StatusTimeline`, `CandidateKanbanBoard`).
    - `lib/` : Utilitaires, helpers, configuration de clients (Prisma Client), constantes.
    - `styles/` : Fichier CSS global (`globals.css` avec les directives Tailwind).
    - `prisma/` : Schéma et migrations Prisma.
- **Routing :** Géré par la convention de nommage de fichiers/dossiers de l'App Router. Utilisation des routes dynamiques pour les détails.
- **Composants Stratégie :**
    - **React Server Components (RSC) par défaut :** Pour la majorité des composants affichant des données (récupération directe des données côté serveur via Prisma, meilleur SEO si la landing page est publique, moins de JS côté client).
    - **Client Components (`'use client'`) :** Uniquement lorsque nécessaire pour l'interactivité (gestion d'état avec `useState`/`useEffect`, gestionnaires d'événements, utilisation de bibliothèques qui dépendent des API du navigateur). Exemple : Formulaires interactifs, Kanban board (drag-and-drop).
- **Gestion d'État :** Principalement via les props et l'état local (`useState`) pour les Client Components. Pour un état global simple (ex: thème clair/sombre, infos utilisateur connecté), React Context ou une bibliothèque légère comme Zustand/Jotai si nécessaire (à éviter si possible pour le MVP).
- **Styling :** Tailwind CSS pour le style utilitaire. Utilisation de `clsx` et `tailwind-merge` pour combiner conditionnellement les classes Tailwind. Organisation des styles globaux et potentiellement des variables de thème dans `globals.css`.

## 5. Architecture Backend (API Routes / Server Actions)

- **Choix d'Interaction :** Privilégier les **Server Actions** pour les mutations de données (formulaires d'ajout/modification) car elles simplifient le code (pas besoin de créer des endpoints API séparés) et s'intègrent bien avec les formulaires React/Next.js. Utiliser les **API Routes** pour des besoins spécifiques (webhooks, points d'accès pour des scripts externes si jamais nécessaire).
- **Séparation de la Logique Métier (Service Layer / Use Cases - Bonne Pratique) :**
    - Créer des fonctions/classes dédiées dans `lib/services/` ou `lib/actions/` qui encapsulent la logique métier (ex: `createApplicationWithInitialStatus`, `updateCandidateStatusInOpening`).
    - Les Server Actions ou API Routes appellent ces services. Cela rend les route handlers/actions plus légers et la logique métier testable indépendamment.
- **Validation :** Utiliser **Zod** pour définir des schémas de validation pour les entrées des Server Actions et des API Routes. Assure la robustesse et la sécurité des données avant de toucher à la base de données. S'intègre bien avec TypeScript et `react-hook-form`.
- **Gestion des Erreurs :** Stratégie cohérente pour la gestion des erreurs (ex: erreurs de validation, erreurs base de données, erreurs de permissions). Retourner des réponses d'erreur claires depuis les Server Actions / API Routes pour affichage dans le frontend.

## 6. Architecture de la Couche Données

- **ORM : Prisma :**
    - `prisma/schema.prisma` : Source unique de vérité pour le schéma de la base de données.
    - `prisma migrate dev` : Gestion des migrations de base de données de manière fiable.
    - `PrismaClient` : Client typé pour interagir avec la base de données de manière sûre et efficace depuis le backend (Server Components, Server Actions, API Routes). Instance unique du client recommandée (`lib/prisma.ts`).
- **Base de Données : PostgreSQL :** Choix standard, fiable et performant, bien supporté par Prisma et adapté à la croissance future.
- **(Optionnel) Repository Pattern :** Pour des applications plus complexes, on pourrait introduire une couche Repository (`lib/repositories/`) qui abstrait totalement l'utilisation de Prisma depuis les services. Pour ce MVP, l'utilisation directe de Prisma dans les services est probablement suffisante et plus simple.

## 7. Authentification & Autorisation

- **Provider : NextAuth.js :** Solution complète et bien intégrée.
- **Stratégies :** Configurer le provider "Credentials" (Email/Password) pour l'authentification locale. Possibilité d'ajouter facilement les providers OAuth (Google, Microsoft) plus tard via la configuration de NextAuth.
- **Gestion de Session :** Utilisation des sessions JWT (par défaut, stateless) ou Database Sessions (via un adaptateur Prisma pour NextAuth si persistance en base préférée).
- **Contrôle d'Accès Basé sur les Rôles (RBAC) :**
    - Ajouter un champ `role` ('CANDIDATE' | 'RECRUITER') au modèle `User` dans Prisma.
    - Protéger les pages/routes de l'App Router via des vérifications dans les layouts ou les pages elles-mêmes (en récupérant la session serveur via NextAuth).
    - Protéger les Server Actions / API Routes en vérifiant le rôle de l'utilisateur authentifié avant d'autoriser l'action.
    - S'assurer que les requêtes Prisma filtrent bien les données par `userId` (pour les candidats) ou par appartenance à une "organisation/équipe" (pour les recruteurs, si ce concept est ajouté).

## 8. Modularité & Évolutivité

- **Structure de Dossiers :** L'organisation par fonctionnalités (`features`) dans `components` et la séparation logique dans `lib` (services, repositories...) facilitent l'ajout/modification de modules.
- **Composants Réutilisables :** Forte utilisation de composants React bien définis.
- **TypeScript :** Le typage statique améliore la maintenabilité et réduit les erreurs lors de l'évolution du code.
- **Prisma Migrations :** Permet de faire évoluer le schéma de la base de données de manière contrôlée.
- **Découplage Backend/Data :** L'utilisation de services/repositories (même simples) aide à découpler la logique métier de l'accès aux données.

## 9. Architecture de Déploiement (Docker)

- **`Dockerfile` :**
    - Multi-stage build pour optimiser la taille de l'image finale.
    - Stage 1 : Installation des dépendances (`npm install`), génération du client Prisma (`npx prisma generate`), build de l'application Next.js (`npm run build`).
    - Stage 2 : Image Node.js légère, copie des artefacts de build (`.next`, `node_modules`, `public`, `package.json`), exécution de l'application (`npm start`).
- **`docker-compose.yml` :**
    - Définition de deux services :
        - `app` : Basé sur le `Dockerfile` de l'application CandiFlow. Expose le port 3000. Dépend de `db`.
        - `db` : Image officielle `postgres`, configuration des volumes pour la persistance des données, définition des variables d'environnement (utilisateur, mot de passe, nom de la base).
- **Variables d'Environnement :** Utilisation de fichiers `.env` (et `.env.local`) gérés par Next.js pour la configuration (URL de la base de données, secrets NextAuth, etc.). Passage de ces variables au conteneur Docker via `docker-compose.yml`.

## 10. Design Patterns Applicables

- **Component-Based Architecture :** Fondamental avec React/Next.js.
- **Server Components / Client Components :** Pattern clé de Next.js App Router.
- **Service Layer / Use Case :** Pour la logique métier.
- **Repository  :** Pour l'abstraction de l'accès aux données.
- **Provider Pattern (React Context) :** Pour la gestion d'état global simple si nécessaire.
- **Middleware :** Pour l'authentification/autorisation ou la logique transversale dans Next.js.
