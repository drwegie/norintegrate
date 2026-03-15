INSERT INTO visa_type (id, name, description)
VALUES (
    'SKILLED_WORKER',
    'Skilled Worker',
    'Residence permit for skilled workers with a job offer in Norway'
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
    (11, 'Apply for Norwegian national ID card',                               'Politiet',     30);;

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
    (10, 11);;

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
    (11, 'Biometric photo',                                 TRUE);;

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
    ('SKILLED_WORKER', 11, 11);;
