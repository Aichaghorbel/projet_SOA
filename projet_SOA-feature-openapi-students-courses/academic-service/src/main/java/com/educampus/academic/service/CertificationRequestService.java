package com.educampus.academic.service;

import com.educampus.academic.dto.CertificationRequestCreate;
import com.educampus.academic.dto.CertificationRequestResponse;
import com.educampus.academic.entity.Student;
import com.educampus.academic.exception.InvalidCertificationTypeException;
import com.educampus.academic.exception.ResourceNotFoundException;
import com.educampus.academic.exception.UniversityAdministrationRejectedException;
import com.educampus.academic.repository.StudentRepository;
import com.educampus.academic.soap.UniversityAdministrationClient;
import com.educampus.academic.soap.generated.CertificationType;
import com.educampus.academic.soap.generated.CheckEligibilityResponse;
import com.educampus.academic.soap.generated.SubmitCertificationResponse;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

/**
 * Workflow de demande de certification :
 * 1. valider le type ; 2. retrouver l'étudiant ; 3. vérifier son éligibilité (CheckEligibility) ;
 * 4. transmettre la demande officielle (SubmitCertification).
 */
@Service
public class CertificationRequestService {

    private final StudentRepository studentRepository;
    private final UniversityAdministrationClient client;

    public CertificationRequestService(StudentRepository studentRepository,
                                       UniversityAdministrationClient client) {
        this.studentRepository = studentRepository;
        this.client = client;
    }

    public CertificationRequestResponse submit(CertificationRequestCreate request) {
        CertificationType type = parseType(request.certificationType());

        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", request.studentId()));

        // Identifiant partagé avec University Administration : le numéro étudiant
        String soapStudentId = student.getStudentNumber();

        CheckEligibilityResponse eligibility = client.checkEligibility(soapStudentId, null);
        if (!eligibility.isEligible()) {
            throw new UniversityAdministrationRejectedException(
                    "Étudiant " + soapStudentId + " non éligible");
        }

        SubmitCertificationResponse submitted = client.submitCertification(soapStudentId, type, null);
        return new CertificationRequestResponse(
                submitted.getRequestId(),
                request.studentId(),
                request.certificationType(),
                submitted.getStatus().value(),
                toOffsetDateTime(submitted));
    }

    static CertificationType parseType(String value) {
        try {
            return CertificationType.fromValue(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidCertificationTypeException(value);
        }
    }

    private static OffsetDateTime toOffsetDateTime(SubmitCertificationResponse response) {
        return response.getSubmittedAt().toGregorianCalendar().toZonedDateTime().toOffsetDateTime();
    }
}
