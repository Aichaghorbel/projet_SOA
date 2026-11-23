package com.educampus.smartspace.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "student_answers", indexes = {
    @Index(name = "idx_student_answers_session", columnList = "exam_session_id"),
    @Index(name = "idx_student_answers_question", columnList = "question_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_session_id", nullable = false)
    private ExamSession examSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private ResponseOption selectedOption;

    @Column(name = "text_response", columnDefinition = "TEXT")
    private String textResponse;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @OneToOne(mappedBy = "studentAnswer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ExamProgress examProgress;

    @PrePersist
    protected void onCreate() {
        if (this.submittedAt == null) this.submittedAt = Instant.now();
    }
}
