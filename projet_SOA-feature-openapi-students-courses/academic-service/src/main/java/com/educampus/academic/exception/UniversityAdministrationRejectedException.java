package com.educampus.academic.exception;

/** L'administration a refusé la demande (SOAP Fault) : traduite en HTTP 422. */
public class UniversityAdministrationRejectedException extends RuntimeException {

    public UniversityAdministrationRejectedException(String message) {
        super(message);
    }
}
