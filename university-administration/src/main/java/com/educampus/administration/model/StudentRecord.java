package com.educampus.administration.model;

import com.educampus.administration.generated.StudentStatus;

/** Vue administrative d'un etudiant (miroir de la table students de init-admin.sql). */
public record StudentRecord(
        String studentId,
        String firstName,
        String lastName,
        StudentStatus status,
        String currentAcademicYear) {
}
