package com.educampus.academic.repository;

import com.educampus.academic.entity.Exam;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByCourseIdOrderByExamDateAsc(Long courseId);
}