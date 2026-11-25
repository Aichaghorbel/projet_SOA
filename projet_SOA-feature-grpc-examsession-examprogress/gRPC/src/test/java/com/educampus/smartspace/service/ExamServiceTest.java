package com.educampus.smartspace.service;

import com.educampus.smartspace.exception.ExamSessionNotFoundException;
import com.educampus.smartspace.exception.QuestionNotFoundException;
import com.educampus.smartspace.model.*;
import com.educampus.smartspace.repository.*;
import com.educampus.smartspace.service.ExamService.ExamProgressEventDto;
import com.educampus.smartspace.service.ExamService.ExamResult;
import com.educampus.smartspace.service.ExamService.StudentAnswerInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExamService — Unit Tests")
class ExamServiceTest {

    @Mock
    private ExamSessionRepository examSessionRepository;

    @Mock
    private StudentAnswerRepository studentAnswerRepository;

    @Mock
    private ExamProgressRepository examProgressRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ResponseOptionRepository responseOptionRepository;

    private CorrectionService correctionService;
    private ExamService examService;

    @BeforeEach
    void setUp() {
        correctionService = new CorrectionService();
        examService = new ExamService(
                examSessionRepository,
                studentAnswerRepository,
                examProgressRepository,
                questionRepository,
                responseOptionRepository,
                correctionService
        );
    }

    // =========================================================================
    // SubmitExam (Unary) Tests
    // =========================================================================

    @Test
    @DisplayName("SubmitExam: Successfully evaluates MCQ and SHORT_ANSWER, computes scores, and marks COMPLETED")
    void testSubmitExamSuccess() {
        Long sessionId = 1L;
        Long studentId = 42L;
        Long examId = 101L;

        ExamSession session = ExamSession.builder()
                .id(sessionId)
                .examId(examId)
                .studentId(studentId)
                .maxScore(10.0)
                .status(ExamStatus.NOT_STARTED)
                .startedAt(Instant.now())
                .build();

        Question q1 = Question.builder()
                .id(10L)
                .examId(examId)
                .questionNumber(1)
                .type(QuestionType.MCQ)
                .text("What protocol is used by gRPC?")
                .maxPoints(5.0)
                .build();

        Question q2 = Question.builder()
                .id(20L)
                .examId(examId)
                .questionNumber(2)
                .type(QuestionType.SHORT_ANSWER)
                .text("Define microservices")
                .maxPoints(5.0)
                .referenceAnswers(List.of("independently deployable modular services"))
                .build();

        ResponseOption correctOption = ResponseOption.builder()
                .id(100L)
                .question(q1)
                .optionLetter("B")
                .optionText("HTTP/2")
                .isCorrect(true)
                .build();

        when(examSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(examSessionRepository.save(any(ExamSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.findByExamIdOrderByQuestionNumberAsc(examId)).thenReturn(List.of(q1, q2));
        when(responseOptionRepository.findById(100L)).thenReturn(Optional.of(correctOption));
        when(studentAnswerRepository.save(any(StudentAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examProgressRepository.save(any(ExamProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        List<StudentAnswerInput> answers = List.of(
                new StudentAnswerInput(10L, 100L, null),
                new StudentAnswerInput(20L, null, "independently deployable modular services")
        );

        ExamResult result = examService.submitExam(sessionId, studentId, examId, answers);

        assertThat(result).isNotNull();
        assertThat(result.examSessionId()).isEqualTo(sessionId);
        assertThat(result.totalScore()).isEqualTo(10.0);
        assertThat(result.maxScore()).isEqualTo(10.0);
        assertThat(result.grade()).isEqualTo("A");
        assertThat(result.passed()).isTrue();

        assertThat(session.getStatus()).isEqualTo(ExamStatus.COMPLETED);
        assertThat(session.getSubmittedAt()).isNotNull();
        assertThat(session.getTotalScore()).isEqualTo(10.0);

        verify(studentAnswerRepository, times(2)).save(any(StudentAnswer.class));
        verify(examProgressRepository, times(2)).save(any(ExamProgress.class));
    }

    @Test
    @DisplayName("SubmitExam: Non-existent session throws ExamSessionNotFoundException")
    void testSubmitExamSessionNotFound() {
        when(examSessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> examService.submitExam(999L, 1L, 101L, List.of()))
                .isInstanceOf(ExamSessionNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("SubmitExam: Missing questions for exam throws QuestionNotFoundException")
    void testSubmitExamNoQuestionsFound() {
        ExamSession session = ExamSession.builder()
                .id(1L)
                .examId(101L)
                .studentId(42L)
                .maxScore(10.0)
                .status(ExamStatus.NOT_STARTED)
                .startedAt(Instant.now())
                .build();

        when(examSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(questionRepository.findByExamIdOrderByQuestionNumberAsc(101L)).thenReturn(List.of());

        assertThatThrownBy(() -> examService.submitExam(1L, 42L, 101L, List.of()))
                .isInstanceOf(QuestionNotFoundException.class)
                .hasMessageContaining("101");
    }

    // =========================================================================
    // StreamExamProgress (Server-Streaming) Tests
    // =========================================================================

    @Test
    @DisplayName("StreamExamProgress: Streams progress events per question with progressive percentages")
    void testStreamExamProgress() {
        Long sessionId = 2L;
        Long studentId = 55L;
        Long examId = 202L;

        ExamSession session = ExamSession.builder()
                .id(sessionId)
                .examId(examId)
                .studentId(studentId)
                .maxScore(10.0)
                .status(ExamStatus.NOT_STARTED)
                .startedAt(Instant.now())
                .build();

        Question q1 = Question.builder()
                .id(1L)
                .examId(examId)
                .questionNumber(1)
                .type(QuestionType.MCQ)
                .text("MCQ 1")
                .maxPoints(5.0)
                .build();

        Question q2 = Question.builder()
                .id(2L)
                .examId(examId)
                .questionNumber(2)
                .type(QuestionType.MCQ)
                .text("MCQ 2")
                .maxPoints(5.0)
                .build();

        ResponseOption opt1 = ResponseOption.builder().id(11L).question(q1).optionLetter("A").isCorrect(true).build();
        ResponseOption opt2 = ResponseOption.builder().id(12L).question(q2).optionLetter("B").isCorrect(true).build();

        when(examSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(examSessionRepository.save(any(ExamSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.findByExamIdOrderByQuestionNumberAsc(examId)).thenReturn(List.of(q1, q2));
        when(responseOptionRepository.findById(11L)).thenReturn(Optional.of(opt1));
        when(responseOptionRepository.findById(12L)).thenReturn(Optional.of(opt2));
        when(studentAnswerRepository.save(any(StudentAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examProgressRepository.save(any(ExamProgress.class))).thenAnswer(inv -> {
            ExamProgress ep = inv.getArgument(0);
            ep.setId(100L);
            return ep;
        });

        List<StudentAnswerInput> answers = List.of(
                new StudentAnswerInput(1L, 11L, null),
                new StudentAnswerInput(2L, 12L, null)
        );

        List<ExamProgressEventDto> events = new ArrayList<>();
        ExamResult result = examService.streamExamProgress(sessionId, studentId, examId, answers, events::add);

        assertThat(result.passed()).isTrue();
        assertThat(result.totalScore()).isEqualTo(10.0);

        // Filter CORRECTED events
        List<ExamProgressEventDto> corrected = events.stream()
                .filter(e -> e.status() == ProgressStatus.CORRECTED)
                .toList();

        assertThat(corrected).hasSize(2);
        assertThat(corrected.get(0).questionNumber()).isEqualTo(1);
        assertThat(corrected.get(0).progressPercentage()).isEqualTo(50);
        assertThat(corrected.get(0).isFinal()).isFalse();

        assertThat(corrected.get(1).questionNumber()).isEqualTo(2);
        assertThat(corrected.get(1).progressPercentage()).isEqualTo(100);
        assertThat(corrected.get(1).isFinal()).isTrue();
    }
}
