package com.educampus.academic.exception;

/**
 * Règle métier violée sur une inscription (cours complet, changement de statut
 * interdit, suppression impossible car des résultats existent) : traduite en 409.
 */
public class EnrollmentConflictException extends DuplicateResourceException {

    public EnrollmentConflictException(String message) {
        super(message);
    }
}