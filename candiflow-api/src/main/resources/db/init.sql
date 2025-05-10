-- Création du type enum user_role s'il n'existe pas déjà
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('ADMIN', 'CANDIDATE', 'RECRUITER');
    END IF;
END
$$;
