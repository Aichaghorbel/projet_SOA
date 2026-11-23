package com.educampus.administration.endpoint;

import com.educampus.administration.config.WebServiceConfig;
import com.educampus.administration.generated.CertificationStatus;
import com.educampus.administration.generated.SubmitCertificationRequest;
import com.educampus.administration.generated.SubmitCertificationResponse;
import java.util.UUID;

import com.educampus.administration.model.CertificationRecord;
import com.educampus.administration.service.CertificationService;
import javax.xml.datatype.DatatypeFactory;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class CertificationEndpoint {

    private final CertificationService service;

    public CertificationEndpoint(CertificationService service) {
        this.service = service;
    }

    @PayloadRoot(namespace = WebServiceConfig.NAMESPACE_URI, localPart = "SubmitCertificationRequest")
    @ResponsePayload
    public SubmitCertificationResponse submitCertification(@RequestPayload SubmitCertificationRequest request)
            throws Exception {
        CertificationRecord record = service.submit(
                request.getStudentId(), request.getCertificationType(), request.getComment());

        SubmitCertificationResponse response = new SubmitCertificationResponse();
        response.setRequestId(record.getRequestId());
        response.setStudentId(record.getStudentId());
        response.setStatus(record.getStatus());
        response.setSubmittedAt(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(record.getSubmittedAt().toString()));
        response.setMessage("Demande enregistree");
        return response;
    }
}
