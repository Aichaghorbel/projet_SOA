package com.educampus.academic.soap;

import java.net.SocketTimeoutException;

import com.educampus.academic.exception.UniversityAdministrationRejectedException;
import com.educampus.academic.exception.UniversityAdministrationUnavailableException;
import com.educampus.academic.soap.generated.CertificationType;
import com.educampus.academic.soap.generated.CheckEligibilityRequest;
import com.educampus.academic.soap.generated.CheckEligibilityResponse;
import com.educampus.academic.soap.generated.SubmitCertificationRequest;
import com.educampus.academic.soap.generated.SubmitCertificationResponse;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;

/**
 * Appels SOAP vers University Administration.
 * Une SOAP Fault (règle métier) devient {@link UniversityAdministrationRejectedException} ;
 * un service injoignable ou un délai dépassé devient {@link UniversityAdministrationUnavailableException}.
 */
@Component
public class UniversityAdministrationClient {

    private final WebServiceTemplate template;

    public UniversityAdministrationClient(WebServiceTemplate universityAdministrationTemplate) {
        this.template = universityAdministrationTemplate;
    }

    public CheckEligibilityResponse checkEligibility(String studentId, String academicYear) {
        CheckEligibilityRequest request = new CheckEligibilityRequest();
        request.setStudentId(studentId);
        request.setAcademicYear(academicYear);
        return (CheckEligibilityResponse) send(request);
    }

    public SubmitCertificationResponse submitCertification(String studentId, CertificationType type, String comment) {
        SubmitCertificationRequest request = new SubmitCertificationRequest();
        request.setStudentId(studentId);
        request.setCertificationType(type);
        request.setComment(comment);
        return (SubmitCertificationResponse) send(request);
    }

    private Object send(Object request) {
        try {
            return template.marshalSendAndReceive(request);
        } catch (SoapFaultClientException e) {
            // Refus métier de l'administration -> 422
            throw new UniversityAdministrationRejectedException(e.getMessage());
        } catch (WebServiceClientException e) {
            // Délai dépassé ou service injoignable -> 503
            if (isTimeout(e)) {
                throw new UniversityAdministrationUnavailableException(
                        "délai dépassé en attendant l'administration", e);
            }
            throw new UniversityAdministrationUnavailableException(e);
        }
    }

    private static boolean isTimeout(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }
}