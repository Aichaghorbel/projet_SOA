package com.educampus.academic.exception;

/** Étudiant déjà inscrit au cours pour cette année : traduite en 409. */
public class DuplicateEnrollmentException extends DuplicateResourceException {

    public DuplicateEnrollmentException(Long studentId, Long courseId, String academicYear) {
        super("L'étudiant " + studentId + " est déjà inscrit au cours " + courseId
                + " pour l'année " + academicYear);
    }
}