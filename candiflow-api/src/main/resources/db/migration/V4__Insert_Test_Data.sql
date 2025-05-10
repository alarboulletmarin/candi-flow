-- V4__Insert_Test_Data.sql
-- Insertion de données de test pour CandiFlow

DO $$
BEGIN

-- Insertion d'utilisateurs de test
IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
    INSERT INTO users (user_id, email, password_hash, role, name, created_at, updated_at)
    VALUES
        -- Mot de passe: 'password123' (bcrypt hash)
        ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin@candiflow.com', '$2a$10$rRyBsGSHK6.uc8fntPwVIuLVHgsAhAX7TcdrqW/RADU0uh7CaChLa', 'ADMIN', 'Admin User', NOW(), NOW()),
        ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'candidate1@example.com', '$2a$10$rRyBsGSHK6.uc8fntPwVIuLVHgsAhAX7TcdrqW/RADU0uh7CaChLa', 'CANDIDATE', 'Sophie Martin', NOW(), NOW()),
        ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'candidate2@example.com', '$2a$10$rRyBsGSHK6.uc8fntPwVIuLVHgsAhAX7TcdrqW/RADU0uh7CaChLa', 'CANDIDATE', 'Thomas Dubois', NOW(), NOW()),
        ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'recruiter1@example.com', '$2a$10$rRyBsGSHK6.uc8fntPwVIuLVHgsAhAX7TcdrqW/RADU0uh7CaChLa', 'RECRUITER', 'Emma Lefebvre', NOW(), NOW()),
        ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'recruiter2@example.com', '$2a$10$rRyBsGSHK6.uc8fntPwVIuLVHgsAhAX7TcdrqW/RADU0uh7CaChLa', 'RECRUITER', 'Lucas Bernard', NOW(), NOW())
    ON CONFLICT (email) DO NOTHING;
END IF;

-- Insertion de tags
IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tags') THEN
    INSERT INTO tags (tag_id, name, created_at, updated_at)
    VALUES
        ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'Urgent', NOW(), NOW()),
        ('f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'Remote', NOW(), NOW()),
        ('f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'Tech', NOW(), NOW()),
        ('f3eebc99-9c0b-4ef8-bb6d-6bb9bd380a99', 'Finance', NOW(), NOW()),
        ('f4eebc99-9c0b-4ef8-bb6d-6bb9bd380aaa', 'Marketing', NOW(), NOW()),
        ('f5eebc99-9c0b-4ef8-bb6d-6bb9bd380abb', 'Senior', NOW(), NOW()),
        ('f6eebc99-9c0b-4ef8-bb6d-6bb9bd380acc', 'Junior', NOW(), NOW()),
        ('f7eebc99-9c0b-4ef8-bb6d-6bb9bd380add', 'Favorite', NOW(), NOW())
    ON CONFLICT (name) DO NOTHING;
END IF;

-- Insertion de candidatures pour le candidat 1
IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'applications') THEN
    INSERT INTO applications (application_id, user_id, company_name, job_title, job_url, date_applied, follow_up_date, general_notes, created_at, updated_at)
    VALUES
        ('a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Google', 'Développeur Full Stack', 'https://careers.google.com/jobs/123', NOW() - INTERVAL '30 days', NOW() + INTERVAL '5 days', 'Entretien technique prévu pour la semaine prochaine', NOW(), NOW()),
        ('a2eebc99-9c0b-4ef8-bb6d-6bb9bd380b22', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Amazon', 'Software Engineer', 'https://amazon.jobs/456', NOW() - INTERVAL '20 days', NOW() + INTERVAL '10 days', 'Premier entretien téléphonique passé avec succès', NOW(), NOW()),
        ('a3eebc99-9c0b-4ef8-bb6d-6bb9bd380b33', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Microsoft', 'Frontend Developer', 'https://careers.microsoft.com/789', NOW() - INTERVAL '15 days', NOW() + INTERVAL '7 days', 'Attente de retour après envoi du CV', NOW(), NOW());

    -- Insertion de candidatures pour le candidat 2
    INSERT INTO applications (application_id, user_id, company_name, job_title, job_url, date_applied, follow_up_date, general_notes, created_at, updated_at)
    VALUES
        ('a4eebc99-9c0b-4ef8-bb6d-6bb9bd380b44', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Apple', 'iOS Developer', 'https://jobs.apple.com/123', NOW() - INTERVAL '25 days', NOW() + INTERVAL '3 days', 'Deuxième entretien prévu', NOW(), NOW()),
        ('a5eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Facebook', 'React Developer', 'https://facebook.careers/456', NOW() - INTERVAL '10 days', NOW() + INTERVAL '15 days', 'Test technique à compléter', NOW(), NOW());
END IF;

-- Insertion de mises à jour de statut pour les candidatures
IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'status_updates') AND 
   EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'application_statuses') THEN
    INSERT INTO status_updates (status_update_id, application_id, status_id, event_date, notes, created_at, updated_at)
    VALUES
        -- Pour Google (candidat 1)
        ('s1eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 
         (SELECT status_id FROM application_statuses WHERE name = 'APPLIED'), 
         NOW() - INTERVAL '30 days', 'Candidature soumise via le site web', NOW(), NOW()),
        
        ('s2eebc99-9c0b-4ef8-bb6d-6bb9bd380c22', 'a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 
         (SELECT status_id FROM application_statuses WHERE name = 'SCREENING'), 
         NOW() - INTERVAL '25 days', 'Email de confirmation reçu', NOW(), NOW()),
        
        ('s3eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', 'a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 
         (SELECT status_id FROM application_statuses WHERE name = 'INTERVIEW'), 
         NOW() - INTERVAL '20 days', 'Entretien téléphonique de 30 minutes', NOW(), NOW()),
        
        -- Pour Amazon (candidat 1)
        ('s4eebc99-9c0b-4ef8-bb6d-6bb9bd380c44', 'a2eebc99-9c0b-4ef8-bb6d-6bb9bd380b22', 
         (SELECT status_id FROM application_statuses WHERE name = 'APPLIED'), 
         NOW() - INTERVAL '20 days', 'Candidature soumise via LinkedIn', NOW(), NOW()),
        
        ('s5eebc99-9c0b-4ef8-bb6d-6bb9bd380c55', 'a2eebc99-9c0b-4ef8-bb6d-6bb9bd380b22', 
         (SELECT status_id FROM application_statuses WHERE name = 'SCREENING'), 
         NOW() - INTERVAL '15 days', 'Screening call avec RH', NOW(), NOW()),
        
        -- Pour Microsoft (candidat 1)
        ('s6eebc99-9c0b-4ef8-bb6d-6bb9bd380c66', 'a3eebc99-9c0b-4ef8-bb6d-6bb9bd380b33', 
         (SELECT status_id FROM application_statuses WHERE name = 'APPLIED'), 
         NOW() - INTERVAL '15 days', 'Candidature soumise', NOW(), NOW()),
        
        -- Pour Apple (candidat 2)
        ('s7eebc99-9c0b-4ef8-bb6d-6bb9bd380c77', 'a4eebc99-9c0b-4ef8-bb6d-6bb9bd380b44', 
         (SELECT status_id FROM application_statuses WHERE name = 'APPLIED'), 
         NOW() - INTERVAL '25 days', 'Candidature soumise', NOW(), NOW()),
        
        ('s8eebc99-9c0b-4ef8-bb6d-6bb9bd380c88', 'a4eebc99-9c0b-4ef8-bb6d-6bb9bd380b44', 
         (SELECT status_id FROM application_statuses WHERE name = 'SCREENING'), 
         NOW() - INTERVAL '20 days', 'Screening initial', NOW(), NOW()),
        
        ('s9eebc99-9c0b-4ef8-bb6d-6bb9bd380c99', 'a4eebc99-9c0b-4ef8-bb6d-6bb9bd380b44', 
         (SELECT status_id FROM application_statuses WHERE name = 'INTERVIEW'), 
         NOW() - INTERVAL '15 days', 'Premier entretien technique', NOW(), NOW()),
        
        ('s10ebc99-9c0b-4ef8-bb6d-6bb9bd380caa', 'a4eebc99-9c0b-4ef8-bb6d-6bb9bd380b44', 
         (SELECT status_id FROM application_statuses WHERE name = 'OFFER'), 
         NOW() - INTERVAL '5 days', 'Offre reçue: 75K€', NOW(), NOW()),
        
        -- Pour Facebook (candidat 2)
        ('s11ebc99-9c0b-4ef8-bb6d-6bb9bd380cbb', 'a5eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 
         (SELECT status_id FROM application_statuses WHERE name = 'APPLIED'), 
         NOW() - INTERVAL '10 days', 'Candidature soumise', NOW(), NOW());
END IF;

-- Insertion de tags pour les candidatures
IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'application_tags') THEN
    INSERT INTO application_tags (application_id, tag_id)
    VALUES
        ('a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88'), -- Google - Tech
        ('a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'f7eebc99-9c0b-4ef8-bb6d-6bb9bd380add'), -- Google - Favorite
        ('a2eebc99-9c0b-4ef8-bb6d-6bb9bd380b22', 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88'), -- Amazon - Tech
        ('a2eebc99-9c0b-4ef8-bb6d-6bb9bd380b22', 'f5eebc99-9c0b-4ef8-bb6d-6bb9bd380abb'), -- Amazon - Senior
        ('a3eebc99-9c0b-4ef8-bb6d-6bb9bd380b33', 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88'), -- Microsoft - Tech
        ('a3eebc99-9c0b-4ef8-bb6d-6bb9bd380b33', 'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a77'), -- Microsoft - Remote
        ('a4eebc99-9c0b-4ef8-bb6d-6bb9bd380b44', 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88'), -- Apple - Tech
        ('a4eebc99-9c0b-4ef8-bb6d-6bb9bd380b44', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66'), -- Apple - Urgent
        ('a5eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88'), -- Facebook - Tech
        ('a5eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 'f6eebc99-9c0b-4ef8-bb6d-6bb9bd380acc'); -- Facebook - Junior
END IF;

-- Insertion d'offres d'emploi pour les recruteurs
IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'job_openings') THEN
    INSERT INTO job_openings (job_opening_id, recruiter_user_id, title, description, status, created_at, updated_at)
    VALUES
        ('j1eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'Développeur Java Senior', 'Nous recherchons un développeur Java expérimenté pour rejoindre notre équipe backend.', 'OPEN', NOW(), NOW()),
        ('j2eebc99-9c0b-4ef8-bb6d-6bb9bd380d22', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'UX/UI Designer', 'Poste de designer UX/UI pour notre application mobile.', 'OPEN', NOW(), NOW()),
        ('j3eebc99-9c0b-4ef8-bb6d-6bb9bd380d33', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'DevOps Engineer', 'Nous recherchons un ingénieur DevOps pour améliorer notre pipeline CI/CD.', 'OPEN', NOW(), NOW());
END IF;

-- Insertion de candidats pour les offres d'emploi
IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'opening_applicants') AND
   EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pipeline_stages') AND
   EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'candidate_sources') THEN
    INSERT INTO opening_applicants (applicant_id, job_opening_id, current_stage_id, source_id, name, email, phone, application_date, created_at, updated_at)
    VALUES
        ('o1eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'j1eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 
         (SELECT stage_id FROM pipeline_stages WHERE name = 'TECHNICAL_TEST'), 
         (SELECT source_id FROM candidate_sources WHERE name = 'LINKEDIN'),
         'Jean Dupont', 'jean.dupont@example.com', '+33612345678', NOW() - INTERVAL '15 days', NOW(), NOW()),
        
        ('o2eebc99-9c0b-4ef8-bb6d-6bb9bd380e22', 'j1eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 
         (SELECT stage_id FROM pipeline_stages WHERE name = 'CV_SCREENING'), 
         (SELECT source_id FROM candidate_sources WHERE name = 'INDEED'),
         'Marie Lambert', 'marie.lambert@example.com', '+33623456789', NOW() - INTERVAL '10 days', NOW(), NOW()),
        
        ('o3eebc99-9c0b-4ef8-bb6d-6bb9bd380e33', 'j2eebc99-9c0b-4ef8-bb6d-6bb9bd380d22', 
         (SELECT stage_id FROM pipeline_stages WHERE name = 'PHONE_INTERVIEW'), 
         (SELECT source_id FROM candidate_sources WHERE name = 'DIRECT_APPLICATION'),
         'Pierre Moreau', 'pierre.moreau@example.com', '+33634567890', NOW() - INTERVAL '12 days', NOW(), NOW()),
        
        ('o4eebc99-9c0b-4ef8-bb6d-6bb9bd380e44', 'j3eebc99-9c0b-4ef8-bb6d-6bb9bd380d33', 
         (SELECT stage_id FROM pipeline_stages WHERE name = 'ONSITE_INTERVIEW'), 
         (SELECT source_id FROM candidate_sources WHERE name = 'REFERRAL'),
         'Lucie Petit', 'lucie.petit@example.com', '+33645678901', NOW() - INTERVAL '20 days', NOW(), NOW());
END IF;

-- Insertion de notes de recruteur
IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'recruiter_notes') THEN
    INSERT INTO recruiter_notes (note_id, applicant_id, author_user_id, note_text, created_at, updated_at)
    VALUES
        ('n1eebc99-9c0b-4ef8-bb6d-6bb9bd380f11', 'o1eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'Candidat très prometteur avec 8 ans d''expérience en Java. A travaillé sur des projets similaires.', NOW(), NOW()),
        ('n2eebc99-9c0b-4ef8-bb6d-6bb9bd380f22', 'o3eebc99-9c0b-4ef8-bb6d-6bb9bd380e33', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'Portfolio impressionnant. À considérer pour le poste senior.', NOW(), NOW()),
        ('n3eebc99-9c0b-4ef8-bb6d-6bb9bd380f33', 'o4eebc99-9c0b-4ef8-bb6d-6bb9bd380e44', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'Excellente expérience en Kubernetes et AWS. Recommandé par l''équipe technique.', NOW(), NOW());
END IF;

-- Insertion de tags pour les candidats
IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'applicant_tags') THEN
    INSERT INTO applicant_tags (applicant_id, tag_id)
    VALUES
        ('o1eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88'), -- Jean - Tech
        ('o1eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'f5eebc99-9c0b-4ef8-bb6d-6bb9bd380abb'), -- Jean - Senior
        ('o2eebc99-9c0b-4ef8-bb6d-6bb9bd380e22', 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88'), -- Marie - Tech
        ('o3eebc99-9c0b-4ef8-bb6d-6bb9bd380e33', 'f4eebc99-9c0b-4ef8-bb6d-6bb9bd380aaa'), -- Pierre - Marketing
        ('o4eebc99-9c0b-4ef8-bb6d-6bb9bd380e44', 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88'), -- Lucie - Tech
        ('o4eebc99-9c0b-4ef8-bb6d-6bb9bd380e44', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66'); -- Lucie - Urgent
END IF;

END
$$;
