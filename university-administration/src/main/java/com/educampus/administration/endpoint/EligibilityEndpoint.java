package com.educampus.administration.endpoint;

import com.educampus.administration.config.WebServiceConfig;
import com.educampus.administration.exception.StudentNotFoundException;
import com.educampus.administration.generated.CheckEligibilityRequest;
import com.educampus.administration.generated.CheckEligibilityResponse;
import com.educampus.administration.generated.StudentStatus;
import java.time.OffsetDateTime;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * Squelette Sprint 1 : reponse simulee (pas de base de donnees).
 * La vraie logique (StudentEligibility en base) est prevue au Sprint 2.
 */
@Endpoint
public class EligibilityEndpoint {

    @PayloadRoot(namespace = WebServiceConfig.NAMESPACE_URI, localPart = "CheckEligibilityRequest")
    @ResponsePayload
    public CheckEligibilityResponse checkEligibility(@RequestPayload CheckEligibilityRequest request) throws Exception {
        String studentId = request.getStudentId();
        if ("UNKNOWN".equalsIgnoreCase(studentId)) {          // permet de tester un SOAP Fault
            throw new StudentNotFoundException(studentId);
        }

        CheckEligibilityResponse response = new CheckEligibilityResponse();
        response.setStudentId(studentId);
        response.setEligible(true);
        response.setStudentStatus(StudentStatus.ACTIVE);
        response.setCheckedAt(now());
        return response;
    }

    static XMLGregorianCalendar now() throws Exception {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(OffsetDateTime.now().toString());
    }
}
