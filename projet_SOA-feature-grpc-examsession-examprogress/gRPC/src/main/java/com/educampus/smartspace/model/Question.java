package com.educampus.smartspace.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions", indexes = {
    @Index(name = "idx_questions_exam_id", columnList = "exam_id"),
    @Index(name = "idx_questions_number", columnList = "exam_id, question_number")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private QuestionType type;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "max_points", nullable = false)
    private Double maxPoints;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_reference_answers",
        joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "reference_answer", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> referenceAnswers = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ResponseOption> options = new ArrayList<>();
}
