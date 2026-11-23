package com.educampus.smartspace.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "response_options", indexes = {
    @Index(name = "idx_response_options_question", columnList = "question_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResponseOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "option_letter", nullable = false, length = 5)
    private String optionLetter;

    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
}
