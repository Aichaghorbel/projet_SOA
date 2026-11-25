package com.educampus.smartspace.controller;

import com.educampus.smartspace.exception.ExamSessionNotFoundException;
import com.educampus.smartspace.model.ExamSession;
import com.educampus.smartspace.model.Question;
import com.educampus.smartspace.repository.ExamSessionRepository;
import com.educampus.smartspace.repository.QuestionRepository;
import com.educampus.smartspace.service.ExamService;
import com.educampus.smartspace.service.ExamService.ExamResult;
import com.educampus.smartspace.service.ExamService.StudentAnswerInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing exam management endpoints for Academic service and web clients.
 * Provides decoupled HTTP access alongside the gRPC adapter.
 */
@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Slf4j
public class ExamController {

    private final ExamService examService;
    private final ExamSessionRepository examSessionRepository;
    private final QuestionRepository questionRepository;

    /**
     * Get exam session details by ID.
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ExamSession> getSession(@PathVariable Long sessionId) {
        log.info("REST: Fetching session {}", sessionId);
        return examSessionRepository.findById(sessionId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ExamSessionNotFoundException("Exam session not found: " + sessionId));
    }

    /**
     * Get all questions for an exam.
     */
    @GetMapping("/{examId}/questions")
    public ResponseEntity<List<Question>> getQuestionsByExam(@PathVariable Long examId) {
        log.info("REST: Fetching questions for exam {}", examId);
        List<Question> questions = questionRepository.findByExamIdOrderByQuestionNumberAsc(examId);
        return ResponseEntity.ok(questions);
    }

    /**
     * Submit an exam session via REST.
     */
    @PostMapping("/sessions/{sessionId}/submit")
    public ResponseEntity<ExamResult> submitExam(
            @PathVariable Long sessionId,
            @RequestParam Long studentId,
            @RequestParam Long examId,
            @RequestBody @Valid List<StudentAnswerInput> answers) {
        log.info("REST: Submitting exam session {}, student {}, exam {}", sessionId, studentId, examId);
        ExamResult result = examService.submitExam(sessionId, studentId, examId, answers);
        return ResponseEntity.ok(result);
    }
}
