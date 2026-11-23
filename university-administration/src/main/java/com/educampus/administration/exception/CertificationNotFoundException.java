package com.educampus.administration.exception;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

@SoapFault(faultCode = FaultCode.CLIENT)
public class CertificationNotFoundException extends RuntimeException {
    public CertificationNotFoundException(String requestId) {
        super("Demande de certification introuvable : " + requestId);
    }
}