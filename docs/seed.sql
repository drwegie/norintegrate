INSERT INTO visa_type (id, name, description)
VALUES (
    'SKILLED_WORKER',
    'Skilled Worker',
    'Residence permit for skilled workers with a job offer in Norway'
);;

INSERT INTO visa_type (id, name, description)
VALUES (
    'FAMILY_REUNIFICATION',
    'Family Reunification',
    'Family reunification residence permit'
);;

INSERT INTO visa_type (id, name, description)
VALUES (
    'STUDENT',
    'Student',
    'Student residence permit'
);;

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
    (11, 'Apply for Norwegian national ID card',                               'Politiet',     30),
    (12, 'Prepare documentation of family relationship',                       'UDI',           7),
    (13, 'Apply for family reunification residence permit',                    'UDI',          60),
    (14, 'Attend interview at Norwegian embassy/consulate',                    'UDI',          14),
    (15, 'Receive admission letter from Norwegian educational institution',    'Institution',   0),
    (16, 'Show proof of financial support',                                    'UDI',           7),
    (17, 'Apply for student residence permit',                                 'UDI',          30);;

SELECT setval(pg_get_serial_sequence('procedure', 'id'), (SELECT MAX(id) FROM procedure));;

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
    (10, 11),
    (12, 13),
    (13, 14),
    (14, 4),
    (15, 16),
    (16, 17),
    (17, 3);;

INSERT INTO document_requirement (procedure_id, document_name, is_mandatory)
VALUES
    (1,  'Employment contract',                             TRUE),
    (2,  'Valid passport',                                  TRUE),
    (2,  'Employment contract',                             TRUE),
    (2,  'Proof of qualifications',                         TRUE),
    (3,  'UDI application reference number',                TRUE),
    (4,  'Residence permit approval letter',                TRUE),
    (4,  'Valid passport',                                  TRUE),
    (5,  'D-number confirmation',                           TRUE),
    (5,  'Valid passport',                                  TRUE),
    (6,  'D-number confirmation',                           TRUE),
    (6,  'Employment details',                              TRUE),
    (7,  'Tax card (skattekort)',                           TRUE),
    (7,  'Employment contract',                             TRUE),
    (8,  'D-number or personnummer',                        TRUE),
    (9,  'Valid passport',                                  TRUE),
    (9,  'Proof of address in Norway',                      TRUE),
    (10, 'Proof of 6 months residence in Norway',           TRUE),
    (10, 'Folkeregisteret registration confirmation',        TRUE),
    (11, 'Personnummer',                                    TRUE),
    (11, 'Valid passport',                                  TRUE),
    (11, 'Biometric photo',                                 TRUE),
    (12, 'Marriage certificate or birth certificate',       TRUE),
    (12, 'Valid passport',                                  TRUE),
    (12, 'Passport copy of reference person in Norway',     TRUE),
    (13, 'Valid passport',                                  TRUE),
    (13, 'Family relationship documentation',               TRUE),
    (13, 'Proof of income of reference person',             TRUE),
    (13, 'Housing documentation',                           FALSE),
    (14, 'UDI application reference number',                TRUE),
    (14, 'Valid passport',                                  TRUE),
    (14, 'Original family relationship documents',          TRUE),
    (15, 'Acceptance letter from Norwegian institution',    TRUE),
    (15, 'Transcript of previous education',                TRUE),
    (15, 'Proof of English language proficiency',           FALSE),
    (16, 'Bank statement showing sufficient funds',         TRUE),
    (16, 'Scholarship confirmation letter',                 FALSE),
    (17, 'Valid passport',                                  TRUE),
    (17, 'Acceptance letter from Norwegian institution',    TRUE),
    (17, 'Proof of financial support',                      TRUE),
    (17, 'Health insurance documentation',                  FALSE);;

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
    ('SKILLED_WORKER', 11, 11),
    ('FAMILY_REUNIFICATION', 12,  1),
    ('FAMILY_REUNIFICATION', 13,  2),
    ('FAMILY_REUNIFICATION', 14,  3),
    ('FAMILY_REUNIFICATION',  4,  4),
    ('FAMILY_REUNIFICATION',  5,  5),
    ('FAMILY_REUNIFICATION',  6,  6),
    ('FAMILY_REUNIFICATION',  7,  7),
    ('FAMILY_REUNIFICATION',  8,  8),
    ('FAMILY_REUNIFICATION',  9,  9),
    ('FAMILY_REUNIFICATION', 10, 10),
    ('FAMILY_REUNIFICATION', 11, 11),
    ('STUDENT', 15,  1),
    ('STUDENT', 16,  2),
    ('STUDENT', 17,  3),
    ('STUDENT',  3,  4),
    ('STUDENT',  4,  5),
    ('STUDENT',  5,  6),
    ('STUDENT',  6,  7),
    ('STUDENT',  7,  8),
    ('STUDENT',  8,  9),
    ('STUDENT',  9, 10),
    ('STUDENT', 10, 11),
    ('STUDENT', 11, 12);;
