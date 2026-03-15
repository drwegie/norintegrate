-- =============================================================================
-- NorIntegrate Seed Data — Skilled Worker Visa Checklist
-- OVERRIDING SYSTEM VALUE is used throughout to insert explicit IDs into
-- GENERATED ALWAYS AS IDENTITY columns for reproducibility.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- visa_type
-- ---------------------------------------------------------------------------
INSERT INTO visa_type (id, name, description)
VALUES (
    'SKILLED_WORKER',
    'Skilled Worker',
    'Residence permit for skilled workers with a job offer in Norway'
);

-- ---------------------------------------------------------------------------
-- procedure (11 rows, ids 1–11)
-- ---------------------------------------------------------------------------
INSERT INTO procedure (id, title, authority, estimated_days)
OVERRIDING SYSTEM VALUE
VALUES
    (1,  'Receive job offer from Norwegian employer',                          'Employer',      0),
    (2,  'Apply for skilled worker residence permit via UDI',                  'UDI',          30),
    (3,  'Book biometrics appointment at Norwegian embassy/consulate',         'UDI',           7),
    (4,  'Apply for D-number from Skatteetaten',                               'Skatteetaten', 14),
    (5,  'Open Norwegian bank account',                                        'Bank',          3),
    (6,  'Obtain tax card (skattekort) from Skatteetaten',                     'Skatteetaten',  3),
    (7,  'Register with NAV',                                                  'NAV',           1),
    (8,  'Register with a GP (fastlege)',                                      'Helfo',         7),
    (9,  'Register with the National Population Register (Folkeregisteret)',   'Skatteetaten', 14),
    (10, 'Obtain national ID number (personnummer)',                            'Skatteetaten', 30),
    (11, 'Apply for Norwegian national ID card',                               'Politiet',     30);

-- ---------------------------------------------------------------------------
-- procedure_dependency (DAG edges)
-- Format: prerequisite_id → dependent_id
--
--  1 → 2   job offer before permit application
--  2 → 3   permit applied before biometrics appointment
--  2 → 4   permit approved before D-number
--  4 → 5   D-number before bank account
--  4 → 6   D-number before tax card
--  4 → 8   D-number before GP registration
--  6 → 7   tax card before NAV registration
--  4 → 9   D-number before Folkeregisteret
--  9 → 10  Folkeregisteret before personnummer
-- 10 → 11  personnummer before national ID card
-- ---------------------------------------------------------------------------
INSERT INTO procedure_dependency (prerequisite_id, dependent_id)
VALUES
    (1,  2),
    (2,  3),
    (2,  4),
    (4,  5),
    (4,  6),
    (4,  8),
    (6,  7),
    (4,  9),
    (9,  10),
    (10, 11);

-- ---------------------------------------------------------------------------
-- document_requirement
-- ---------------------------------------------------------------------------
INSERT INTO document_requirement (procedure_id, document_name, is_mandatory)
VALUES
    -- Procedure 1: Receive job offer
    (1,  'Employment contract',                             TRUE),

    -- Procedure 2: Apply for residence permit
    (2,  'Valid passport',                                  TRUE),
    (2,  'Employment contract',                             TRUE),
    (2,  'Proof of qualifications',                         TRUE),

    -- Procedure 3: Book biometrics appointment
    (3,  'UDI application reference number',                TRUE),

    -- Procedure 4: Apply for D-number
    (4,  'Residence permit approval letter',                TRUE),
    (4,  'Valid passport',                                  TRUE),

    -- Procedure 5: Open bank account
    (5,  'D-number confirmation',                           TRUE),
    (5,  'Valid passport',                                  TRUE),

    -- Procedure 6: Obtain tax card
    (6,  'D-number confirmation',                           TRUE),
    (6,  'Employment details',                              TRUE),

    -- Procedure 7: Register with NAV
    (7,  'Tax card (skattekort)',                           TRUE),
    (7,  'Employment contract',                             TRUE),

    -- Procedure 8: Register with GP (fastlege)
    (8,  'D-number or personnummer',                        TRUE),

    -- Procedure 9: Register with Folkeregisteret
    (9,  'Valid passport',                                  TRUE),
    (9,  'Proof of address in Norway',                      TRUE),

    -- Procedure 10: Obtain personnummer
    (10, 'Proof of 6 months residence in Norway',           TRUE),
    (10, 'Folkeregisteret registration confirmation',        TRUE),

    -- Procedure 11: Apply for national ID card
    (11, 'Personnummer',                                    TRUE),
    (11, 'Valid passport',                                  TRUE),
    (11, 'Biometric photo',                                 TRUE);

-- ---------------------------------------------------------------------------
-- checklist_template
-- All 11 procedures linked to SKILLED_WORKER, display_order 1–11
-- ---------------------------------------------------------------------------
INSERT INTO checklist_template (visa_type_id, procedure_id, display_order)
VALUES
    ('SKILLED_WORKER',  1,  1),
    ('SKILLED_WORKER',  2,  2),
    ('SKILLED_WORKER',  3,  3),
    ('SKILLED_WORKER',  4,  4),
    ('SKILLED_WORKER',  5,  5),
    ('SKILLED_WORKER',  6,  6),
    ('SKILLED_WORKER',  7,  7),
    ('SKILLED_WORKER',  8,  8),
    ('SKILLED_WORKER',  9,  9),
    ('SKILLED_WORKER', 10, 10),
    ('SKILLED_WORKER', 11, 11);
