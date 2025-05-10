Ce document détaille la structure des tables de la base de données PostgreSQL pour CandiFlow, incluant les tables de référence pour une meilleure intégrité et flexibilité.

**Types ENUM  :**

```sql
CREATE TYPE user_role AS ENUM ('CANDIDATE', 'RECRUITER');
CREATE TYPE job_status AS ENUM ('OPEN', 'CLOSED');
```

---

### Table : `users`

**Description :** Stocke les informations d'authentification et le rôle des utilisateurs de CandiFlow.

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `user_id` | `UUID` | `PK`, `NN`, `DEFAULT gen_random_uuid()` | Identifiant unique (UUID) de l'utilisateur. |
| `email` | `VARCHAR(255)` | `UK`, `NN` | Adresse email unique, utilisée pour la connexion. Doit être validée. |
| `password_hash` | `VARCHAR(255)` | `NN` | Hash sécurisé (BCrypt) du mot de passe. **Ne jamais stocker en clair.** |
| `role` | `user_role` | `NN` | Rôle de l'utilisateur (Enum: 'CANDIDATE', 'RECRUITER'). Définit l'interface/accès. |
| `name` | `VARCHAR(255)` | `NULLABLE` | Nom d'affichage de l'utilisateur (pourrait être utilisé pour l'auteur des notes). |
| `created_at` | `TIMESTAMPTZ` | `NN`, `DEFAULT now()` | Date/heure de création (UTC). |
| `updated_at` | `TIMESTAMPTZ` | `NN`, `DEFAULT now()` | Date/heure de dernière modification (UTC) (Trigger recommandé pour màj auto). |

**Index :**

- Index unique sur `email`.

---

### Table : `applications`

**Description :** Stocke les informations sur une candidature spécifique soumise par un utilisateur 'CANDIDATE'.

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `application_id` | `UUID` | `PK`, `NN`, `DEFAULT gen_random_uuid()` | Identifiant unique de la candidature. |
| `user_id` | `UUID` | `FK -> users(user_id) ON DELETE CASCADE`, `NN` | Référence à l'utilisateur (Candidat). Si l'utilisateur est supprimé, ses candidatures le sont aussi. |
| `company_name` | `VARCHAR(255)` | `NN` | Nom de l'entreprise. |
| `job_title` | `VARCHAR(255)` | `NN` | Intitulé du poste visé. |
| `job_url` | `TEXT` | `NULLABLE` | URL de l'annonce d'emploi originale. |
| `date_applied` | `TIMESTAMPTZ` | `NN` | Date/heure auxquelles la candidature a été initialement marquée comme "Postulé". |
| `follow_up_date` | `DATE` | `NULLABLE` | Date cible pour une prochaine action/relance. |
| `general_notes` | `TEXT` | `NULLABLE` | Notes générales libres du candidat sur cette candidature globale. |
| `~~tags~~` | `~~TEXT[]~~` |  | **Supprimé.** Remplacé par la table de jointure `application_tags`. |
| `created_at` | `TIMESTAMPTZ` | `NN`, `DEFAULT now()` | Date/heure de création de l'enregistrement. |
| `updated_at` | `TIMESTAMPTZ` | `NN`, `DEFAULT now()` | Date/heure de dernière modification. |

**Index :**

- Index sur `user_id`.
- Index potentiel sur `company_name`, `job_title`.

---

### Table : `application_statuses`

**Description :** Table de référence pour les différents statuts possibles d'une candidature (pour la timeline candidat).

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `status_id` | `UUID` | `PK`, `NN`, `DEFAULT gen_random_uuid()` | Identifiant unique du statut de candidature. |
| `name` | `VARCHAR(100)` | `UK`, `NN` | Nom affichable du statut (ex: 'Postulé', 'Entretien RH', 'Test Technique'). Unique. |
| `description` | `TEXT` | `NULLABLE` | Description plus détaillée du statut si nécessaire. |
| `display_order` | `INTEGER` | `NULLABLE` | Ordre d'affichage potentiel si les statuts ont une séquence logique par défaut. |
| `icon_name` | `VARCHAR(50)` | `NULLABLE` | Nom d'une icône associée (pour l'UI). |
| `is_active` | `BOOLEAN` | `NN`, `DEFAULT true` | Permet de désactiver un statut sans le supprimer (pour l'historique). |

**Index :**

- Index unique sur `name`.
- Index sur `display_order`.

---

### Table : `status_updates`

**Description :** Historique des étapes et statuts pour une candidature donnée, liée à la table de référence `application_statuses`.

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `status_update_id` | `UUID` | `PK`, `NN`, `DEFAULT gen_random_uuid()` | Identifiant unique de cette mise à jour de statut. |
| `application_id` | `UUID` | `FK -> applications(application_id) ON DELETE CASCADE`, `NN` | Référence à la candidature. |
| `status_id` | `UUID` | `FK -> application_statuses(status_id) ON DELETE RESTRICT`, `NN` | **Modifié.** Référence au statut défini dans la table `application_statuses`. `RESTRICT` empêche de supprimer un statut s'il est utilisé. |
| `event_date` | `TIMESTAMPTZ` | `NN` | Date/heure précises de l'atteinte de ce statut. |
| `notes` | `TEXT` | `NULLABLE` | Notes spécifiques à cette étape. |
| `created_at` | `TIMESTAMPTZ` | `NN`, `DEFAULT now()` | Date/heure de création de cet enregistrement d'étape. |

**Index :**

- Index sur `application_id`.
- Index sur `status_id`.
- Index sur `event_date`.

---

### Table : `documents`

**Description :** Métadonnées des fichiers (CV, LM...) liés à une candidature.

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `document_id` | `UUID` | `PK`, `NN`, `DEFAULT gen_random_uuid()` | Identifiant unique du document. |
| `application_id` | `UUID` | `FK -> applications(application_id) ON DELETE CASCADE`, `NN` | Référence à la candidature. |
| `file_name` | `VARCHAR(255)` | `NN` | Nom original du fichier. |
| `storage_path` | `TEXT` | `NN` | Chemin de stockage relatif ou identifiant. |
| `file_type` | `VARCHAR(100)` | `NULLABLE` | Type MIME du fichier. |
| `file_size` | `BIGINT` | `NULLABLE` | Taille du fichier en octets. |
| `uploaded_at` | `TIMESTAMPTZ` | `NN`, `DEFAULT now()` | Date/heure de l'upload. |

**Index :**

- Index sur `application_id`.

---

### Table : `job_openings`

**Description :** Stocke les informations sur les offres d'emploi gérées par les utilisateurs Recruteurs.

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `job_opening_id` | `UUID` | `PK`, `NN`, `DEFAULT gen_random_uuid()` | Identifiant unique de l'offre. |
| `recruiter_user_id` | `UUID` | `FK -> users(user_id) ON DELETE SET NULL`, `NN` | Référence au recruteur créateur. |
| `title` | `VARCHAR(255)` | `NN` | Intitulé du poste de l'offre. |
| `description` | `TEXT` | `NULLABLE` | Description complète de l'offre. |
| `status` | `job_status` | `NN`, `DEFAULT 'OPEN'` | Statut de l'offre (Enum: 'OPEN', 'CLOSED'). |
| `created_at` | `TIMESTAMPTZ` | `NN`, `DEFAULT now()` | Date/heure de création. |
| `updated_at` | `TIMESTAMPTZ` | `NN`, `DEFAULT now()` | Date/heure de dernière modification. |

**Index :**

- Index sur `recruiter_user_id`.
- Index sur `status`.

---

### Table : `pipeline_stages`

**Description :** Table de référence pour les étapes du pipeline de recrutement (colonnes du Kanban recruteur).

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `stage_id` | `UUID` | `PK`, `NN`, `DEFAULT gen_random_uuid()` | Identifiant unique de l'étape du pipeline. |
| `name` | `VARCHAR(100)` | `UK`, `NN` | Nom affichable de l'étape (ex: 'Nouveau', 'Présélection', 'Entretien RH'). Unique. |
| `description` | `TEXT` | `NULLABLE` | Description de ce que représente cette étape. |
| `display_order` | `INTEGER` | `NN`, `UK` | Ordre d'affichage des colonnes dans le Kanban (0, 1, 2...). Unique pour l'ordre. |
| `is_end_stage` | `BOOLEAN` | `NN`, `DEFAULT false` | Indique si cette étape est une étape finale (ex: 'Recruté', 'Non Retenu'). |
| `is_active` | `BOOLEAN` | `NN`, `DEFAULT true` | Permet de masquer une étape du Kanban sans la supprimer. |

**Index :**

- Index unique sur `name`.
- Index unique sur `display_order`.

---

### Table : `candidate_sources`

**Description :** Table de référence pour les sources des candidatures (pour la vue recruteur).

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `source_id` | `UUID` | `PK`, `NN`, `DEFAULT gen_random_uuid()` | Identifiant unique de la source. |
| `name` | `VARCHAR(100)` | `UK`, `NN` | Nom de la source (ex: 'LinkedIn', 'Site Carrière', 'Cooptation', 'Autre'). Unique. |
| `description` | `TEXT` | `NULLABLE` | Description si nécessaire. |
| `is_active` | `BOOLEAN` | `NN`, `DEFAULT true` | Permet de désactiver une source. |

**Index :**

- Index unique sur `name`.

---

### Table : `opening_applicants`

**Description :** Informations sur un candidat suivi pour une offre d'emploi spécifique, liée aux tables de référence.

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `applicant_id` | `UUID` | `PK`, `NN`, `DEFAULT gen_random_uuid()` | Identifiant unique de cet enregistrement candidat/offre. |
| `job_opening_id` | `UUID` | `FK -> job_openings(job_opening_id) ON DELETE CASCADE`, `NN` | Référence à l'offre. |
| `name` | `VARCHAR(255)` | `NN` | Nom complet du candidat. |
| `email` | `VARCHAR(255)` | `NULLABLE` | Email du candidat. |
| `phone` | `VARCHAR(50)` | `NULLABLE` | Téléphone du candidat. |
| `cv_storage_path` | `TEXT` | `NULLABLE` | Chemin/URL vers le CV. |
| `cover_letter_storage_path` | `TEXT` | `NULLABLE` | Chemin/URL vers la LM. |
| `current_stage_id` | `UUID` | `FK -> pipeline_stages(stage_id) ON DELETE RESTRICT`, `NN` | **Modifié.** Référence à l'étape actuelle dans le pipeline. `RESTRICT` empêche suppression étape si utilisée. |
| `application_date` | `DATE` | `NULLABLE`, `DEFAULT CURRENT_DATE` | Date d'ajout/candidature. |
| `source_id` | `UUID` | `FK -> candidate_sources(source_id) ON DELETE SET NULL`, `NULLABLE` | **Modifié.** Référence à la source. Si source supprimée, devient NULL. |
| `created_at` | `TIMESTAMPTZ` | `NN`, `DEFAULT now()` | Date/heure de création. |
| `updated_at` | `TIMESTAMPTZ` | `NN`, `DEFAULT now()` | Date/heure de dernière modification. |

**Index :**

- Index sur `job_opening_id`.
- Index sur `current_stage_id`.
- Index sur `email`.
- Index sur `source_id`.

---

### Table : `tags`

**Description :** Table de référence stockant tous les tags uniques utilisés dans l'application.

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `tag_id` | `UUID` | `PK`, `NN`, `DEFAULT gen_random_uuid()` | Identifiant unique du tag. |
| `name` | `VARCHAR(100)` | `UK`, `NN` | Le nom du tag (ex: 'React', 'Senior', 'Remote'). Unique. |

**Index :**

- Index unique sur `name`.

---

### Table : `application_tags` (Table de Jointure)

**Description :** Lie les `applications` (candidat) aux `tags` (relation Many-to-Many).

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `application_id` | `UUID` | `PK`, `FK -> applications(application_id) ON DELETE CASCADE`, `NN` | Référence à l'application. |
| `tag_id` | `UUID` | `PK`, `FK -> tags(tag_id) ON DELETE CASCADE`, `NN` | Référence au tag. |

*Note : La clé primaire est composite (`application_id`, `tag_id`).*

**Index :**

- Index sur `tag_id` (pour trouver toutes les applications avec un tag).

---

### Table : `applicant_tags` (Table de Jointure)

**Description :** Lie les `opening_applicants` (recruteur) aux `tags` (relation Many-to-Many).

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `applicant_id` | `UUID` | `PK`, `FK -> opening_applicants(applicant_id) ON DELETE CASCADE`, `NN` | Référence au candidat suivi. |
| `tag_id` | `UUID` | `PK`, `FK -> tags(tag_id) ON DELETE CASCADE`, `NN` | Référence au tag. |

*Note : La clé primaire est composite (`applicant_id`, `tag_id`).*

**Index :**

- Index sur `tag_id` (pour trouver tous les candidats avec un tag).

---

### Table : `recruiter_notes`

**Description :** Stocke les notes prises par les Recruteurs sur les `opening_applicants`.

| Nom Colonne | Type Données (PG) | Contraintes | Description |
| --- | --- | --- | --- |
| `note_id` | `UUID` | `PK`, `NN`, `DEFAULT gen_random_uuid()` | Identifiant unique de la note. |
| `applicant_id` | `UUID` | `FK -> opening_applicants(applicant_id) ON DELETE CASCADE`, `NN` | Référence au candidat suivi concerné. |
| `author_user_id` | `UUID` | `FK -> users(user_id) ON DELETE SET NULL`, `NN` | Référence à l'utilisateur (Recruteur) auteur. |
| `note_text` | `TEXT` | `NN` | Contenu de la note. |
| `created_at` | `TIMESTAMPTZ` | `NN`, `DEFAULT now()` | Date/heure de création de la note. |

**Index :**

- Index sur `applicant_id`.
- Index sur `author_user_id`.

---
