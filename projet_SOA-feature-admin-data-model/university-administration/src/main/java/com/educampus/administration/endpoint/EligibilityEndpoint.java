package com.educampus.administration.endpoint;

import com.educampus.administration.config.WebServiceConfig;
import com.educampus.administration.generated.CheckEligibilityRequest;
import com.educampus.administration.generated.CheckEligibilityResponse;
import com.educampus.administration.generated.StudentStatus;
import com.educampus.administration.service.EligibilityService;
import java.time.OffsetDateTime;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class EligibilityEndpoint {

    private final EligibilityService service;

    public EligibilityEndpoint(EligibilityService service) {
        this.service = service;
    }

    @PayloadRoot(namespace = WebServiceConfig.NAMESPACE_URI, localPart = "CheckEligibilityRequest")
    @ResponsePayload
    public CheckEligibilityResponse checkEligibility(@RequestPayload CheckEligibilityRequest request)
            throws Exception {
        StudentStatus status = service.checkEligibility(request.getStudentId(), request.getAcademicYear());

        CheckEligibilityResponse response = new CheckEligibilityResponse();
        response.setStudentId(request.getStudentId());
        response.setEligible(true);
        response.setStudentStatus(status);
        response.setCheckedAt(now());
        return response;
    }

    /** Utilisé aussi par CertificationEndpoint (Issue 13) si présent. */
    static XMLGregorianCalendar now() throws Exception {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(OffsetDateTime.now().toString());
    }
}