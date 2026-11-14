package com.educampus.administration.endpoint;

import com.educampus.administration.config.WebServiceConfig;
import com.educampus.administration.generated.CertificationStatus;
import com.educampus.administration.generated.SubmitCertificationRequest;
import com.educampus.administration.generated.SubmitCertificationResponse;
import java.util.UUID;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/** Squelette Sprint 1 : reponse simulee (statut SUBMITTED). */
@Endpoint
public class CertificationEndpoint {

    @PayloadRoot(namespace = WebServiceConfig.NAMESPACE_URI, localPart = "SubmitCertificationRequest")
    @ResponsePayload
    public SubmitCertificationResponse submitCertification(@RequestPayload SubmitCertificationRequest request) throws Exception {
        SubmitCertificationResponse response = new SubmitCertificationResponse();
        response.setRequestId(UUID.randomUUID().toString());
        response.setStudentId(request.getStudentId());
        response.setStatus(CertificationStatus.SUBMITTED);
        response.setSubmittedAt(EligibilityEndpoint.now());
        response.setMessage("Demande enregistree");
        return response;
    }
}
