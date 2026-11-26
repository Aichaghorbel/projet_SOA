package com.educampus.academic.repository;

import com.educampus.academic.entity.Result;
import com.educampus.academic.entity.SessionType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ResultRepository
        extends JpaRepository<Result, Long>, JpaSpecificationExecutor<Result> {

    List<Result> findByEnrollmentIdOrderByIdAsc(Long enrollmentId);

    boolean existsByEnrollmentId(Long enrollmentId);

    /** examId null est traduit en "exam_id is null" par Spring Data. */
    boolean existsByEnrollmentIdAndExamIdAndSessionType(Long enrollmentId, Long examId, SessionType sessionType);

    boolean existsByEnrollmentIdAndExamIdAndSessionTypeAndIdNot(
            Long enrollmentId, Long examId, SessionType sessionType, Long id);
}