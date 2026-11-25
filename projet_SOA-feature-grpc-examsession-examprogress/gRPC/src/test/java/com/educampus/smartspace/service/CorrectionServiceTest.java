package com.educampus.smartspace.service;

import com.educampus.smartspace.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("CorrectionService — Unit Tests")
class CorrectionServiceTest {

    private CorrectionService correctionService;

    @BeforeEach
    void setUp() {
        correctionService = new CorrectionService();
    }

    // ===== MCQ =====

    @Test
    @DisplayName("MCQ: correct option selected yields full points")
    void mcqCorrectOption() {
        Question q = buildMcqQuestion(10.0);
        ResponseOption correct = buildOption(q, "B", true);
        StudentAnswer answer = StudentAnswer.builder().question(q).selectedOption(correct).build();

        assertThat(correctionService.evaluate(q, answer)).isEqualTo(10.0);
    }

    @Test
    @DisplayName("MCQ: wrong option selected yields 0 points")
    void mcqWrongOption() {
        Question q = buildMcqQuestion(10.0);
        ResponseOption wrong = buildOption(q, "A", false);
        StudentAnswer answer = StudentAnswer.builder().question(q).selectedOption(wrong).build();

        assertThat(correctionService.evaluate(q, answer)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("MCQ: no option selected yields 0 points")
    void mcqNoOption() {
        Question q = buildMcqQuestion(10.0);
        StudentAnswer answer = StudentAnswer.builder().question(q).selectedOption(null).build();

        assertThat(correctionService.evaluate(q, answer)).isEqualTo(0.0);
    }

    // ===== Jaccard / SHORT_ANSWER =====

    @Test
    @DisplayName("SHORT_ANSWER: identical text yields full points")
    void jaccardIdentical() {
        Question q = buildShortAnswerQuestion(10.0, List.of("grpc uses protocol buffers"));
        StudentAnswer answer = StudentAnswer.builder().question(q)
                .textResponse("grpc uses protocol buffers").build();

        assertThat(correctionService.evaluate(q, answer)).isEqualTo(10.0);
    }

    @Test
    @DisplayName("SHORT_ANSWER: very similar text above threshold yields full points")
    void jaccardAboveThreshold() {
        Question q = buildShortAnswerQuestion(10.0,
                List.of("REST uses HTTP 1.1 with JSON while gRPC uses HTTP 2 with Protocol Buffers"));
        // Mostly overlapping tokens — should exceed 0.8 Jaccard
        StudentAnswer answer = StudentAnswer.builder().question(q)
                .textResponse("REST uses HTTP 1.1 with JSON but gRPC uses HTTP 2 with Protocol Buffers").build();

        assertThat(correctionService.evaluate(q, answer)).isEqualTo(10.0);
    }

    @Test
    @DisplayName("SHORT_ANSWER: empty response yields 0 points")
    void jaccardEmptyResponse() {
        Question q = buildShortAnswerQuestion(10.0, List.of("some reference answer"));
        StudentAnswer answer = StudentAnswer.builder().question(q).textResponse("").build();

        assertThat(correctionService.evaluate(q, answer)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("SHORT_ANSWER: null response yields 0 points")
    void jaccardNullResponse() {
        Question q = buildShortAnswerQuestion(10.0, List.of("some reference answer"));
        StudentAnswer answer = StudentAnswer.builder().question(q).textResponse(null).build();

        assertThat(correctionService.evaluate(q, answer)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("SHORT_ANSWER: no reference answers yields 0 points")
    void jaccardNoReferences() {
        Question q = buildShortAnswerQuestion(10.0, List.of());
        StudentAnswer answer = StudentAnswer.builder().question(q)
                .textResponse("some answer").build();

        assertThat(correctionService.evaluate(q, answer)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("SHORT_ANSWER: partial match yields partial points")
    void jaccardPartialMatch() {
        Question q = buildShortAnswerQuestion(10.0, List.of("the quick brown fox jumps over the lazy dog"));
        // Low overlap
        StudentAnswer answer = StudentAnswer.builder().question(q)
                .textResponse("the quick").build();

        double result = correctionService.evaluate(q, answer);
        assertThat(result).isGreaterThan(0.0).isLessThan(10.0);
    }

    // ===== Grade computation =====

    @Test
    @DisplayName("Grade A: score >= 90%")
    void gradeA() {
        assertThat(correctionService.computeGrade(9.5, 10.0)).isEqualTo("A");
    }

    @Test
    @DisplayName("Grade B: score >= 80%")
    void gradeB() {
        assertThat(correctionService.computeGrade(8.0, 10.0)).isEqualTo("B");
    }

    @Test
    @DisplayName("Grade C: score >= 70%")
    void gradeC() {
        assertThat(correctionService.computeGrade(7.0, 10.0)).isEqualTo("C");
    }

    @Test
    @DisplayName("Grade D: score >= 60%")
    void gradeD() {
        assertThat(correctionService.computeGrade(6.0, 10.0)).isEqualTo("D");
    }

    @Test
    @DisplayName("Grade F: score < 60%")
    void gradeF() {
        assertThat(correctionService.computeGrade(5.0, 10.0)).isEqualTo("F");
    }

    @Test
    @DisplayName("Grade F: maxScore = 0")
    void gradeFWhenMaxScoreZero() {
        assertThat(correctionService.computeGrade(0.0, 0.0)).isEqualTo("F");
    }

    // ===== Jaccard similarity =====

    @Test
    @DisplayName("Jaccard: same sets return 1.0")
    void jaccardSameSets() {
        assertThat(correctionService.computeJaccardSimilarity("hello world", "hello world")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Jaccard: disjoint sets return 0.0")
    void jaccardDisjointSets() {
        assertThat(correctionService.computeJaccardSimilarity("hello world", "foo bar")).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Jaccard: empty strings return 1.0")
    void jaccardBothEmpty() {
        assertThat(correctionService.computeJaccardSimilarity("", "")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Jaccard: one empty returns 0.0")
    void jaccardOneEmpty() {
        assertThat(correctionService.computeJaccardSimilarity("hello", "")).isEqualTo(0.0);
    }

    // ===== Null safety =====

    @Test
    @DisplayName("evaluate: null question returns 0.0")
    void evaluateNullQuestion() {
        StudentAnswer answer = StudentAnswer.builder().build();
        assertThat(correctionService.evaluate(null, answer)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("evaluate: null answer returns 0.0")
    void evaluateNullAnswer() {
        Question q = buildMcqQuestion(5.0);
        assertThat(correctionService.evaluate(q, null)).isEqualTo(0.0);
    }

    // ===== Helpers =====

    private Question buildMcqQuestion(double maxPoints) {
        return Question.builder()
                .id(1L)
                .examId(101L)
                .questionNumber(1)
                .type(QuestionType.MCQ)
                .text("Sample MCQ question?")
                .maxPoints(maxPoints)
                .build();
    }

    private Question buildShortAnswerQuestion(double maxPoints, List<String> references) {
        return Question.builder()
                .id(2L)
                .examId(101L)
                .questionNumber(2)
                .type(QuestionType.SHORT_ANSWER)
                .text("Sample short answer question?")
                .maxPoints(maxPoints)
                .referenceAnswers(references)
                .build();
    }

    private ResponseOption buildOption(Question question, String letter, boolean isCorrect) {
        return ResponseOption.builder()
                .id(isCorrect ? 2L : 1L)
                .question(question)
                .optionLetter(letter)
                .optionText("Option " + letter)
                .isCorrect(isCorrect)
                .build();
    }
}
