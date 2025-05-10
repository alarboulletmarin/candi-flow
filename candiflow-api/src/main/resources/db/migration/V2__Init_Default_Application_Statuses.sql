-- V2__Init_Default_Application_Statuses.sql
-- Initialisation des statuts d'application par défaut

-- Insertion des statuts d'application par défaut
-- Vérification que la table existe avant d'insérer des données
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'application_statuses') THEN
        INSERT INTO application_statuses (name, description, display_order, icon_name, is_active)
        VALUES
            ('APPLIED', 'Candidature soumise', 1, 'send', true),
            ('SCREENING', 'Première évaluation', 2, 'search', true),
            ('INTERVIEW', 'Entretien programmé', 3, 'calendar', true),
            ('OFFER', 'Offre reçue', 4, 'star', true),
            ('REJECTED', 'Candidature rejetée', 5, 'x-circle', true),
            ('ACCEPTED', 'Offre acceptée', 6, 'check-circle', true),
            ('DECLINED', 'Offre déclinée', 7, 'x-circle', true)
        ON CONFLICT (name) DO NOTHING;
    END IF;
END
$$;
