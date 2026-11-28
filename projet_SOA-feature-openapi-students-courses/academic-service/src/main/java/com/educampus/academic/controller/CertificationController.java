package com.educampus.academic.controller;

import com.educampus.academic.dto.CertificationRequestCreate;
import com.educampus.academic.dto.CertificationRequestResponse;
import com.educampus.academic.service.CertificationRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint /api/v1/certification-requests (voir contracts/enrollments-results.yaml).
 * La demande est relayée vers University Administration (SOAP) après vérification d'éligibilité.
 */
@RestController
@RequestMapping("/api/v1/certification-requests")
@Validated
@Tag(name = "Certification", description = "Demandes de certification (intégration SOAP)")
public class CertificationController {

    private final CertificationRequestService service;

    public CertificationController(CertificationRequestService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Soumettre une demande de certification")
    public ResponseEntity<CertificationRequestResponse> create(
            @Valid @RequestBody CertificationRequestCreate request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.submit(request));
    }
}
