package com.educampus.academic.dto;

/** Détail d'une erreur de validation sur un champ (schéma FieldError du contrat). */
public record FieldErrorDetail(String field, String message) {
}
