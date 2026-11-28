package com.educampus.administration.service;

import com.educampus.administration.exception.InvalidRequestException;
import com.educampus.administration.exception.StudentNotEligibleException;
import com.educampus.administration.exception.StudentNotFoundException;
import com.educampus.administration.generated.StudentStatus;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class EligibilityService {

    private static final Pattern ACADEMIC_YEAR = Pattern.compile("^\\d{4}-\\d{4}$");

    /** Etudiants simulés (remplaçable par un repository / base de données). */
    private static final Map<String, StudentStatus> STUDENTS = Map.of(
            "STU-001", StudentStatus.ACTIVE,
            "STU-002", StudentStatus.ACTIVE,
            "STU-SUSP", StudentStatus.SUSPENDED,
            "STU-WDR", StudentStatus.WITHDRAWN,
            "STU-GRAD", StudentStatus.GRADUATED,
            "ETU2025101", StudentStatus.ACTIVE,
            "ETU2025102", StudentStatus.ACTIVE);

    /**
     * Vérifie l'éligibilité. Retourne le statut si l'étudiant est éligible,
     * sinon lève une exception convertie en SOAP Fault.
     */
    public StudentStatus checkEligibility(String studentId, String academicYear) {
        if (studentId == null || studentId.isBlank()) {
            throw new InvalidRequestException("studentId obligatoire");
        }
        if (academicYear != null && !ACADEMIC_YEAR.matcher(academicYear).matches()) {
            throw new InvalidRequestException("academicYear invalide (format attendu : 2026-2027)");
        }
        StudentStatus status = STUDENTS.get(studentId);
        if (status == null) {
            throw new StudentNotFoundException(studentId);
        }
        return switch (status) {
            case ACTIVE -> status;
            case SUSPENDED -> throw new StudentNotEligibleException(studentId, "compte suspendu");
            case WITHDRAWN -> throw new StudentNotEligibleException(studentId, "etudiant retire");
            case GRADUATED -> throw new StudentNotEligibleException(studentId, "etudiant deja diplome");
        };
    }
}