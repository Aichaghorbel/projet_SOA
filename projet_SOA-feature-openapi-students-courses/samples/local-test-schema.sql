-- ============================================================================
-- SCHÉMA DE TEST LOCAL UNIQUEMENT - NE PAS COPIER DANS db/ - NE PAS FAIRE DE REMPLACEMENT
-- Sert à tester les API avant que le Membre 2 livre init-academic.sql.
-- Le schéma officiel est celui du Membre 2 : il remplace celui-ci.
-- Chargement : voir README, section "Tester les API".
-- ============================================================================

CREATE TABLE IF NOT EXISTS students (
    id             BIGSERIAL PRIMARY KEY,
    student_number VARCHAR(20)  NOT NULL UNIQUE,
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    date_of_birth  DATE,
    phone          VARCHAR(20),
    program        VARCHAR(100),
    level          INTEGER      NOT NULL CHECK (level BETWEEN 1 AND 5),
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS courses (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(10)  NOT NULL UNIQUE,
    title        VARCHAR(150) NOT NULL,
    description  VARCHAR(1000),
    credits      INTEGER      NOT NULL CHECK (credits BETWEEN 1 AND 30),
    semester     VARCHAR(2)   NOT NULL CHECK (semester IN ('S1','S2','S3','S4','S5','S6')),
    teacher_name VARCHAR(100),
    capacity     INTEGER      CHECK (capacity BETWEEN 1 AND 500),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
