package com.educampus.academic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Corps des requêtes POST / PUT /api/v1/students (schéma StudentRequest du contrat). */
public record StudentRequest(
        @NotBlank(message = "le numéro étudiant est obligatoire")
        @Pattern(regexp = "^[A-Za-z0-9-]{4,20}$",
                message = "4 à 20 caractères : lettres, chiffres ou tiret")
        String studentNumber,

        @NotBlank(message = "le prénom est obligatoire")
        @Size(min = 2, max = 100, message = "le prénom doit contenir 2 à 100 caractères")
        String firstName,

        @NotBlank(message = "le nom est obligatoire")
        @Size(min = 2, max = 100, message = "le nom doit contenir 2 à 100 caractères")
        String lastName,

        @NotBlank(message = "l'email est obligatoire")
        @Email(message = "doit être une adresse email valide")
        @Size(max = 150, message = "150 caractères maximum")
        String email,

        @Past(message = "la date de naissance doit être dans le passé")
        LocalDate dateOfBirth,

        @Pattern(regexp = "^\\+?[0-9 ]{8,20}$",
                message = "téléphone invalide (8 à 20 chiffres, + autorisé au début)")
        String phone,

        @Size(max = 100, message = "100 caractères maximum")
        String program,

        @NotNull(message = "le niveau est obligatoire")
        @Min(value = 1, message = "le niveau minimum est 1")
        @Max(value = 5, message = "le niveau maximum est 5")
        Integer level) {
}
