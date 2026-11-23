package com.educampus.academic.exception;

/** Résultat absent : traduit en 404 par GlobalExceptionHandler. */
public class ResultNotFoundException extends ResourceNotFoundException {

    public ResultNotFoundException(Long id) {
        super("Résultat " + id + " introuvable");
    }
}