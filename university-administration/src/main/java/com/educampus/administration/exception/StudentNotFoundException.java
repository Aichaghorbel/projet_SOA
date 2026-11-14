package com.educampus.administration.exception;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/** Convertie automatiquement en SOAP Fault (faultcode Client). */
@SoapFault(faultCode = FaultCode.CLIENT)
public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(String studentId) {
        super("Etudiant introuvable : " + studentId);
    }
}
