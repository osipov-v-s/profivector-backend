-- ProfiVector B2B default reference data.
-- Run manually after Hibernate/JPA has created the current PostgreSQL schema.
-- This file intentionally contains reference data only; it does not create or alter schema objects.

BEGIN;

-- ============================================================
-- 1. Roles
-- ============================================================

INSERT INTO role (name)
SELECT seed.name
FROM (VALUES
    ('ADMIN'),
    ('HR'),
    ('SPECIALIST'),
    ('APPLICANT')
) AS seed(name)
WHERE NOT EXISTS (
    SELECT 1
    FROM role existing
    WHERE existing.name = seed.name
);

-- ============================================================
-- 2. Genders
-- ============================================================

INSERT INTO gender (name)
SELECT seed.name
FROM (VALUES
    ('MALE'),
    ('FEMALE')
) AS seed(name)
WHERE NOT EXISTS (
    SELECT 1
    FROM gender existing
    WHERE existing.name = seed.name
);

-- ============================================================
-- 3. Psychological test types
-- ============================================================
-- The complete psychological test type catalog is retained because the frontend
-- uses these exact names when presenting and submitting tests. VR test types are
-- stored in a separate, removed domain and are not part of this catalog.

INSERT INTO psych_test_type (name)
SELECT seed.name
FROM (VALUES
    ('Temperament'),
    ('Group-Roles'),
    ('Professional-Orientation'),
    ('Engineering-Thinking'),
    ('Intellectual-Potential'),
    ('Professional-Orientation-Klimov')
) AS seed(name)
WHERE NOT EXISTS (
    SELECT 1
    FROM psych_test_type existing
    WHERE existing.name = seed.name
);

-- ============================================================
-- 4. Psychological tests
-- ============================================================
-- The current model has no separate reference table for test definitions.
-- psych_test stores Applicant/Specialist test results and therefore must not be seeded.

-- ============================================================
-- 5. Psychological parameter names
-- ============================================================
-- Names are preserved exactly because PsychParamNameService resolves them by name.
-- The complete catalog from the source database is inserted for frontend compatibility.
-- completion_time_seconds is retained as a parameter name as well as being a dedicated
-- psych_test column because existing clients may request the reference name explicitly.

INSERT INTO psych_param_name (name)
SELECT seed.name
FROM (VALUES
    ('working_bee_score'),
    ('supervision_score'),
    ('motivation_score'),
    ('idea_generator_score'),
    ('supplier_score'),
    ('dedicator_score'),
    ('controller_score'),
    ('completion_time_seconds'),
    ('engineering_thinking_level'),
    ('nature_score'),
    ('tech_score'),
    ('human_score'),
    ('artistic_score'),
    ('sign_score'),
    ('extrav_introver_score'),
    ('neirotizm_score'),
    ('sincerity_score'),
    ('company_worker'),
    ('chairman'),
    ('shaper'),
    ('plant'),
    ('resource_investigator'),
    ('monitor_evaluation'),
    ('team_worker'),
    ('completer_finisher'),
    ('iq_score')
) AS seed(name)
WHERE NOT EXISTS (
    SELECT 1
    FROM psych_param_name existing
    WHERE existing.name = seed.name
);

-- ============================================================
-- 6. Relations between tests and parameters
-- ============================================================
-- No rows are inserted here. In the current schema psych_test_param links a concrete
-- user result (psych_test) to concrete calculated values (psych_param). It is not a
-- reference relation between psych_test_type and psych_param_name.

-- ============================================================
-- 7. Other required reference data
-- ============================================================

-- Applicant.targetProfession is required by the registration DTO. These are the
-- minimal industrial professions present in the legacy reference data. The current
-- Profession relation to ProfessionSphere is nullable, so unreliable legacy spheres
-- ("crate" and "it-guys") are intentionally not restored.
INSERT INTO profession (name, profession_sphere_id)
SELECT seed.name, NULL
FROM (VALUES
    ('Горный инженер'),
    ('Горный мастер'),
    ('Водитель белаза')
) AS seed(name)
WHERE NOT EXISTS (
    SELECT 1
    FROM profession existing
    WHERE existing.name = seed.name
);

-- PredictionService resolves these values through PredictionTypeEnum.
INSERT INTO prediction_type (name)
SELECT seed.name
FROM (VALUES
    ('CLUSTER'),
    ('MATH')
) AS seed(name)
WHERE NOT EXISTS (
    SELECT 1
    FROM prediction_type existing
    WHERE existing.name = seed.name
);

COMMIT;

-- ============================================================
-- Verification queries
-- Uncomment for manual verification after executing this file.
-- ============================================================

-- SELECT name FROM role ORDER BY name;
-- SELECT name FROM gender ORDER BY name;
-- SELECT name FROM psych_test_type ORDER BY name;
-- SELECT name FROM psych_param_name ORDER BY name;
-- SELECT name FROM profession ORDER BY name;
-- SELECT name FROM prediction_type ORDER BY name;

-- Expected seed counts (assuming the target tables were empty):
-- roles = 4, genders = 2, psychological test types = 6,
-- psychological parameter names = 26, professions = 3, prediction types = 2.
