package com.educampus.academic.exception;

public class UniversityAdministrationUnavailableException extends RuntimeException {

    public UniversityAdministrationUnavailableException(Throwable cause) {
        super("Service d'administration indisponible", cause);
    }

    public UniversityAdministrationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}