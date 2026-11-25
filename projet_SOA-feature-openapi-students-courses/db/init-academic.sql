-- ============================================================
-- EduCampus - Academic Service - init-academic.sql
-- SGBD : PostgreSQL 16
-- Les tables students et courses correspondent exactement aux
-- entités JPA du Membre 1 (Student.java, Course.java) afin que
-- spring.jpa.hibernate.ddl-auto=validate passe.
-- room_id (exams, course_schedules) = référence logique vers
-- Smart Space (pas de clé étrangère entre services).
-- ============================================================

-- ------------------------- students -------------------------
CREATE TABLE IF NOT EXISTS students (
    id              BIGSERIAL    PRIMARY KEY,
    student_number  VARCHAR(20)  NOT NULL UNIQUE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    date_of_birth   DATE,
    phone           VARCHAR(20),
    program         VARCHAR(100),
    level           INTEGER      NOT NULL CHECK (level BETWEEN 1 AND 5),
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

-- -------------------------- courses --------------------------
CREATE TABLE IF NOT EXISTS courses (
    id            BIGSERIAL    PRIMARY KEY,
    code          VARCHAR(10)  NOT NULL UNIQUE
                  CHECK (code ~ '^[A-Z]{2,5}[0-9]{2,4}$'),
    title         VARCHAR(150) NOT NULL,
    description   VARCHAR(1000),
    credits       INTEGER      NOT NULL CHECK (credits BETWEEN 1 AND 30),
    semester      VARCHAR(2)   NOT NULL
                  CHECK (semester IN ('S1','S2','S3','S4','S5','S6')),
    teacher_name  VARCHAR(100),
    capacity      INTEGER      CHECK (capacity BETWEEN 1 AND 500),
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- ---------------------- course_schedules ----------------------
CREATE TABLE IF NOT EXISTS course_schedules (
    id           BIGSERIAL PRIMARY KEY,
    course_id    BIGINT    NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    day_of_week  INTEGER   NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time   TIME      NOT NULL,
    end_time     TIME      NOT NULL,
    room_id      BIGINT,
    CHECK (end_time > start_time)
);

-- ------------------------- enrollments -------------------------
CREATE TABLE IF NOT EXISTS enrollments (
    id             BIGSERIAL   PRIMARY KEY,
    student_id     BIGINT      NOT NULL REFERENCES students(id),
    course_id      BIGINT      NOT NULL REFERENCES courses(id),
    academic_year  VARCHAR(9)  NOT NULL CHECK (academic_year ~ '^[0-9]{4}-[0-9]{4}$'),
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                   CHECK (status IN ('PENDING','CONFIRMED','CANCELLED','COMPLETED')),
    enrolled_at    TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT uq_enrollment UNIQUE (student_id, course_id, academic_year)
);

-- ---------------------------- exams ----------------------------
CREATE TABLE IF NOT EXISTS exams (
    id            BIGSERIAL    PRIMARY KEY,
    course_id     BIGINT       NOT NULL REFERENCES courses(id),
    title         VARCHAR(200) NOT NULL,
    exam_date     TIMESTAMP    NOT NULL,
    duration_min  INTEGER      NOT NULL CHECK (duration_min > 0),
    room_id       BIGINT,
    max_grade     NUMERIC(4,2) NOT NULL DEFAULT 20.00,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- --------------------------- results ---------------------------
CREATE TABLE IF NOT EXISTS results (
    id             BIGSERIAL    PRIMARY KEY,
    enrollment_id  BIGINT       NOT NULL REFERENCES enrollments(id),
    exam_id        BIGINT       REFERENCES exams(id),
    grade          NUMERIC(4,2) NOT NULL CHECK (grade >= 0 AND grade <= 20),
    session_type   VARCHAR(20)  NOT NULL DEFAULT 'NORMAL'
                   CHECK (session_type IN ('NORMAL','RATTRAPAGE')),
    status         VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                   CHECK (status IN ('DRAFT','PUBLISHED')),
    source         VARCHAR(20)  NOT NULL DEFAULT 'MANUAL'
                   CHECK (source IN ('MANUAL','EXAM_AUTO')),
    comment        VARCHAR(500),
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    published_at   TIMESTAMP,
    CONSTRAINT uq_result UNIQUE (enrollment_id, exam_id, session_type)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_student ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course  ON enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_results_enrollment  ON results(enrollment_id);
CREATE INDEX IF NOT EXISTS idx_results_exam        ON results(exam_id);
CREATE INDEX IF NOT EXISTS idx_exams_course        ON exams(course_id);

-- ------------------------- Données de test -------------------------
-- Valeurs volontairement différentes des exemples de samples/ (student.json,
-- course.json) pour ne pas provoquer de 409 lors des tests de Membre 1.
INSERT INTO students (student_number, first_name, last_name, email, level, program) VALUES
  ('ETU2025101','Sarra','Trabelsi','sarra.trabelsi@educampus.tn',3,'Génie Logiciel'),
  ('ETU2025102','Yassine','Mejri','yassine.mejri@educampus.tn',2,'Génie Logiciel')
ON CONFLICT DO NOTHING;

INSERT INTO courses (code, title, credits, semester, teacher_name, capacity) VALUES
  ('ALG101','Algorithmique et structures de données',4,'S1','Dr. Salma Gharbi',40),
  ('DB201','Bases de données avancées',3,'S4','Dr. Hatem Jlassi',35)
ON CONFLICT DO NOTHING;

INSERT INTO enrollments (student_id, course_id, academic_year, status)
SELECT s.id, c.id, '2026-2027', v.status
FROM (VALUES ('ETU2025101','ALG101','CONFIRMED'),
             ('ETU2025102','ALG101','CONFIRMED'),
             ('ETU2025101','DB201','PENDING')) AS v(sn, cc, status)
JOIN students s ON s.student_number = v.sn
JOIN courses  c ON c.code = v.cc
ON CONFLICT DO NOTHING;

INSERT INTO exams (course_id, title, exam_date, duration_min)
SELECT id, 'Examen final Algorithmique', TIMESTAMP '2027-01-15 09:00', 120
FROM courses WHERE code = 'ALG101';

INSERT INTO results (enrollment_id, exam_id, grade, session_type, status, source)
SELECT e.id, x.id, 15.50, 'NORMAL', 'PUBLISHED', 'MANUAL'
FROM enrollments e
JOIN students s ON s.id = e.student_id
JOIN courses  c ON c.id = e.course_id
JOIN exams    x ON x.course_id = c.id
WHERE s.student_number = 'ETU2025101' AND c.code = 'ALG101';
