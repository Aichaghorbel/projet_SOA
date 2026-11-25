package com.educampus.academic.dto;

import com.educampus.academic.entity.Student;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Réponse des endpoints /api/v1/students (schéma Student du contrat). */
public record StudentResponse(
        Long id,
        String studentNumber,
        String firstName,
        String lastName,
        String email,
        LocalDate dateOfBirth,
        String phone,
        String program,
        Integer level,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static StudentResponse from(Student s) {
        return new StudentResponse(s.getId(), s.getStudentNumber(), s.getFirstName(),
                s.getLastName(), s.getEmail(), s.getDateOfBirth(), s.getPhone(),
                s.getProgram(), s.getLevel(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
