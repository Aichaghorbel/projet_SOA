package com.educampus.academic.service;

import com.educampus.academic.dto.PageResponse;
import com.educampus.academic.dto.StudentRequest;
import com.educampus.academic.dto.StudentResponse;
import com.educampus.academic.entity.Student;
import com.educampus.academic.exception.DuplicateResourceException;
import com.educampus.academic.exception.ResourceNotFoundException;
import com.educampus.academic.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Logique métier des étudiants : règles de doublon (409) et ressource absente (404). */
@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> list(int page, int size, String q) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<Student> result = (q == null || q.isBlank())
                ? repository.findAll(pageable)
                : repository.search(q.trim(), pageable);
        return PageResponse.from(result, StudentResponse::from);
    }

    @Transactional(readOnly = true)
    public StudentResponse get(Long id) {
        return StudentResponse.from(findOrThrow(id));
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email déjà utilisé : " + request.email());
        }
        if (repository.existsByStudentNumber(request.studentNumber())) {
            throw new DuplicateResourceException(
                    "Numéro étudiant déjà utilisé : " + request.studentNumber());
        }
        Student student = new Student();
        apply(student, request);
        return StudentResponse.from(repository.saveAndFlush(student));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = findOrThrow(id);
        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Email déjà utilisé : " + request.email());
        }
        if (repository.existsByStudentNumberAndIdNot(request.studentNumber(), id)) {
            throw new DuplicateResourceException(
                    "Numéro étudiant déjà utilisé : " + request.studentNumber());
        }
        apply(student, request);
        return StudentResponse.from(repository.saveAndFlush(student));
    }

    @Transactional
    public void delete(Long id) {
        Student student = findOrThrow(id);
        repository.delete(student);
        // flush immédiat : une contrainte de clé étrangère violée est détectée ici (-> 409)
        repository.flush();
    }

    private Student findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", id));
    }

    private void apply(Student student, StudentRequest request) {
        student.setStudentNumber(request.studentNumber());
        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setEmail(request.email());
        student.setDateOfBirth(request.dateOfBirth());
        student.setPhone(request.phone());
        student.setProgram(request.program());
        student.setLevel(request.level());
    }
}
