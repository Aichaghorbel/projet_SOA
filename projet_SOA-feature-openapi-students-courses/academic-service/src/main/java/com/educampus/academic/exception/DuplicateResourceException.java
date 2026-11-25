package com.educampus.academic.exception;

/**
 * Doublon (email, numéro étudiant, code cours) ou ressource encore référencée :
 * traduite en HTTP 409 par le GlobalExceptionHandler.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
