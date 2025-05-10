## 1. Introduction

Ce document présente le modèle logique de la base de données PostgreSQL pour `CandiFlow`, représenté sous forme de diagramme Entité-Relation (ERD) avec la syntaxe Mermaid correcte.

## 2. Diagramme Entité-Relation (Mermaid ER Diagram)

```mermaid
erDiagram
    %% -- Core Entities & User Links --
    USERS ||--o{ APPLICATIONS : "candidate_tracks"
    USERS ||--o{ JOB_OPENINGS : "recruiter_creates"
    USERS ||--o{ RECRUITER_NOTES : "recruiter_writes"

    %% -- Candidate Application Tracking --
    APPLICATIONS ||--|{ STATUS_UPDATES : "has_history"
    APPLICATIONS ||--o{ DOCUMENTS : "has_documents"
    APPLICATIONS ||--o{ APPLICATION_TAGS : "is_tagged_with"

    %% -- Recruiter Job Opening Tracking --
    JOB_OPENINGS ||--|{ OPENING_APPLICANTS : "has_applicants"
    JOB_OPENINGS }|--|| USERS : "created_by"

    %% -- Opening Applicants Details --
    OPENING_APPLICANTS ||--|{ JOB_OPENINGS : "belongs_to"
    OPENING_APPLICANTS ||--|{ PIPELINE_STAGES : "is_at_stage"
    OPENING_APPLICANTS }o--|| CANDIDATE_SOURCES : "sourced_from (optional)"
    OPENING_APPLICANTS ||--o{ RECRUITER_NOTES : "has_notes"
    OPENING_APPLICANTS ||--o{ APPLICANT_TAGS : "is_tagged_with"

    %% -- Links to Reference Tables --
    STATUS_UPDATES       }|--|| APPLICATIONS : "details_for"
    STATUS_UPDATES       }|--|| APPLICATION_STATUSES : "uses_status"
    DOCUMENTS            }|--|| APPLICATIONS : "attached_to"
    RECRUITER_NOTES      }|--|| OPENING_APPLICANTS : "about_applicant"
    RECRUITER_NOTES      }|--|| USERS : "authored_by"

    %% -- Tagging Many-to-Many --
    APPLICATION_TAGS ||--|| APPLICATIONS : "links_application"
    APPLICATION_TAGS ||--|| TAGS : "uses_tag"
    APPLICANT_TAGS   ||--|| OPENING_APPLICANTS : "links_applicant"
    APPLICANT_TAGS   ||--|| TAGS : "uses_tag"

    %% -- Table Definitions with Keys (Corrected Line Breaks) --
    USERS {
        UUID user_id PK
        VARCHAR email UK, NN
        VARCHAR password_hash NN
        USER_ROLE role ENUM, NN
        VARCHAR name NULL
        TIMESTAMPTZ created_at NN
        TIMESTAMPTZ updated_at NN
    }

    APPLICATIONS {
        UUID application_id PK
        UUID user_id FK, NN
        VARCHAR company_name NN
        VARCHAR job_title NN
        TEXT job_url NULL
        TIMESTAMPTZ date_applied NN
        DATE follow_up_date NULL
        TEXT general_notes NULL
        TIMESTAMPTZ created_at NN
        TIMESTAMPTZ updated_at NN
    }

    APPLICATION_STATUSES {
        UUID status_id PK
        VARCHAR name UK, NN
        TEXT description NULL
        INTEGER display_order NULL
        VARCHAR icon_name NULL
        BOOLEAN is_active NN
    }

    STATUS_UPDATES {
        UUID status_update_id PK
        UUID application_id FK, NN
        UUID status_id FK, NN
        TIMESTAMPTZ event_date NN
        TEXT notes NULL
        TIMESTAMPTZ created_at NN
    }

    DOCUMENTS {
        UUID document_id PK
        UUID application_id FK, NN
        VARCHAR file_name NN
        TEXT storage_path NN
        VARCHAR file_type NULL
        BIGINT file_size NULL
        TIMESTAMPTZ uploaded_at NN
    }

    JOB_OPENINGS {
        UUID job_opening_id PK
        UUID recruiter_user_id FK, NN
        VARCHAR title NN
        TEXT description NULL
        JOB_STATUS status ENUM, NN
        TIMESTAMPTZ created_at NN
        TIMESTAMPTZ updated_at NN
    }

    PIPELINE_STAGES {
        UUID stage_id PK
        VARCHAR name UK, NN
        TEXT description NULL
        INTEGER display_order UK, NN
        BOOLEAN is_end_stage NN
        BOOLEAN is_active NN
    }

    CANDIDATE_SOURCES {
        UUID source_id PK
        VARCHAR name UK, NN
        TEXT description NULL
        BOOLEAN is_active NN
    }

    OPENING_APPLICANTS {
        UUID applicant_id PK
        UUID job_opening_id FK, NN
        UUID current_stage_id FK, NN
        UUID source_id FK, NULL
        VARCHAR name NN
        VARCHAR email NULL
        VARCHAR phone NULL
        TEXT cv_storage_path NULL
        TEXT cover_letter_storage_path NULL
        DATE application_date NULL
        TIMESTAMPTZ created_at NN
        TIMESTAMPTZ updated_at NN
    }

    RECRUITER_NOTES {
        UUID note_id PK
        UUID applicant_id FK, NN
        UUID author_user_id FK, NN
        TEXT note_text NN
        TIMESTAMPTZ created_at NN
    }

    TAGS {
        UUID tag_id PK
        VARCHAR name UK, NN
    }

    APPLICATION_TAGS {
        UUID application_id PK, FK
        UUID tag_id PK, FK
    }

    APPLICANT_TAGS {
        UUID applicant_id PK, FK
        UUID tag_id PK, FK
    }
```
