-- V3__Init_Default_Pipeline_Stages_And_Sources.sql
-- Initialisation des étapes du pipeline et des sources de candidats par défaut

DO $$
BEGIN
    -- Insertion des étapes du pipeline par défaut
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pipeline_stages') THEN
        INSERT INTO pipeline_stages (name, description, display_order, is_end_stage, is_active)
        VALUES
            ('CV_SCREENING', 'Analyse du CV', 1, false, true),
            ('PHONE_INTERVIEW', 'Entretien téléphonique', 2, false, true),
            ('TECHNICAL_TEST', 'Test technique', 3, false, true),
            ('ONSITE_INTERVIEW', 'Entretien sur site', 4, false, true),
            ('FINAL_DECISION', 'Décision finale', 5, false, true),
            ('OFFER_SENT', 'Offre envoyée', 6, false, true),
            ('HIRED', 'Candidat embauché', 7, true, true),
            ('REJECTED', 'Candidature rejetée', 8, true, true)
        ON CONFLICT (name) DO NOTHING;
    END IF;

    -- Insertion des sources de candidats par défaut
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'candidate_sources') THEN
        INSERT INTO candidate_sources (name, description, is_active)
        VALUES
            ('LINKEDIN', 'Candidats provenant de LinkedIn', true),
            ('INDEED', 'Candidats provenant de Indeed', true),
            ('REFERRAL', 'Candidats recommandés par des employés', true),
            ('DIRECT_APPLICATION', 'Candidatures directes sur le site web', true),
            ('JOB_FAIR', 'Salons de l''emploi', true),
            ('UNIVERSITY', 'Partenariats universitaires', true),
            ('OTHER', 'Autres sources', true)
        ON CONFLICT (name) DO NOTHING;
    END IF;
END
$$;
