package com.educampus.smartspace.repository;

import com.educampus.smartspace.model.ExamProgress;
import com.educampus.smartspace.model.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamProgressRepository extends JpaRepository<ExamProgress, Long> {
    Optional<ExamProgress> findByStudentAnswerId(Long studentAnswerId);

    @Query("SELECT ep FROM ExamProgress ep " +
           "JOIN ep.studentAnswer sa " +
           "WHERE sa.examSession.id = :examSessionId " +
           "ORDER BY ep.timestamp ASC")
    List<ExamProgress> findByExamSessionId(@Param("examSessionId") Long examSessionId);

    @Query("SELECT ep FROM ExamProgress ep " +
           "JOIN ep.studentAnswer sa " +
           "WHERE sa.examSession.id = :examSessionId AND ep.status = :status")
    List<ExamProgress> findByExamSessionIdAndStatus(
            @Param("examSessionId") Long examSessionId,
            @Param("status") ProgressStatus status);
}
