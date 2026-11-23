-- ==========================================================
-- EduCampus: Smart Space & Exam Service (gRPC)
-- Database: smartspace_db
-- M4: Exam Management tables, indexes and seed data
-- ==========================================================

-- ===== Questions =====
CREATE TABLE IF NOT EXISTS questions (
    id              BIGSERIAL PRIMARY KEY,
    exam_id         BIGINT NOT NULL,
    question_number INTEGER NOT NULL,
    type            VARCHAR(20) NOT NULL,
    text            TEXT NOT NULL,
    max_points      DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    CONSTRAINT chk_question_type CHECK (type IN ('MCQ', 'SHORT_ANSWER')),
    CONSTRAINT uq_question_exam_number UNIQUE (exam_id, question_number)
);

-- Short-answer reference answers (element collection)
CREATE TABLE IF NOT EXISTS question_reference_answers (
    question_id      BIGINT NOT NULL,
    reference_answer TEXT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- ===== Response Options (MCQ choices) =====
CREATE TABLE IF NOT EXISTS response_options (
    id            BIGSERIAL PRIMARY KEY,
    question_id   BIGINT NOT NULL,
    option_letter VARCHAR(5) NOT NULL,
    option_text   TEXT NOT NULL,
    is_correct    BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT uq_option_question_letter UNIQUE (question_id, option_letter)
);

-- ===== Exam Sessions =====
CREATE TABLE IF NOT EXISTS exam_sessions (
    id           BIGSERIAL PRIMARY KEY,
    exam_id      BIGINT NOT NULL,
    student_id   BIGINT NOT NULL,
    room_id      BIGINT,
    started_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    submitted_at TIMESTAMP WITH TIME ZONE,
    status       VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    max_score    DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_score  DOUBLE PRECISION,
    CONSTRAINT chk_exam_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'SUBMITTED', 'COMPLETED'))
);

-- ===== Student Answers =====
CREATE TABLE IF NOT EXISTS student_answers (
    id                 BIGSERIAL PRIMARY KEY,
    exam_session_id    BIGINT NOT NULL,
    question_id        BIGINT NOT NULL,
    selected_option_id BIGINT,
    text_response      TEXT,
    submitted_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    FOREIGN KEY (exam_session_id) REFERENCES exam_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id),
    FOREIGN KEY (selected_option_id) REFERENCES response_options(id),
    CONSTRAINT uq_student_answer UNIQUE (exam_session_id, question_id)
);

-- ===== Exam Progress =====
CREATE TABLE IF NOT EXISTS exam_progress (
    id                BIGSERIAL PRIMARY KEY,
    student_answer_id BIGINT NOT NULL,
    status            VARCHAR(20) NOT NULL,
    points_earned     DOUBLE PRECISION,
    timestamp         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    FOREIGN KEY (student_answer_id) REFERENCES student_answers(id) ON DELETE CASCADE,
    CONSTRAINT chk_progress_status CHECK (status IN ('CORRECTING', 'CORRECTED', 'ERROR')),
    CONSTRAINT uq_progress_answer UNIQUE (student_answer_id)
);

-- ===== Indexes =====
CREATE INDEX IF NOT EXISTS idx_questions_exam_id ON questions(exam_id);
CREATE INDEX IF NOT EXISTS idx_questions_number ON questions(exam_id, question_number);
CREATE INDEX IF NOT EXISTS idx_response_options_question ON response_options(question_id);
CREATE INDEX IF NOT EXISTS idx_exam_sessions_student ON exam_sessions(student_id);
CREATE INDEX IF NOT EXISTS idx_exam_sessions_exam ON exam_sessions(exam_id);
CREATE INDEX IF NOT EXISTS idx_exam_sessions_status ON exam_sessions(status);
CREATE INDEX IF NOT EXISTS idx_student_answers_session ON student_answers(exam_session_id);
CREATE INDEX IF NOT EXISTS idx_student_answers_question ON student_answers(question_id);
CREATE INDEX IF NOT EXISTS idx_exam_progress_answer ON exam_progress(student_answer_id);

-- ===== Seed / Test Data =====
-- Exam 101: "Introduction to SOA" with 3 questions

INSERT INTO questions (exam_id, question_number, type, text, max_points) VALUES
    (101, 1, 'MCQ',          'Which communication protocol is used in gRPC?',             4.0),
    (101, 2, 'MCQ',          'What is the primary benefit of microservices architecture?', 3.0),
    (101, 3, 'SHORT_ANSWER', 'Explain the difference between REST and gRPC in 1-2 sentences.', 3.0)
ON CONFLICT DO NOTHING;

-- Options for Q1 (correct: B — HTTP/2)
INSERT INTO response_options (question_id, option_letter, option_text, is_correct)
SELECT id, 'A', 'HTTP/1.1', FALSE FROM questions WHERE exam_id=101 AND question_number=1
ON CONFLICT DO NOTHING;
INSERT INTO response_options (question_id, option_letter, option_text, is_correct)
SELECT id, 'B', 'HTTP/2', TRUE FROM questions WHERE exam_id=101 AND question_number=1
ON CONFLICT DO NOTHING;
INSERT INTO response_options (question_id, option_letter, option_text, is_correct)
SELECT id, 'C', 'WebSocket', FALSE FROM questions WHERE exam_id=101 AND question_number=1
ON CONFLICT DO NOTHING;
INSERT INTO response_options (question_id, option_letter, option_text, is_correct)
SELECT id, 'D', 'MQTT', FALSE FROM questions WHERE exam_id=101 AND question_number=1
ON CONFLICT DO NOTHING;

-- Options for Q2 (correct: C — independent scalability)
INSERT INTO response_options (question_id, option_letter, option_text, is_correct)
SELECT id, 'A', 'Single deployment unit', FALSE FROM questions WHERE exam_id=101 AND question_number=2
ON CONFLICT DO NOTHING;
INSERT INTO response_options (question_id, option_letter, option_text, is_correct)
SELECT id, 'B', 'Shared database', FALSE FROM questions WHERE exam_id=101 AND question_number=2
ON CONFLICT DO NOTHING;
INSERT INTO response_options (question_id, option_letter, option_text, is_correct)
SELECT id, 'C', 'Independent scalability and deployment', TRUE FROM questions WHERE exam_id=101 AND question_number=2
ON CONFLICT DO NOTHING;
INSERT INTO response_options (question_id, option_letter, option_text, is_correct)
SELECT id, 'D', 'Monolithic architecture', FALSE FROM questions WHERE exam_id=101 AND question_number=2
ON CONFLICT DO NOTHING;

-- Reference answers for Q3 (SHORT_ANSWER)
INSERT INTO question_reference_answers (question_id, reference_answer)
SELECT id, 'REST uses HTTP/1.1 with JSON while gRPC uses HTTP/2 with Protocol Buffers for faster binary communication'
FROM questions WHERE exam_id=101 AND question_number=3
ON CONFLICT DO NOTHING;
INSERT INTO question_reference_answers (question_id, reference_answer)
SELECT id, 'gRPC uses Protocol Buffers and HTTP/2 for efficient binary serialization whereas REST uses JSON over HTTP'
FROM questions WHERE exam_id=101 AND question_number=3
ON CONFLICT DO NOTHING;

-- Initial Exam Session for testing
INSERT INTO exam_sessions (id, exam_id, student_id, max_score, status, started_at)
VALUES (1, 101, 42, 10.0, 'NOT_STARTED', NOW())
ON CONFLICT (id) DO NOTHING;

