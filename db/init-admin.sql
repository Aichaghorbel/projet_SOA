-- =====================================================================
-- EduCampus — University Administration Service
-- Module : M6 — Modèle de données
-- Fichier : init-admin.sql
-- Branche : feature/admin-data-model
-- SGBD   : PostgreSQL 15+
-- Schéma : university_admin
-- ---------------------------------------------------------------------
-- Rôle dans l'architecture EduCampus
--   Le service University Administration (SOAP) communique avec le
--   système administratif de l'université pour :
--     1) vérifier le statut et l'éligibilité d'un étudiant (CheckEligibility),
--     2) transmettre les demandes officielles de certification
--        (SubmitCertification).
--   Ce script initialise la base PostgreSQL dédiée (3e base du
--   docker-compose du M7) et doit rester cohérent avec le contrat
--   XSD/WSDL produit par le M5 (feature/soap-xsd-wsdl).
--
-- Conventions
--   * Identifiants d'étudiant (`student_id`) : VARCHAR(50), partagés entre
--     les 3 microservices Academic (M1/M2), Smart Space (M3/M4) et
--     University Administration (M5/M6). À aligner avec M2 et Academic.
--   * `request_id` des certifications : UUID v4 (généré côté service),
--     retourné dans le SubmitCertificationResponse.
--   * Horodatages : `TIMESTAMP WITH TIME ZONE` (mapping naturel du
--     xs:dateTime du XSD de M5).
--   * Les énumérations SQL reflètent exactement les simpleType du XSD :
--       - student_status        → StudentStatus
--       - certification_type    → CertificationType
--       - certification_status  → CertificationStatus (imposés par le sprint, Issue 6)
--       - fault_code            → FaultCode
--
-- Référence contractuelle (XSD de M5, targetNamespace
--   http://educampus.com/soap/university-administration) :
--     Opération 1 : CheckEligibility      (CheckEligibilityRequest/Response)
--     Opération 2 : SubmitCertification   (SubmitCertificationRequest/Response)
--     Fault       : ServiceFault (code, message, timestamp)
-- =====================================================================

-- Création du schéma dédié (idempotent : ignoré si déjà existant)
CREATE SCHEMA IF NOT EXISTS university_admin;

-- Extension nécessaire pour la génération d'UUID v4 (gen_random_uuid())
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================================
-- 1. Types énumérés (miroir des simpleType du XSD de M5)
-- =====================================================================

-- StudentStatus : ACTIVE | SUSPENDED | GRADUATED | WITHDRAWN
CREATE TYPE university_admin.student_status AS ENUM (
    'ACTIVE',
    'SUSPENDED',
    'GRADUATED',
    'WITHDRAWN'
);

-- CertificationType : ENROLLMENT_CERTIFICATE | TRANSCRIPT |
--                    SUCCESS_CERTIFICATE | DEGREE_CERTIFICATE
CREATE TYPE university_admin.certification_type AS ENUM (
    'ENROLLMENT_CERTIFICATE',
    'TRANSCRIPT',
    'SUCCESS_CERTIFICATE',
    'DEGREE_CERTIFICATE'
);

-- CertificationStatus : imposés par le sprint (Issue 6)
-- SUBMITTED → IN_REVIEW → (APPROVED | REJECTED)
CREATE TYPE university_admin.certification_status AS ENUM (
    'SUBMITTED',
    'IN_REVIEW',
    'APPROVED',
    'REJECTED'
);

-- FaultCode : codes d'erreur du ServiceFault du XSD de M5
CREATE TYPE university_admin.fault_code AS ENUM (
    'INVALID_REQUEST',
    'STUDENT_NOT_FOUND',
    'INTERNAL_ERROR'
);

-- =====================================================================
-- 2. Table : students
--    Miroir administratif des étudiants connus d'Academic (M1/M2).
--    Le service n'est pas propriétaire du cycle de vie de l'étudiant :
--    il conserve une vue administrative (statut, année courante)
--    utilisée par CheckEligibility.
--    Note : l'éligibilité n'est PAS stockée ici. Elle est calculée à la
--    volée par CheckEligibility à partir du statut (règle métier :
--    eligible = (status = ACTIVE)). Le résultat de chaque contrôle est
--    journalisé dans eligibility_checks (eligible + reason).
-- =====================================================================
CREATE TABLE university_admin.students (
    student_id            VARCHAR(50)   NOT NULL,
    first_name            VARCHAR(100)  NOT NULL,
    last_name             VARCHAR(100)  NOT NULL,
    email                 VARCHAR(255)  NOT NULL,
    status                university_admin.student_status NOT NULL DEFAULT 'ACTIVE',
    current_academic_year VARCHAR(20),                       -- ex : 2026-2027
    enrollment_year       INTEGER,                           -- année 1ère inscription
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT pk_students PRIMARY KEY (student_id),
    CONSTRAINT uk_students_email UNIQUE (email),
    CONSTRAINT ck_students_email CHECK (email ~* '^[^@]+@[^@]+\.[^@]+$'),
    CONSTRAINT ck_students_year CHECK (
        current_academic_year IS NULL OR current_academic_year ~ '^\d{4}-\d{4}$'
    ),
    CONSTRAINT ck_students_enrollment_year CHECK (
        enrollment_year IS NULL OR enrollment_year BETWEEN 1990 AND 2100
    )
);

COMMENT ON TABLE  university_admin.students IS
    'Miroir administratif des étudiants (aligné sur Academic M1/M2). Source du statut utilisé par CheckEligibility.';
COMMENT ON COLUMN university_admin.students.student_id IS
    'Identifiant partagé entre Academic, Smart Space et University Admin (XSD StudentId : 1..50).';
COMMENT ON COLUMN university_admin.students.status IS
    'Statut administratif (XSD StudentStatus : ACTIVE/SUSPENDED/GRADUATED/WITHDRAWN). L''éligibilité est déduite de ce statut (règle : eligible = (status = ACTIVE)).';

-- Index d'accès par email et par statut (filtres fréquents côté administration)
CREATE INDEX idx_students_status    ON university_admin.students (status);
CREATE INDEX idx_students_academic  ON university_admin.students (current_academic_year);

-- =====================================================================
-- 3. Table : certification_requests
--    Persiste chaque appel SubmitCertification (Opération 2 du WSDL).
--    Une demande = un étudiant + un type de certification + un statut.
-- =====================================================================
CREATE TABLE university_admin.certification_requests (
    request_id          UUID                       NOT NULL DEFAULT gen_random_uuid(),
    student_id          VARCHAR(50)                NOT NULL,
    certification_type  university_admin.certification_type   NOT NULL,
    status              university_admin.certification_status NOT NULL DEFAULT 'SUBMITTED',
    comment             VARCHAR(1000),                                   -- SubmitCertificationRequest/comment (minOccurs=0)
    message             VARCHAR(1000),                                   -- SubmitCertificationResponse/message (minOccurs=0)
    submitted_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    reviewed_at         TIMESTAMP WITH TIME ZONE,                        -- renseigné à IN_REVIEW/APPROVED/REJECTED
    reviewer_id         VARCHAR(50),                                     -- agent administratif
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT pk_certification_requests PRIMARY KEY (request_id),
    CONSTRAINT fk_cert_req_student
        FOREIGN KEY (student_id) REFERENCES university_admin.students (student_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_cert_req_reviewed_at
        CHECK ( (status IN ('IN_REVIEW','APPROVED','REJECTED') AND reviewed_at IS NOT NULL)
                OR (status = 'SUBMITTED') )
);

COMMENT ON TABLE  university_admin.certification_requests IS
    'Demandes officielles de certification (Opération SubmitCertification du WSDL).';
COMMENT ON COLUMN university_admin.certification_requests.request_id IS
    'UUID v4 renvoyé dans le SubmitCertificationResponse/requestId.';
COMMENT ON COLUMN university_admin.certification_requests.status IS
    'Cycle SUBMITTED → IN_REVIEW → (APPROVED | REJECTED) (imposé par le sprint, Issue 6).';
COMMENT ON COLUMN university_admin.certification_requests.reviewer_id IS
    'Identifiant de l''agent administratif ayant traité la demande.';

-- Index composites pour les requêtes métier courantes :
--   - tableau de bord : liste par statut pour un étudiant
--   - reporting : toutes les demandes d''un type donné par statut
CREATE INDEX idx_cert_req_student_status ON university_admin.certification_requests (student_id, status);
CREATE INDEX idx_cert_req_type_status    ON university_admin.certification_requests (certification_type, status);
CREATE INDEX idx_cert_req_submitted_at   ON university_admin.certification_requests (submitted_at);

-- =====================================================================
-- 4. Table : eligibility_checks
--    Journal d'audit de tous les appels CheckEligibility (Opération 1).
--    Permet de tracer qui a interrogé l'éligibilité, quand, avec quel
--    résultat. Indépendant de l'état courant de `students` (historique
--    immuable).
-- =====================================================================
CREATE TABLE university_admin.eligibility_checks (
    id              BIGSERIAL                       PRIMARY KEY,
    student_id      VARCHAR(50)                     NOT NULL,
    academic_year   VARCHAR(20),                          -- ex : 2026-2027 (CheckEligibilityRequest/academicYear, minOccurs=0)
    eligible        BOOLEAN                         NOT NULL,
    student_status  university_admin.student_status NOT NULL,
    reason          VARCHAR(255),                        -- CheckEligibilityResponse/reason (minOccurs=0)
    checked_at      TIMESTAMP WITH TIME ZONE        NOT NULL DEFAULT now()
);

COMMENT ON TABLE  university_admin.eligibility_checks IS
    'Journal d''audit immuable des appels CheckEligibility (Opération 1 du WSDL).';
COMMENT ON COLUMN university_admin.eligibility_checks.academic_year IS
    'Année académique transmise dans la requête (minOccurs=0).';
COMMENT ON COLUMN university_admin.eligibility_checks.eligible IS
    'Résultat boolérenvoyé dans CheckEligibilityResponse/eligible.';
COMMENT ON COLUMN university_admin.eligibility_checks.student_status IS
    'Statut au moment du contrôle (CheckEligibilityResponse/studentStatus).';
COMMENT ON COLUMN university_admin.eligibility_checks.reason IS
    'Raison renseignée surtout quand eligible=false.';

-- Index pour le suivi opérationnel (volume important : 1 ligne par appel)
CREATE INDEX idx_elig_checks_student    ON university_admin.eligibility_checks (student_id);
CREATE INDEX idx_elig_checks_checked_at  ON university_admin.eligibility_checks (checked_at);

-- =====================================================================
-- 5. Table : audit_log
--    Trace des erreurs SOAP (ServiceFault) et des opérations sensibles.
--    Sert également au débogage et au support (corrélation des appels).
--    `student_id` est un TEXTE LIBRE (sans contrainte de clé étrangère) :
--    le journal d'audit doit pouvoir enregistrer un studentId même si
--    l'étudiant n'existe pas dans le référentiel (cas STUDENT_NOT_FOUND).
--    On sacrifie l'intégrité référentielle au profit de l'exhaustivité de
--    la trace, ce qui est l'objectif premier d'un journal d'audit.
-- =====================================================================
CREATE TABLE university_admin.audit_log (
    id             BIGSERIAL                       PRIMARY KEY,
    student_id     VARCHAR(50),                                -- texte libre, sans FK ; NULL si l'opération ne cible pas un étudiant
    operation      VARCHAR(50)                     NOT NULL,   -- CheckEligibility | SubmitCertification
    correlation_id VARCHAR(50)                     NOT NULL,   -- identifiant de trace distribué (Jaeger)
    fault_code     university_admin.fault_code,               -- NULL si succès, sinon code du ServiceFault
    fault_message  VARCHAR(1000),                              -- ServiceFault/message
    timestamp      TIMESTAMP WITH TIME ZONE        NOT NULL DEFAULT now()
);

COMMENT ON TABLE  university_admin.audit_log IS
    'Trace des opérations SOAP et des ServiceFault (aligné sur le XSD de M5).';
COMMENT ON COLUMN university_admin.audit_log.student_id IS
    'Identifiant de l''étudiant ciblé par l''opération (texte libre, SANS FK). Permet de tracer un studentId inconnu (STUDENT_NOT_FOUND) que la FK refuserait. NULL si l''opération ne cible pas un étudiant.';
COMMENT ON COLUMN university_admin.audit_log.operation IS
    'Nom de l''opération WSDL : CheckEligibility ou SubmitCertification.';
COMMENT ON COLUMN university_admin.audit_log.correlation_id IS
    'Identifiant de corrélation pour le tracing distribué (Jaeger, bonus M7).';
COMMENT ON COLUMN university_admin.audit_log.fault_code IS
    'Code d''erreur du ServiceFault (XSD FaultCode). NULL si l''appel a réussi.';

CREATE INDEX idx_audit_log_student        ON university_admin.audit_log (student_id) WHERE student_id IS NOT NULL;
CREATE INDEX idx_audit_log_operation       ON university_admin.audit_log (operation);
CREATE INDEX idx_audit_log_fault_code       ON university_admin.audit_log (fault_code) WHERE fault_code IS NOT NULL;
CREATE INDEX idx_audit_log_correlation_id   ON university_admin.audit_log (correlation_id);

-- =====================================================================
-- 6. Triggers : maintien automatique de updated_at
-- =====================================================================
CREATE OR REPLACE FUNCTION university_admin trg_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_students_updated_at
    BEFORE UPDATE ON university_admin.students
    FOR EACH ROW EXECUTE FUNCTION university_admin.trg_set_updated_at();

CREATE TRIGGER trg_cert_req_updated_at
    BEFORE UPDATE ON university_admin.certification_requests
    FOR EACH ROW EXECUTE FUNCTION university_admin.trg_set_updated_at();

-- =====================================================================
-- 7. Vues de reporting (facultatif, utile au support)
-- =====================================================================

-- Vue : certifications en cours de traitement (SUBMITTED ou IN_REVIEW)
CREATE OR REPLACE VIEW university_admin.v_certifications_in_progress AS
SELECT
    cr.request_id,
    cr.student_id,
    s.first_name,
    s.last_name,
    s.email,
    cr.certification_type,
    cr.status,
    cr.submitted_at,
    cr.reviewed_at,
    cr.reviewer_id
FROM university_admin.certification_requests cr
JOIN university_admin.students s ON s.student_id = cr.student_id
WHERE cr.status IN ('SUBMITTED', 'IN_REVIEW')
ORDER BY cr.submitted_at;

COMMENT ON VIEW university_admin.v_certifications_in_progress IS
    'Demandes de certification à traiter par l''administration (SUBMITTED + IN_REVIEW).';

-- =====================================================================
-- 8. Données de référence (jeu de test / démo)
--    Les statuts de certification couvrent les 4 valeurs imposées par
--    le sprint (SUBMITTED, IN_REVIEW, APPROVED, REJECTED).
-- =====================================================================

-- Étudiants — statuts couvrant les 4 valeurs de StudentStatus.
-- L'éligibilité n'est PAS stockée : elle est calculée à la volée par
-- CheckEligibility à partir du statut (règle : eligible = (status = ACTIVE)).
INSERT INTO university_admin.students
    (student_id, first_name, last_name, email, status, current_academic_year, enrollment_year)
VALUES
    ('STD-2026-0001', 'Amira',  'Benali',   'amira.benali@campus.edu',   'ACTIVE',    '2026-2027', 2024),
    ('STD-2026-0002', 'Yanis',   'Cherif',   'yanis.cherif@campus.edu',   'SUSPENDED', '2026-2027', 2023),
    ('STD-2026-0003', 'Lucie',   'Dupont',   'lucie.dupont@campus.edu',   'GRADUATED', '2025-2026', 2022),
    ('STD-2026-0004', 'Karim',   'El Amrani','karim.elamrani@campus.edu', 'WITHDRAWN', '2025-2026', 2021);

-- Demandes de certification — les 4 valeurs de CertificationStatus
INSERT INTO university_admin.certification_requests
    (request_id, student_id, certification_type, status, comment, message, submitted_at, reviewed_at, reviewer_id)
VALUES
    ('11111111-1111-4111-8111-111111111111', 'STD-2026-0001', 'ENROLLMENT_CERTIFICATE', 'APPROVED',  'Attestation demandée par l''étudiant pour la bourse 2026-2027', 'Document officiel généré et transmis.',           '2026-09-01 09:15:00+02', '2026-09-03 14:30:00+02', 'ADM-007'),
    ('22222222-2222-4222-8222-222222222222', 'STD-2026-0003', 'TRANSCRIPT',              'IN_REVIEW', 'Relevé de notes final pour admission en master',                'Vérification des résultats en cours.',          '2026-09-05 10:00:00+02', '2026-09-06 09:00:00+02', 'ADM-012'),
    ('33333333-3333-4333-8333-333333333333', 'STD-2026-0001', 'SUCCESS_CERTIFICATE',    'SUBMITTED', 'Attestation de réussite semestre 5',                            NULL,                                            '2026-09-10 11:20:00+02', NULL,                     NULL),
    ('44444444-4444-4444-8444-444444444444', 'STD-2026-0002', 'DEGREE_CERTIFICATE',     'REJECTED',  'Diplôme demandé malgré la suspension',                          'Demande refusée : statut SUSPENDED non éligible.','2026-09-12 16:45:00+02', '2026-09-15 10:10:00+02', 'ADM-007');

-- Journal d'audit — exemples de CheckEligibility (succès + échec).
-- La colonne `eligible` est le résultat du calcul (status = ACTIVE) :
--   - STD-2026-0001 ACTIVE    -> TRUE
--   - STD-2026-0002 SUSPENDED -> FALSE (raison renseignée)
--   - STD-2026-0003 GRADUATED -> FALSE (règle métier : seul ACTIVE est éligible)
--   - STD-2026-0004 WITHDRAWN -> FALSE
INSERT INTO university_admin.eligibility_checks
    (student_id, academic_year, eligible, student_status, reason, checked_at)
VALUES
    ('STD-2026-0001', '2026-2027', TRUE,  'ACTIVE',    NULL,                                          '2026-09-01 09:14:55+02'),
    ('STD-2026-0002', '2026-2027', FALSE, 'SUSPENDED', 'Suspension disciplinaire en cours',            '2026-09-12 16:44:50+02'),
    ('STD-2026-0003', '2025-2026', FALSE, 'GRADUATED', 'Statut GRADUATED : seul ACTIVE est éligible',  '2026-09-05 10:05:00+02'),
    ('STD-2026-0004', '2025-2026', FALSE, 'WITHDRAWN', 'Étudiant désinscrit — non éligible',           '2026-09-13 08:30:00+02');

-- Audit log — exemples de ServiceFault (STUDENT_NOT_FOUND + INVALID_REQUEST).
-- student_id est un TEXTE LIBRE (sans FK) : on peut conserver l'identifiant
-- demandé même quand l'étudiant n'existe pas (cas STUDENT_NOT_FOUND).
INSERT INTO university_admin.audit_log
    (student_id, operation, correlation_id, fault_code, fault_message, timestamp)
VALUES
    ('STD-2026-9999', 'CheckEligibility',    'jaeger-trace-001', 'STUDENT_NOT_FOUND', 'Aucun étudiant pour studentId=STD-2026-9999', '2026-09-01 09:20:10+02'),
    ('STD-2026-0002', 'SubmitCertification', 'jaeger-trace-002', 'INVALID_REQUEST',  'certificationType manquant ou non autorisé',  '2026-09-02 11:05:33+02'),
    ('STD-2026-0001', 'CheckEligibility',    'jaeger-trace-003', NULL,                 NULL,                                          '2026-09-03 14:29:58+02');

-- =====================================================================
-- 9. Contrôle final
--    Renvoie un récapitulatif rapide des données initialisées.
-- =====================================================================
SELECT 'students'              AS table_name, count(*) AS n FROM university_admin.students
UNION ALL SELECT 'certification_requests', count(*) FROM university_admin.certification_requests
UNION ALL SELECT 'eligibility_checks',   count(*) FROM university_admin.eligibility_checks
UNION ALL SELECT 'audit_log',            count(*) FROM university_admin.audit_log
ORDER BY table_name;
