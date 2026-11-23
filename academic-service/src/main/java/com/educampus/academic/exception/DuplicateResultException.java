package com.educampus.academic.exception;

/** Résultat déjà enregistré pour (inscription, examen, session) : traduite en 409. */
public class DuplicateResultException extends DuplicateResourceException {

    public DuplicateResultException(Long enrollmentId, Long examId, String sessionType) {
        super("Un résultat existe déjà pour l'inscription " + enrollmentId
                + (examId == null ? "" : ", l'examen " + examId) + " et la session " + sessionType);
    }
}