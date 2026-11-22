-- ========================================================
-- Script d'initialisation PostgreSQL pour SmartSpace
-- Tables : room et reservation (basées sur les modèles Java)
-- ========================================================

-- 1. Suppression des tables si elles existent (dans l'ordre des contraintes)
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS room CASCADE;

-- 2. Création de la table 'room'
CREATE TABLE room (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    location VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Création de la table 'reservation'
CREATE TABLE reservation (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    etudiant_id VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    examen_session_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Clé étrangère vers la table room
    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) 
        REFERENCES room(id) ON DELETE CASCADE,

    -- Cohérence temporelle : l'heure de début doit être antérieure à l'heure de fin
    CONSTRAINT chk_reservation_time CHECK (start_time < end_time),

    -- Valeurs valides pour le statut (PENDING, CONFIRMED, CANCELED)
    CONSTRAINT chk_reservation_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELED')),

    -- Contrainte d'unicité (room_id, start_time, end_time)
    CONSTRAINT uq_reservation_room_time UNIQUE (room_id, start_time, end_time)
);

-- 4. Index pour optimiser les requêtes et les vérifications de chevauchement
CREATE INDEX idx_reservation_room_time ON reservation (room_id, start_time, end_time);
CREATE INDEX idx_reservation_etudiant ON reservation (etudiant_id);
CREATE INDEX idx_reservation_session ON reservation (examen_session_id);
