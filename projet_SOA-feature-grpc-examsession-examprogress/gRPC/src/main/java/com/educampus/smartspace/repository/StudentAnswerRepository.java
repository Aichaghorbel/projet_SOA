package com.educampus.smartspace.repository;

import com.educampus.smartspace.model.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findByExamSessionId(Long examSessionId);
    List<StudentAnswer> findByExamSessionIdOrderByQuestionIdAsc(Long examSessionId);
    Optional<StudentAnswer> findByExamSessionIdAndQuestionId(Long examSessionId, Long questionId);
}
