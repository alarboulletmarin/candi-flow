# 1. Vision & Objectifs du Projet

**Vision :** Créer **CandiFlow**, une application web **simple, épurée et moderne**, conçue pour être **exclusivement auto-hébergée** (via Docker). Elle vise à offrir aux **candidats** une expérience agréable et détaillée pour suivre leurs candidatures, et aux **recruteurs/équipes RH** un outil interne efficace pour gérer leur pipeline de recrutement.

**Objectifs Clés :**

- **Pour les Candidats :** Remplacer les méthodes manuelles (Excel, fichiers locaux) par une interface centralisée offrant une vue chronologique claire de chaque processus de candidature.
- **Pour les Recruteurs (Usage Interne) :** Fournir un outil simple et auto-hébergé pour suivre les candidats associés à des offres d'emploi spécifiques au sein de leur organisation.
- **Modèle :** Application **100% gratuite et open source**, destinée à l'auto-hébergement.
- **Expérience Utilisateur :** Priorité absolue à une interface **intuitive, agréable visuellement**, et **attentive aux détails** qui facilitent réellement le suivi.

## 2. Nom de l'Application

- **Nom choisi :** `CandiFlow` ( → Focus sur le flux du processus)

## 3. Stack Technologique

- **Framework :** **Next.js** (avec TypeScript)
- **Styling :** **Tailwind CSS** (pour un design sur mesure et épuré)
- **Base de Données :** **PostgreSQL** (robustesse pour les données chronologiques)
- **ORM :** **Prisma** (sécurité des types, migrations faciles)
- **Conteneurisation :** **Docker** & **Docker Compose** (pour un auto-hébergement simplifié)
- **Authentification :** **NextAuth.js** (pour la gestion des comptes utilisateurs locaux)

## 4. Architecture & Expérience Utilisateur Détaillée

L'architecture modulaire avec Next.js/Prisma est maintenue. L'accent est mis sur l'UX :

- **Interface Épurée :** Utilisation de Tailwind CSS, bonne typographie, espaces blancs généreux, palette de couleurs limitée et cohérente. Peut-être s'inspirer de designs minimalistes.
- **Micro-interactions & Feedback :** Transitions douces (CSS/Framer Motion), indicateurs de chargement (skeletons/spinners), notifications "toast" claires pour les actions (ex: "Candidature enregistrée").
- **Attention aux Détails :** États vides informatifs, facilité de copier/coller des infos, pré-remplissage des dates, peut-être des raccourcis clavier de base. Mode sombre à considérer pour plus tard.
- **Modèle de Données Affiné (Statut) :**
    - Une table `Application` (infos générales : entreprise, poste, url...).
    - Une table `StatusUpdate` liée à `Application` avec : `id`, `applicationId`, `status` (texte/enum : Postulé, Entretien RH, Test Technique...), `eventDate` (date et heure), `notes` (texte optionnel spécifique à cette étape).
- **Composants Clés :**
    - `ApplicationList` / `ApplicationCard` : Affichage clair en liste ou grille.
    - `StatusTimeline` : **LE composant clé pour la vue candidat.** Affiche les `StatusUpdate` triés par date sous forme de frise verticale (ex: points sur une ligne, icônes, dates claires, notes associées visibles).
    - `AddStatusUpdateForm` : Modal/formulaire simple pour ajouter une nouvelle étape à la timeline.
    - `DocumentManager` : Interface simple pour lier/uploader des fichiers à une application.
    - `OpeningList` / `CandidatePipeline` (pour recruteurs) : Vues spécifiques au rôle recruteur.

## 5. Fonctionnalités du MVP (Minimum Viable Product) - Enrichi "Expérience"

**Fonctionnalités Communes :**

- **Landing Page Simple :** Présentation de CandiFlow, claire et directe.
- **Authentification Utilisateur :** Création de compte local (Email/Mot de passe) simple et sécurisée via NextAuth.js. Gestion de session fluide (l'utilisateur reste connecté longtemps). Définition du rôle (Candidat/Recruteur).

**Fonctionnalités MVP - Vue Candidat (Focus sur l'Expérience ++) :**

- **Dashboard / Liste (`ApplicationList` / `ApplicationCard`) :**
    - Affichage clair des candidatures (Carte ou Ligne).
    - **Badges de Statut Visuels :** Couleurs distinctes pour chaque statut actuel (ex: Bleu pour "En cours", Vert pour "Entretien", Rouge pour "Refusé") pour une lecture rapide.
    - **Indicateur "Prochaine Action" :** Mise en évidence visuelle (ex: icône calendrier, point de couleur) pour les candidatures dont la date de "Prochaine action / Relance" est aujourd'hui ou passée.
    - **Tri/Filtre Réactifs :** Tri par date, entreprise, statut. Filtre par statut instantané (côté client sur les données chargées pour plus de fluidité).
    - *(Optionnel - à évaluer pour MVP)* : Menu "..." sur chaque item pour actions rapides (ex: "Ajouter une étape", "Marquer comme refusé").
- **Ajout Candidature (`AddApplicationForm`) :**
    - Formulaire épuré et guidé.
    - **Pré-remplissage Intelligent :** Date de candidature à aujourd'hui par défaut. Statut initial "Postulé".
    - **Récupération Titre depuis URL (Optionnel mais appréciable) :** Tenter de récupérer le titre de la page (`<title>`) depuis l'URL de l'annonce collée pour pré-remplir le champ "Intitulé du Poste".
    - **Validation Claire :** Utilisation de `react-hook-form` + `zod` pour une validation des champs en temps réel et des messages d'erreur précis.
- **Vue Détail Candidature (`ApplicationDetailView`) :**
    - Infos générales modifiables (formulaire d'édition simple).
    - **Frise Chronologique (StatusTimeline) Améliorée :**
        - Affichage vertical clair des étapes (`StatusUpdate`).
        - **Icônes Pertinentes :** Icône distincte pour chaque type de statut (Postulé, Entretien Tel, Entretien Physique, Test, Offre, Refus...).
        - **Dates Relatives & Absolues :** Afficher "16/04/2025" et "(Il y a 3 jours)" avec `date-fns` ou `day.js`.
        - Notes spécifiques à chaque étape clairement visibles.
    - **Ajout d'Étape Intuitif (`AddStatusUpdateForm`) :** Bouton "+ Ajouter une étape" bien visible, ouvrant une modale/section simple pour choisir le statut, la date (par défaut aujourd'hui) et ajouter une note.
    - **Section Notes Générales :** Éditeur de texte simple (Markdown de base ?).
    - **Section Documents (`DocumentManager`) :**
        - Upload simple via glisser-déposer ou bouton.
        - Affichage du nom de fichier et d'une icône de type de fichier (PDF, DOCX...).
        - Lien pour ouvrir/télécharger. Suppression facile.
    - **Champ "Prochaine action / Relance" :** Sélecteur de date clair, petite zone de texte pour la note de relance.
    - **Boutons "Copier" :** Pour copier facilement l'URL de l'annonce ou l'email du contact noté.

**Fonctionnalités MVP - Vue Recruteur (Suivi Interne Efficace & Agréable) :**

- **Dashboard / Gestion des Offres d'Emploi (`OpeningList`) :**
    - Affichage clair des offres (Titre du poste, Département/Équipe si pertinent, Statut).
    - **Indicateurs Visuels Clés :**
        - Badge de statut distinct ("Ouvert" / "Fermé").
        - **Compteur de Candidats Actifs :** Afficher directement sur la ligne/carte de l'offre le nombre de candidats actuellement dans le pipeline (ex: "Développeur Frontend (7 actifs)").
        - Date de dernière activité sur l'offre ou ses candidats.
    - **Création/Archivage Simplifiés :** Bouton "+ Créer une offre" clair. Possibilité de marquer une offre comme "Fermée" (archive) facilement pour la masquer de la vue par défaut.
    - **Filtres Rapides :** Filtrer les offres par statut (Ouvertes / Fermées).
- **Vue Détail d'une Offre / Pipeline Candidats (`CandidatePipeline`) :**
    - **Vue Principale : Tableau Kanban Visuel :**
        - Colonnes représentant les étapes clés du processus de recrutement (personnalisables ? Pour le MVP, des colonnes fixes : "Nouveau", "Présélection", "Entretien RH", "Entretien Tech", "Test Pratique", "Offre", "Recruté", "Non Retenu").
        - **Cartes Candidat (`CandidateCard`) :** Cartes claires pour chaque candidat affichant : Nom, peut-être la source (si ajoutée), date d'entrée dans l'étape actuelle.
        - **Drag-and-Drop Intuitif :** **Glisser-déposer** une carte candidat d'une colonne à l'autre pour mettre à jour son statut instantanément. Feedback visuel lors du déplacement et de la confirmation.
    - **(Optionnel) Vue Liste Alternative :** Un bouton pour basculer entre la vue Kanban et une vue Liste simple des candidats (triable par nom, statut, date...).
    - **Ajout Rapide de Candidat :** Bouton "+ Ajouter Candidat" sur la page de l'offre, ouvrant un formulaire simple (Nom, Email, Téléphone ?, Source ?, upload CV/LM).
    - **Accès Rapide aux Infos Candidat :** Cliquer sur une carte/ligne ouvre une modale/un panneau latéral avec les détails du candidat (CV/LM consultables, historique des statuts *pour cette offre*, notes).
- **Gestion Simplifiée des Candidats :**
    - **Upload de Fichiers Clair :** Uploader CV/LM lors de l'ajout, possibilité d'en ajouter/supprimer depuis la vue détail du candidat. Prévisualisation simple (icône type de fichier, lien d'ouverture).
    - **Détection Basique de Doublons (par email) :** Lors de l'ajout d'un email, vérifier s'il existe déjà *pour cette offre* ou dans les offres *ouvertes* et afficher un avertissement discret.
- **Notes Contextuelles et Suivi :**
    - Section "Notes" sur la fiche candidat (dans le contexte de l'offre).
    - **Notes Horodatées et Attribuées :** Chaque note enregistre automatiquement la date/heure et le recruteur connecté qui l'a ajoutée (important si plusieurs recruteurs utilisent la même instance). Format simple : `[AAAA-MM-JJ HH:MM - Nom Recruteur] Note...`

## 6. Exigences Non-Fonctionnelles Clés

- **Auto-Hébergement Uniquement** via Docker.
- **100% Gratuit.**
- **Interface & UX Prioritaires :** Simple, épurée, agréable, détaillée.
- **Sécurité** des données et de l'authentification.
- **Performance** (Next.js et Prisma y aident).
- **Responsive Design** de base (utilisable sur différentes tailles d'écran).

## 7. Évolutions Possibles (Post-MVP)

- **Amélioration de la Frise :** Icônes, couleurs par statut, visualisation plus graphique.
- **Notifications/Rappels :** Pour les dates de relance.
- **Recherche/Filtrage Avancé :** Recherche plein texte dans les notes, filtres combinés.
- **Statistiques Candidat :** Taux de succès par type de poste/entreprise, durée moyenne des processus.
- **Personnalisation :** Statuts personnalisables par l'utilisateur.
- **Améliorations Recruteur :** Timeline pour les candidats côté recruteur, collaboration multi-recruteurs sur une instance.
- **Import/Export CSV.**

## 8. Prochaines Étapes Suggérées

1. **Confirmer le nom :** `CandiFlow` (Validé).
2. **Mettre en place le projet Next.js** + TypeScript + Tailwind.
3. **Configurer Prisma** avec le schéma affiné (tables `Application` et `StatusUpdate`).
4. **Implémenter l'authentification** de base (local).
5. **Construire le composant `StatusTimeline`** - C'est le cœur de l'expérience candidat !
6. **Développer les vues Candidat** (Liste, Ajout, Détail avec la timeline).
7. **Mettre en place Docker** pour l'environnement de dev (Next.js + Postgres).
8. Itérer et affiner l'UX/UI.
