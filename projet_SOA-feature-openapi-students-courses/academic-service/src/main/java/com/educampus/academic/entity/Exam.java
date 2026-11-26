package com.educampus.academic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité Exam. Table {@code exams}. {@code roomId} est une référence logique
 * vers le service Smart Space (pas de clé étrangère entre services).
 */
@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "exam_date", nullable = false)
    private LocalDateTime examDate;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "max_grade", nullable = false, precision = 4, scale = 2)
    private BigDecimal maxGrade = new BigDecimal("20.00");

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDateTime examDate) {
        this.examDate = examDate;
    }

    public Integer getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(Integer durationMin) {
        this.durationMin = durationMin;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public BigDecimal getMaxGrade() {
        return maxGrade;
    }

    public void setMaxGrade(BigDecimal maxGrade) {
        this.maxGrade = maxGrade;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}