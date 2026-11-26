package com.educampus.academic.controller;

import com.educampus.academic.dto.PageResponse;
import com.educampus.academic.dto.StudentRequest;
import com.educampus.academic.dto.StudentResponse;
import com.educampus.academic.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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

/** Endpoints /api/v1/students (voir contracts/students-courses.yaml). */
@RestController
@RequestMapping("/api/v1/students")
@Validated
@Tag(name = "Students", description = "Gestion des étudiants")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Récupérer la liste des étudiants")
    public PageResponse<StudentResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Size(max = 100) String q) {
        return service.list(page, size, q);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un étudiant")
    public StudentResponse get(@PathVariable @Min(1) Long id) {
        return service.get(id);
    }

    @PostMapping
    @Operation(summary = "Créer un étudiant")
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        StudentResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/students/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un étudiant (remplacement complet)")
    public StudentResponse update(@PathVariable @Min(1) Long id,
                                  @Valid @RequestBody StudentRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un étudiant")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
