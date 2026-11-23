package com.educampus.smartspace.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "exam_progress", indexes = {
    @Index(name = "idx_exam_progress_student_answer", columnList = "student_answer_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_answer_id", nullable = false)
    private StudentAnswer studentAnswer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProgressStatus status;

    @Column(name = "points_earned")
    private Double pointsEarned;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) this.timestamp = Instant.now();
    }
}
