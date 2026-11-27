package com.educampus.administration.exception;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/** Etudiant inéligible : renvoyée comme SOAP Fault (faultcode Client). */
@SoapFault(faultCode = FaultCode.CLIENT)
public class StudentNotEligibleException extends RuntimeException {
    public StudentNotEligibleException(String studentId, String reason) {
        super("Etudiant " + studentId + " inéligible : " + reason);
    }
}