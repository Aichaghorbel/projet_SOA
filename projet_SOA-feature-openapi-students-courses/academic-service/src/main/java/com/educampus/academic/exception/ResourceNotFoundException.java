package com.educampus.academic.exception;

/** Ressource introuvable : traduite en HTTP 404 par le GlobalExceptionHandler. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " " + id + " introuvable");
    }
}
