package com.educampus.academic.service;

import com.educampus.academic.dto.PagedResponse;
import com.educampus.academic.dto.ResultRequest;
import com.educampus.academic.dto.ResultResponse;
import com.educampus.academic.dto.ResultUpdateRequest;
import com.educampus.academic.entity.Enrollment;
import com.educampus.academic.entity.EnrollmentStatus;
import com.educampus.academic.entity.Exam;
import com.educampus.academic.entity.Result;
import com.educampus.academic.entity.ResultSource;
import com.educampus.academic.entity.ResultStatus;
import com.educampus.academic.exception.DuplicateResultException;
import com.educampus.academic.exception.EnrollmentNotFoundException;
import com.educampus.academic.exception.ExamNotFoundException;
import com.educampus.academic.exception.ResultConflictException;
import com.educampus.academic.exception.ResultNotFoundException;
import com.educampus.academic.repository.EnrollmentRepository;
import com.educampus.academic.repository.ExamRepository;
import com.educampus.academic.repository.ResultRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResultService {

    private final ResultRepository resultRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamRepository examRepository;

    public ResultService(ResultRepository resultRepository,
                         EnrollmentRepository enrollmentRepository,
                         ExamRepository examRepository) {
        this.resultRepository = resultRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.examRepository = examRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ResultResponse> list(Long studentId, Long courseId, Long examId,
                                              ResultStatus status, int page, int size) {
        Specification<Result> spec = Specification.where(null);
        if (examId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("examId"), examId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (studentId != null || courseId != null) {
            // filtre sur l'inscription associée (sous-requête, pas de relation JPA)
            spec = spec.and((root, query, cb) -> {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<Enrollment> enrollment = sub.from(Enrollment.class);
                List<Predicate> predicates = new java.util.ArrayList<>();
                if (studentId != null) {
                    predicates.add(cb.equal(enrollment.get("studentId"), studentId));
                }
                if (courseId != null) {
                    predicates.add(cb.equal(enrollment.get("courseId"), courseId));
                }
                sub.select(enrollment.<Long>get("id")).where(predicates.toArray(new Predicate[0]));
                return root.get("enrollmentId").in(sub);
            });
        }
        return PagedResponse.from(resultRepository
                .findAll(spec, PageRequest.of(page, size, Sort.by("id")))
                .map(ResultResponse::from));
    }

    @Transactional(readOnly = true)
    public List<ResultResponse> listByEnrollment(Long enrollmentId) {
        if (!enrollmentRepository.existsById(enrollmentId)) {
            throw new EnrollmentNotFoundException(enrollmentId);
        }
        return resultRepository.findByEnrollmentIdOrderByIdAsc(enrollmentId).stream()
                .map(ResultResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResultResponse get(Long id) {
        return ResultResponse.from(findOrThrow(id));
    }

    @Transactional
    public ResultResponse create(ResultRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(() -> new EnrollmentNotFoundException(request.enrollmentId()));
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new ResultConflictException("L'inscription " + enrollment.getId() + " est annulée");
        }
        if (request.examId() != null) {
            checkExam(request.examId(), enrollment, request.grade());
        }
        if (resultRepository.existsByEnrollmentIdAndExamIdAndSessionType(
                request.enrollmentId(), request.examId(), request.sessionType())) {
            throw new DuplicateResultException(request.enrollmentId(), request.examId(),
                    request.sessionType().name());
        }

        Result result = new Result();
        result.setEnrollmentId(request.enrollmentId());
        result.setExamId(request.examId());
        result.setGrade(request.grade());
        result.setSessionType(request.sessionType());
        result.setSource(ResultSource.MANUAL);
        result.setComment(request.comment());
        applyStatus(result, request.status() == null ? ResultStatus.DRAFT : request.status());
        return ResultResponse.from(resultRepository.save(result));
    }

    @Transactional
    public ResultResponse update(Long id, ResultUpdateRequest request) {
        Result result = findOrThrow(id);
        if (resultRepository.existsByEnrollmentIdAndExamIdAndSessionTypeAndIdNot(
                result.getEnrollmentId(), result.getExamId(), request.sessionType(), id)) {
            throw new DuplicateResultException(result.getEnrollmentId(), result.getExamId(),
                    request.sessionType().name());
        }
        if (result.getExamId() != null) {
            Exam exam = examRepository.findById(result.getExamId())
                    .orElseThrow(() -> new ExamNotFoundException(result.getExamId()));
            checkGradeWithinMax(exam, request.grade());
        }
        result.setGrade(request.grade());
        result.setSessionType(request.sessionType());
        result.setComment(request.comment());
        applyStatus(result, request.status());
        return ResultResponse.from(resultRepository.save(result));
    }

    /** Un résultat publié ne peut pas être supprimé. */
    @Transactional
    public void delete(Long id) {
        Result result = findOrThrow(id);
        if (result.getStatus() == ResultStatus.PUBLISHED) {
            throw new ResultConflictException("Impossible de supprimer le résultat " + id + " : il est publié");
        }
        resultRepository.delete(result);
    }

    private void checkExam(Long examId, Enrollment enrollment, BigDecimal grade) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new ExamNotFoundException(examId));
        if (!exam.getCourseId().equals(enrollment.getCourseId())) {
            throw new ResultConflictException("L'examen " + examId + " n'appartient pas au cours de l'inscription "
                    + enrollment.getId());
        }
        checkGradeWithinMax(exam, grade);
    }

    private void checkGradeWithinMax(Exam exam, BigDecimal grade) {
        if (exam.getMaxGrade() != null && grade.compareTo(exam.getMaxGrade()) > 0) {
            throw new ResultConflictException("La note " + grade + " dépasse le maximum de l'examen ("
                    + exam.getMaxGrade() + ")");
        }
    }

    private void applyStatus(Result result, ResultStatus status) {
        result.setStatus(status);
        if (status == ResultStatus.PUBLISHED) {
            if (result.getPublishedAt() == null) {
                result.setPublishedAt(LocalDateTime.now());
            }
        } else {
            result.setPublishedAt(null);
        }
    }

    private Result findOrThrow(Long id) {
        return resultRepository.findById(id).orElseThrow(() -> new ResultNotFoundException(id));
    }
}