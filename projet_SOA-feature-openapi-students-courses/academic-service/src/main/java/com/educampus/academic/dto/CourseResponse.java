package com.educampus.academic.dto;

import com.educampus.academic.entity.Course;
import com.educampus.academic.entity.Semester;
import java.time.LocalDateTime;

/** Réponse des endpoints /api/v1/courses (schéma Course du contrat). */
public record CourseResponse(
        Long id,
        String code,
        String title,
        String description,
        Integer credits,
        Semester semester,
        String teacherName,
        Integer capacity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static CourseResponse from(Course c) {
        return new CourseResponse(c.getId(), c.getCode(), c.getTitle(), c.getDescription(),
                c.getCredits(), c.getSemester(), c.getTeacherName(), c.getCapacity(),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}
