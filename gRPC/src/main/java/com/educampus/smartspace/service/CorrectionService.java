package com.educampus.smartspace.service;

import com.educampus.smartspace.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure auto-correction logic: MCQ exact matching and Jaccard similarity for short answers.
 * Stateless service — no DB access, fully unit-testable.
 */
@Service
public class CorrectionService {

    public static final double JACCARD_THRESHOLD = 0.8;

    /**
     * Evaluates a StudentAnswer against its Question and optional selected ResponseOption.
     * Correct answers are fetched from the DB-side Question/ResponseOption — never from the client.
     */
    public double evaluate(Question question, StudentAnswer answer) {
        if (question == null || answer == null) return 0.0;

        return switch (question.getType()) {
            case MCQ -> evaluateMCQ(question, answer);
            case SHORT_ANSWER -> evaluateShortAnswer(question, answer);
        };
    }

    private double evaluateMCQ(Question question, StudentAnswer answer) {
        if (answer.getSelectedOption() == null) return 0.0;
        // Correct if the selected option has isCorrect == true
        return Boolean.TRUE.equals(answer.getSelectedOption().getIsCorrect())
                ? question.getMaxPoints()
                : 0.0;
    }

    private double evaluateShortAnswer(Question question, StudentAnswer answer) {
        String studentResponse = answer.getTextResponse();
        List<String> references = question.getReferenceAnswers();

        if (studentResponse == null || studentResponse.isBlank()
                || references == null || references.isEmpty()) {
            return 0.0;
        }

        double maxSim = references.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ref -> computeJaccardSimilarity(studentResponse, ref))
                .max()
                .orElse(0.0);

        if (maxSim >= JACCARD_THRESHOLD) {
            return question.getMaxPoints();
        }
        return Math.round((question.getMaxPoints() * maxSim) * 100.0) / 100.0;
    }

    public double computeJaccardSimilarity(String a, String b) {
        Set<String> tokensA = tokenize(a);
        Set<String> tokensB = tokenize(b);

        if (tokensA.isEmpty() && tokensB.isEmpty()) return 1.0;
        if (tokensA.isEmpty() || tokensB.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(tokensA);
        intersection.retainAll(tokensB);

        Set<String> union = new HashSet<>(tokensA);
        union.addAll(tokensB);

        return (double) intersection.size() / union.size();
    }

    public Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("\\s+"))
                .map(token -> token.replaceAll("[^a-z0-9]", ""))
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toSet());
    }

    public String computeGrade(double score, double maxScore) {
        if (maxScore <= 0.0) return "F";
        double pct = (score / maxScore) * 100.0;
        if (pct >= 90.0) return "A";
        if (pct >= 80.0) return "B";
        if (pct >= 70.0) return "C";
        if (pct >= 60.0) return "D";
        return "F";
    }
}
