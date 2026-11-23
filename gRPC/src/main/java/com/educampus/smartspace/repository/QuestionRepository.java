package com.educampus.smartspace.repository;

import com.educampus.smartspace.model.Question;
import com.educampus.smartspace.model.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByExamIdOrderByQuestionNumberAsc(Long examId);
    Optional<Question> findByExamIdAndQuestionNumber(Long examId, Integer questionNumber);
    List<Question> findByExamIdAndType(Long examId, QuestionType type);
    long countByExamId(Long examId);
}
