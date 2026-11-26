package com.educampus.smartspace.service;

import com.educampus.smartspace.exception.ExamSessionNotFoundException;
import com.educampus.smartspace.exception.InvalidExamSubmissionException;
import com.educampus.smartspace.exception.QuestionNotFoundException;
import com.educampus.smartspace.model.*;
import com.educampus.smartspace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Orchestrates exam submission, automated correction (MCQ + Jaccard),
 * student answer persistence and streaming of progress events.
 *
 * Design: answers' correct data is ALWAYS fetched from DB — never trusted from client.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExamService {

    private final ExamSessionRepository examSessionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final ExamProgressRepository examProgressRepository;
    private final QuestionRepository questionRepository;
    private final ResponseOptionRepository responseOptionRepository;
    private final CorrectionService correctionService;

    // =====================================================================
    // Unary RPC — SubmitExam
    // =====================================================================

    /**
     * Processes all answers synchronously, persists results, and returns final score.
     */
    public ExamResult submitExam(Long examSessionId, Long studentId,
                                 Long examId, List<StudentAnswerInput> answerInputs) {

        log.info("SubmitExam (unary): session={}, student={}, exam={}, answers={}",
                examSessionId, studentId, examId, answerInputs != null ? answerInputs.size() : 0);

        ExamSession session = resolveSession(examSessionId, studentId, examId);
        session.setStatus(ExamStatus.IN_PROGRESS);
        examSessionRepository.save(session);

        List<Question> questions = questionRepository.findByExamIdOrderByQuestionNumberAsc(examId);
        if (questions.isEmpty()) {
            throw new QuestionNotFoundException("No questions found for examId=" + examId);
        }

        double totalScore = 0.0;

        if (answerInputs != null) {
            for (StudentAnswerInput input : answerInputs) {
                Question question = findQuestion(questions, input.questionId());
                StudentAnswer answer = persistStudentAnswer(session, question, input);
                double points = correctionService.evaluate(question, answer);
                totalScore += points;
                persistProgress(answer, ProgressStatus.CORRECTED, points);
            }
        }

        double maxScore = questions.stream().mapToDouble(Question::getMaxPoints).sum();
        session.setTotalScore(totalScore);
        session.setStatus(ExamStatus.COMPLETED);
        session.setSubmittedAt(Instant.now());
        examSessionRepository.save(session);

        String grade = correctionService.computeGrade(totalScore, maxScore);
        boolean passed = !grade.equals("F");

        log.info("SubmitExam complete: session={}, score={}/{}, grade={}", session.getId(), totalScore, maxScore, grade);
        return new ExamResult(session.getId(), totalScore, maxScore, grade, passed);
    }

    // =====================================================================
    // Server-Streaming RPC — StreamExamProgress
    // =====================================================================

    /**
     * Processes answers one-by-one, emitting a progress event per question.
     * Final event has isFinal=true.
     */
    public ExamResult streamExamProgress(Long examSessionId, Long studentId,
                                         Long examId, List<StudentAnswerInput> answerInputs,
                                         Consumer<ExamProgressEventDto> eventEmitter) {

        log.info("StreamExamProgress: session={}, student={}, exam={}", examSessionId, studentId, examId);

        ExamSession session = resolveSession(examSessionId, studentId, examId);
        session.setStatus(ExamStatus.IN_PROGRESS);
        examSessionRepository.save(session);

        List<Question> questions = questionRepository.findByExamIdOrderByQuestionNumberAsc(examId);
        if (questions.isEmpty()) {
            throw new QuestionNotFoundException("No questions found for examId=" + examId);
        }

        double totalScore = 0.0;
        int totalQuestions = answerInputs != null ? answerInputs.size() : 0;
        List<StudentAnswerInput> inputs = answerInputs != null ? answerInputs : List.of();

        for (int i = 0; i < inputs.size(); i++) {
            StudentAnswerInput input = inputs.get(i);
            Question question = findQuestion(questions, input.questionId());

            // Emit CORRECTING event
            if (eventEmitter != null) {
                eventEmitter.accept(new ExamProgressEventDto(
                        session.getId(), question.getId(), question.getQuestionNumber(),
                        ProgressStatus.CORRECTING, 0.0,
                        (int) Math.round(((double) i / Math.max(totalQuestions, 1)) * 100),
                        Instant.now(), false));
            }

            StudentAnswer answer = persistStudentAnswer(session, question, input);
            double points = correctionService.evaluate(question, answer);
            totalScore += points;
            ExamProgress progress = persistProgress(answer, ProgressStatus.CORRECTED, points);

            int progressPercentage = (int) Math.round(((i + 1.0) / Math.max(totalQuestions, 1)) * 100);
            boolean isFinal = (i == inputs.size() - 1);

            if (eventEmitter != null) {
                eventEmitter.accept(new ExamProgressEventDto(
                        session.getId(), question.getId(), question.getQuestionNumber(),
                        ProgressStatus.CORRECTED, points, progressPercentage,
                        progress.getTimestamp(), isFinal));
            }
        }

        double maxScore = questions.stream().mapToDouble(Question::getMaxPoints).sum();
        session.setTotalScore(totalScore);
        session.setStatus(ExamStatus.COMPLETED);
        session.setSubmittedAt(Instant.now());
        examSessionRepository.save(session);

        String grade = correctionService.computeGrade(totalScore, maxScore);
        boolean passed = !grade.equals("F");

        log.info("StreamExamProgress complete: session={}, score={}/{}", session.getId(), totalScore, maxScore);
        return new ExamResult(session.getId(), totalScore, maxScore, grade, passed);
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private ExamSession resolveSession(Long examSessionId, Long studentId, Long examId) {
        if (examSessionId != null && examSessionId > 0) {
            ExamSession session = examSessionRepository.findById(examSessionId)
                    .orElseThrow(() -> new ExamSessionNotFoundException(
                            "ExamSession not found: id=" + examSessionId));

            if (session.getStatus() == ExamStatus.COMPLETED || session.getStatus() == ExamStatus.SUBMITTED) {
                throw new InvalidExamSubmissionException(
                        "Exam session " + examSessionId + " has already been submitted and completed (status=" + session.getStatus() + "). Re-submission is not allowed.");
            }
            if (session.getExamId() != null && !session.getExamId().equals(examId)) {
                throw new InvalidExamSubmissionException(
                        "ExamSession " + examSessionId + " belongs to exam " + session.getExamId() + ", not exam " + examId);
            }
            if (session.getStudentId() != null && !session.getStudentId().equals(studentId)) {
                throw new InvalidExamSubmissionException(
                        "ExamSession " + examSessionId + " belongs to student " + session.getStudentId() + ", not student " + studentId);
            }
            return session;
        }
        // Create new session on-the-fly when session id=0 (for integration tests or new sessions)
        List<Question> qs = questionRepository.findByExamIdOrderByQuestionNumberAsc(examId);
        double maxScore = qs.stream().mapToDouble(Question::getMaxPoints).sum();
        ExamSession newSession = ExamSession.builder()
                .examId(examId)
                .studentId(studentId)
                .maxScore(maxScore > 0 ? maxScore : 100.0)
                .startedAt(Instant.now())
                .status(ExamStatus.NOT_STARTED)
                .build();
        return examSessionRepository.save(newSession);
    }

    private Question findQuestion(List<Question> questions, Long questionId) {
        return questions.stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new QuestionNotFoundException("Question not found: id=" + questionId));
    }

    private StudentAnswer persistStudentAnswer(ExamSession session, Question question,
                                               StudentAnswerInput input) {
        ResponseOption selectedOption = null;
        if (input.selectedOptionId() != null && input.selectedOptionId() > 0) {
            selectedOption = responseOptionRepository.findById(input.selectedOptionId())
                    .orElse(null);
        }

        // Upsert to handle re-tries or re-evaluations gracefully
        Optional<StudentAnswer> existingOpt = studentAnswerRepository
                .findByExamSessionIdAndQuestionId(session.getId(), question.getId());

        StudentAnswer answer;
        if (existingOpt.isPresent()) {
            answer = existingOpt.get();
            answer.setSelectedOption(selectedOption);
            answer.setTextResponse(input.textResponse());
            answer.setSubmittedAt(Instant.now());
        } else {
            answer = StudentAnswer.builder()
                    .examSession(session)
                    .question(question)
                    .selectedOption(selectedOption)
                    .textResponse(input.textResponse())
                    .submittedAt(Instant.now())
                    .build();
        }

        return studentAnswerRepository.save(answer);
    }

    private ExamProgress persistProgress(StudentAnswer answer, ProgressStatus status, double points) {
        Optional<ExamProgress> existingOpt = examProgressRepository
                .findByStudentAnswerId(answer.getId());

        ExamProgress progress;
        if (existingOpt.isPresent()) {
            progress = existingOpt.get();
            progress.setStatus(status);
            progress.setPointsEarned(points);
            progress.setTimestamp(Instant.now());
        } else {
            progress = ExamProgress.builder()
                    .studentAnswer(answer)
                    .status(status)
                    .pointsEarned(points)
                    .timestamp(Instant.now())
                    .build();
        }
        return examProgressRepository.save(progress);
    }

    // =====================================================================
    // DTOs
    // =====================================================================

    public record StudentAnswerInput(
            Long questionId,
            Long selectedOptionId,   // for MCQ; null or 0 if not applicable
            String textResponse      // for SHORT_ANSWER; null if not applicable
    ) {}

    public record ExamResult(
            Long examSessionId,
            double totalScore,
            double maxScore,
            String grade,
            boolean passed
    ) {}

    public record ExamProgressEventDto(
            Long examSessionId,
            Long questionId,
            Integer questionNumber,
            ProgressStatus status,
            double pointsEarned,
            int progressPercentage,
            Instant timestamp,
            boolean isFinal
    ) {}
}
