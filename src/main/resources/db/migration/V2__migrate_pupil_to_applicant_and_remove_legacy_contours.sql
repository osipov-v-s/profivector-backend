-- Data-preserving transition from the school domain to the B2B Applicant domain.
-- This migration is intentionally not executed by the repository task.

DO $$
BEGIN
    IF to_regclass('public.pupil') IS NOT NULL
       AND to_regclass('public.applicant') IS NULL THEN
        ALTER TABLE pupil RENAME TO applicant;
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.applicant') IS NOT NULL THEN
        ALTER TABLE applicant ADD COLUMN IF NOT EXISTS target_profession_id BIGINT;
        ALTER TABLE applicant ADD COLUMN IF NOT EXISTS company_id BIGINT;
        ALTER TABLE applicant ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
        ALTER TABLE applicant ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
        ALTER TABLE applicant ALTER COLUMN created_at TYPE TIMESTAMP
            USING created_at::timestamp;
        UPDATE applicant
        SET updated_at = COALESCE(updated_at, created_at, CURRENT_TIMESTAMP);
        ALTER TABLE applicant ALTER COLUMN updated_at SET NOT NULL;

        IF to_regclass('public.profession') IS NOT NULL
           AND NOT EXISTS (
                SELECT 1 FROM pg_constraint
                WHERE conrelid = 'applicant'::regclass
                  AND conname = 'fk_applicant_target_profession') THEN
            ALTER TABLE applicant
                ADD CONSTRAINT fk_applicant_target_profession
                FOREIGN KEY (target_profession_id) REFERENCES profession(id);
        END IF;

        IF to_regclass('public.company') IS NOT NULL
           AND NOT EXISTS (
                SELECT 1 FROM pg_constraint
                WHERE conrelid = 'applicant'::regclass
                  AND conname = 'fk_applicant_company') THEN
            ALTER TABLE applicant
                ADD CONSTRAINT fk_applicant_company
                FOREIGN KEY (company_id) REFERENCES company(id);
        END IF;

        ALTER TABLE applicant DROP COLUMN IF EXISTS school;
        ALTER TABLE applicant DROP COLUMN IF EXISTS health_condition;
        ALTER TABLE applicant DROP COLUMN IF EXISTS nationality;
        ALTER TABLE applicant DROP COLUMN IF EXISTS extra_activities;
        ALTER TABLE applicant DROP COLUMN IF EXISTS class_number;
        ALTER TABLE applicant DROP COLUMN IF EXISTS class_label;
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.psych_test') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public' AND table_name = 'psych_test'
                     AND column_name = 'pupil_id')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema = 'public' AND table_name = 'psych_test'
                         AND column_name = 'applicant_id') THEN
        ALTER TABLE psych_test RENAME COLUMN pupil_id TO applicant_id;
    END IF;

    IF to_regclass('public.prediction') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public' AND table_name = 'prediction'
                     AND column_name = 'pupil_id')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema = 'public' AND table_name = 'prediction'
                         AND column_name = 'applicant_id') THEN
        ALTER TABLE prediction RENAME COLUMN pupil_id TO applicant_id;
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.account') IS NOT NULL THEN
        ALTER TABLE account ADD COLUMN IF NOT EXISTS company_id BIGINT;
        ALTER TABLE account ADD COLUMN IF NOT EXISTS name VARCHAR(50);
        ALTER TABLE account ADD COLUMN IF NOT EXISTS surname VARCHAR(50);
        ALTER TABLE account ADD COLUMN IF NOT EXISTS patronymic VARCHAR(50);

        IF to_regclass('public.company') IS NOT NULL
           AND NOT EXISTS (
                SELECT 1 FROM pg_constraint
                WHERE conrelid = 'account'::regclass
                  AND conname = 'fk_account_company') THEN
            ALTER TABLE account
                ADD CONSTRAINT fk_account_company
                FOREIGN KEY (company_id) REFERENCES company(id);
        END IF;

        IF to_regclass('public.specialist') IS NOT NULL THEN
            UPDATE account a
            SET company_id = COALESCE(a.company_id, s.company_id),
                name = COALESCE(a.name, s.name),
                surname = COALESCE(a.surname, s.surname),
                patronymic = COALESCE(a.patronymic, s.patronymic)
            FROM specialist s
            WHERE s.account_id = a.id;
        END IF;

        IF to_regclass('public.applicant') IS NOT NULL THEN
            UPDATE account a
            SET company_id = COALESCE(a.company_id, ap.company_id),
                name = COALESCE(a.name, ap.name),
                surname = COALESCE(a.surname, ap.surname),
                patronymic = COALESCE(a.patronymic, ap.patronymic)
            FROM applicant ap
            WHERE ap.account_id = a.id;
        END IF;
    END IF;
END $$;

-- Preserve account-role links while replacing the legacy role.
DO $$
DECLARE
    old_role_id BIGINT;
    applicant_role_id BIGINT;
BEGIN
    IF to_regclass('public.role') IS NULL THEN
        RETURN;
    END IF;

    SELECT id INTO old_role_id FROM role WHERE name = 'PUPIL';
    SELECT id INTO applicant_role_id FROM role WHERE name = 'APPLICANT';

    IF old_role_id IS NOT NULL AND applicant_role_id IS NULL THEN
        UPDATE role SET name = 'APPLICANT' WHERE id = old_role_id;
    ELSIF old_role_id IS NOT NULL THEN
        IF to_regclass('public.account_roles') IS NOT NULL THEN
            INSERT INTO account_roles (account_id, role_id)
            SELECT account_id, applicant_role_id
            FROM account_roles
            WHERE role_id = old_role_id
            ON CONFLICT DO NOTHING;
            DELETE FROM account_roles WHERE role_id = old_role_id;
        END IF;
        DELETE FROM role WHERE id = old_role_id;
    END IF;

    IF to_regclass('public.account_roles') IS NOT NULL THEN
        DELETE FROM account_roles
        WHERE role_id IN (
            SELECT id FROM role WHERE name IN ('TEACHER', 'DIRECTOR', 'EMPLOYEE'));
    END IF;
    DELETE FROM role WHERE name IN ('TEACHER', 'DIRECTOR', 'EMPLOYEE');
END $$;

-- Drop only known legacy tables, child tables first. No CASCADE is used: an
-- unknown dependency makes the migration fail visibly instead of deleting it.
DROP TABLE IF EXISTS vr_test_answer;
DROP TABLE IF EXISTS vr_test;
DROP TABLE IF EXISTS vr_test_type;
DROP TABLE IF EXISTS simulation;
DROP TABLE IF EXISTS scenario;
DROP TABLE IF EXISTS simulation_data_source;
DROP TABLE IF EXISTS simulation_type;
DROP TABLE IF EXISTS pupil_grade;
DROP TABLE IF EXISTS pupil_subject_profile;
DROP TABLE IF EXISTS interest_level;
DROP TABLE IF EXISTS participation_level;
DROP TABLE IF EXISTS probability_level;
DROP TABLE IF EXISTS subject;

DO $$
DECLARE
    item RECORD;
    new_name TEXT;
BEGIN
    FOR item IN
        SELECT conrelid::regclass AS table_name, conname
        FROM pg_constraint
        WHERE conname ILIKE '%pupil%'
    LOOP
        new_name := replace(item.conname, 'pupil', 'applicant');
        IF new_name <> item.conname THEN
            EXECUTE format('ALTER TABLE %s RENAME CONSTRAINT %I TO %I',
                           item.table_name, item.conname, new_name);
        END IF;
    END LOOP;

    FOR item IN
        SELECT schemaname, indexname
        FROM pg_indexes
        WHERE indexname ILIKE '%pupil%'
    LOOP
        new_name := replace(item.indexname, 'pupil', 'applicant');
        IF new_name <> item.indexname
           AND to_regclass(format('%I.%I', item.schemaname, new_name)) IS NULL THEN
            EXECUTE format('ALTER INDEX %I.%I RENAME TO %I',
                           item.schemaname, item.indexname, new_name);
        END IF;
    END LOOP;
END $$;
