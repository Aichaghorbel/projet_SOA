package com.educampus.academic.exception;

/** Inscription absente : traduite en 404 par GlobalExceptionHandler. */
public class EnrollmentNotFoundException extends ResourceNotFoundException {

    public EnrollmentNotFoundException(Long id) {
        super("Inscription " + id + " introuvable");
    }
}