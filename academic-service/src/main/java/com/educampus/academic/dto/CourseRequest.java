package com.educampus.academic.dto;

import com.educampus.academic.entity.Semester;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Corps des requêtes POST / PUT /api/v1/courses (schéma CourseRequest du contrat). */
public record CourseRequest(
        @NotBlank(message = "le code est obligatoire")
        @Pattern(regexp = "^[A-Z]{2,5}[0-9]{2,4}$",
                message = "format attendu : 2 à 5 lettres majuscules + 2 à 4 chiffres (ex. SOA301)")
        String code,

        @NotBlank(message = "le titre est obligatoire")
        @Size(min = 3, max = 150, message = "le titre doit contenir 3 à 150 caractères")
        String title,

        @Size(max = 1000, message = "1000 caractères maximum")
        String description,

        @NotNull(message = "les crédits sont obligatoires")
        @Min(value = 1, message = "minimum 1 crédit")
        @Max(value = 30, message = "maximum 30 crédits")
        Integer credits,

        @NotNull(message = "le semestre est obligatoire (S1 à S6)")
        Semester semester,

        @Size(max = 100, message = "100 caractères maximum")
        String teacherName,

        @Min(value = 1, message = "la capacité minimum est 1")
        @Max(value = 500, message = "la capacité maximum est 500")
        Integer capacity) {
}
