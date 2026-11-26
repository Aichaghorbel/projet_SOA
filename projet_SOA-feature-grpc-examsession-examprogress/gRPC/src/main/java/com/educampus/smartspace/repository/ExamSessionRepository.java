package com.educampus.smartspace.repository;

import com.educampus.smartspace.model.ExamSession;
import com.educampus.smartspace.model.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    List<ExamSession> findByStudentId(Long studentId);
    List<ExamSession> findByExamId(Long examId);
    List<ExamSession> findByStatus(ExamStatus status);
    List<ExamSession> findByStudentIdAndExamId(Long studentId, Long examId);
}
