-- ========================================================
-- Script d'initialisation PostgreSQL pour SmartSpace
-- Tables : room et reservation (basées sur les modèles Java)
-- ========================================================

CREATE TABLE IF NOT EXISTS room (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    location VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reservation (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    etudiant_id VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    examen_session_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) 
        REFERENCES room(id) ON DELETE CASCADE,
    CONSTRAINT chk_reservation_time CHECK (start_time < end_time),
    CONSTRAINT chk_reservation_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELED')),
    CONSTRAINT uq_reservation_room_time UNIQUE (room_id, start_time, end_time)
);

CREATE INDEX IF NOT EXISTS idx_reservation_room_time ON reservation (room_id, start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_reservation_etudiant ON reservation (etudiant_id);
CREATE INDEX IF NOT EXISTS idx_reservation_session ON reservation (examen_session_id);

-- Initial seed rooms
INSERT INTO room (id, name, capacity, location, is_active) VALUES
    (1, 'Amphi A', 100, 'Bâtiment Principal - RDC', TRUE),
    (2, 'Salle B101', 30, 'Bâtiment Informatique - 1er étage', TRUE),
    (3, 'Labo Réseau', 25, 'Bâtiment Informatique - 2ème étage', TRUE)
ON CONFLICT (id) DO NOTHING;
