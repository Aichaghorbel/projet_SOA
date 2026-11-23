package com.educampus.academic.controller;

import com.educampus.academic.dto.EnrollmentRequest;
import com.educampus.academic.dto.EnrollmentResponse;
import com.educampus.academic.dto.EnrollmentStatusRequest;
import com.educampus.academic.dto.PagedResponse;
import com.educampus.academic.dto.ResultResponse;
import com.educampus.academic.entity.EnrollmentStatus;
import com.educampus.academic.service.EnrollmentService;
import com.educampus.academic.service.ResultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/enrollments")
@Validated
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final ResultService resultService;

    public EnrollmentController(EnrollmentService enrollmentService, ResultService resultService) {
        this.enrollmentService = enrollmentService;
        this.resultService = resultService;
    }

    @GetMapping
    public PagedResponse<EnrollmentResponse> list(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) EnrollmentStatus status,
            @RequestParam(required = false) String academicYear,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return enrollmentService.list(studentId, courseId, status, academicYear, page, size);
    }

    @GetMapping("/{enrollmentId}")
    public EnrollmentResponse get(@PathVariable @Min(1) Long enrollmentId) {
        return enrollmentService.get(enrollmentId);
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> create(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse created = enrollmentService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{enrollmentId}")
    public EnrollmentResponse updateStatus(@PathVariable @Min(1) Long enrollmentId,
                                           @Valid @RequestBody EnrollmentStatusRequest request) {
        return enrollmentService.updateStatus(enrollmentId, request.status());
    }

    @DeleteMapping("/{enrollmentId}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long enrollmentId) {
        enrollmentService.delete(enrollmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{enrollmentId}/results")
    public List<ResultResponse> listResults(@PathVariable @Min(1) Long enrollmentId) {
        return resultService.listByEnrollment(enrollmentId);
    }
}