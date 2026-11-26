package com.educampus.administration.exception;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

@SoapFault(faultCode = FaultCode.CLIENT)
public class DuplicateCertificationException extends RuntimeException {
    public DuplicateCertificationException(String studentId, String type) {
        super("Une demande " + type + " est deja en cours pour l'etudiant " + studentId);
    }
}