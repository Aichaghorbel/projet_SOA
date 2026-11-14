package com.educampus.academic.service;

import com.educampus.academic.dto.CourseRequest;
import com.educampus.academic.dto.CourseResponse;
import com.educampus.academic.dto.PageResponse;
import com.educampus.academic.entity.Course;
import com.educampus.academic.entity.Semester;
import com.educampus.academic.exception.DuplicateResourceException;
import com.educampus.academic.exception.ResourceNotFoundException;
import com.educampus.academic.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Logique métier des cours : règle de doublon sur le code (409) et ressource absente (404). */
@Service
public class CourseService {

    private final CourseRepository repository;

    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> list(int page, int size, Semester semester, String q) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        boolean hasText = q != null && !q.isBlank();
        Page<Course> result;
        if (semester != null && hasText) {
            result = repository.searchBySemester(semester, q.trim(), pageable);
        } else if (semester != null) {
            result = repository.findBySemester(semester, pageable);
        } else if (hasText) {
            result = repository.search(q.trim(), pageable);
        } else {
            result = repository.findAll(pageable);
        }
        return PageResponse.from(result, CourseResponse::from);
    }

    @Transactional(readOnly = true)
    public CourseResponse get(Long id) {
        return CourseResponse.from(findOrThrow(id));
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Code de cours déjà utilisé : " + request.code());
        }
        Course course = new Course();
        apply(course, request);
        return CourseResponse.from(repository.saveAndFlush(course));
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = findOrThrow(id);
        if (repository.existsByCodeAndIdNot(request.code(), id)) {
            throw new DuplicateResourceException("Code de cours déjà utilisé : " + request.code());
        }
        apply(course, request);
        return CourseResponse.from(repository.saveAndFlush(course));
    }

    @Transactional
    public void delete(Long id) {
        Course course = findOrThrow(id);
        repository.delete(course);
        // flush immédiat : une contrainte de clé étrangère violée est détectée ici (-> 409)
        repository.flush();
    }

    private Course findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours", id));
    }

    private void apply(Course course, CourseRequest request) {
        course.setCode(request.code());
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setCredits(request.credits());
        course.setSemester(request.semester());
        course.setTeacherName(request.teacherName());
        course.setCapacity(request.capacity());
    }
}
