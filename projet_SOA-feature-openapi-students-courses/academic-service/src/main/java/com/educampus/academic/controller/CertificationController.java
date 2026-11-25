package com.educampus.academic.controller;

import com.educampus.academic.dto.CertificationRequestCreate;
import com.educampus.academic.dto.CertificationRequestResponse;
import com.educampus.academic.exception.ResourceNotFoundException;
import com.educampus.academic.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint /api/v1/certification-requests (voir contracts/enrollments-results.yaml).
 *
 * <p>NOTE : le client SOAP vers University Administration (SubmitCertification) n'est pas encore
 * branché. En attendant, la demande est validée (l'étudiant doit exister) puis acceptée avec le
 * statut SUBMITTED, sans persistance. À remplacer par l'appel SOAP réel.
 */
@RestController
@RequestMapping("/api/v1/certification-requests")
@Validated
@Tag(name = "Certification", description = "Demandes de certification (intégration SOAP)")
public class CertificationController {

    private final StudentRepository studentRepository;
    private final AtomicLong sequence = new AtomicLong();

    public CertificationController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @PostMapping
    @Operation(summary = "Soumettre une demande de certification")
    public ResponseEntity<CertificationRequestResponse> create(
            @Valid @RequestBody CertificationRequestCreate request) {
        if (!studentRepository.existsById(request.studentId())) {
            throw new ResourceNotFoundException("Étudiant", request.studentId());
        }
        OffsetDateTime now = OffsetDateTime.now();
        String requestId = String.format("CERT-%d-%04d", now.getYear(), sequence.incrementAndGet());
        CertificationRequestResponse body = new CertificationRequestResponse(
                requestId, request.studentId(), request.certificationType(), "SUBMITTED", now);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }
}
