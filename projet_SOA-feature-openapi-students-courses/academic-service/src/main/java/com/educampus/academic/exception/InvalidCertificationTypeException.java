package com.educampus.academic.exception;

/** Type de certification inconnu du contrat XSD : traduite en HTTP 400. */
public class InvalidCertificationTypeException extends RuntimeException {

    public InvalidCertificationTypeException(String value) {
        super("Type de certification invalide : " + value
                + " (attendu : ENROLLMENT_CERTIFICATE, TRANSCRIPT, SUCCESS_CERTIFICATE, DEGREE_CERTIFICATE)");
    }
}
