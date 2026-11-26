package com.educampus.academic.controller;

import com.educampus.academic.dto.PagedResponse;
import com.educampus.academic.dto.ResultRequest;
import com.educampus.academic.dto.ResultResponse;
import com.educampus.academic.dto.ResultUpdateRequest;
import com.educampus.academic.entity.ResultStatus;
import com.educampus.academic.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Endpoints /api/v1/results (voir contracts/enrollments-results.yaml). */
@RestController
@RequestMapping("/api/v1/results")
@Validated
@Tag(name = "Results", description = "Résultats (notes) des étudiants")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping
    @Operation(summary = "Lister les résultats")
    public PagedResponse<ResultResponse> list(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) ResultStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return resultService.list(studentId, courseId, examId, status, page, size);
    }

    @GetMapping("/{resultId}")
    @Operation(summary = "Consulter un résultat")
    public ResultResponse get(@PathVariable @Min(1) Long resultId) {
        return resultService.get(resultId);
    }

    @PostMapping
    @Operation(summary = "Enregistrer un résultat")
    public ResponseEntity<ResultResponse> create(@Valid @RequestBody ResultRequest request) {
        ResultResponse created = resultService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{resultId}")
    @Operation(summary = "Remplacer un résultat")
    public ResultResponse update(@PathVariable @Min(1) Long resultId,
                                 @Valid @RequestBody ResultUpdateRequest request) {
        return resultService.update(resultId, request);
    }

    @DeleteMapping("/{resultId}")
    @Operation(summary = "Supprimer un résultat")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long resultId) {
        resultService.delete(resultId);
        return ResponseEntity.noContent().build();
    }
}