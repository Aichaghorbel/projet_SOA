package com.educampus.academic.exception;

/** Examen absent : traduit en 404 par GlobalExceptionHandler. */
public class ExamNotFoundException extends ResourceNotFoundException {

    public ExamNotFoundException(Long id) {
        super("Examen " + id + " introuvable");
    }
}