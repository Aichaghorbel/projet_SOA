package com.educampus.smartspace.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_sessions", indexes = {
    @Index(name = "idx_exam_sessions_student", columnList = "student_id"),
    @Index(name = "idx_exam_sessions_exam", columnList = "exam_id"),
    @Index(name = "idx_exam_sessions_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ExamStatus status = ExamStatus.NOT_STARTED;

    @Column(name = "max_score", nullable = false)
    private Double maxScore;

    @Column(name = "total_score")
    private Double totalScore;

    @OneToMany(mappedBy = "examSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StudentAnswer> studentAnswers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.status == null) this.status = ExamStatus.NOT_STARTED;
        if (this.startedAt == null) this.startedAt = Instant.now();
    }
}
