package com.educampus.academic.exception;

/**
 * Règle métier violée sur un résultat (inscription annulée, examen d'un autre
 * cours, note au-dessus du maximum, résultat publié non supprimable) : traduite en 409.
 */
public class ResultConflictException extends DuplicateResourceException {

    public ResultConflictException(String message) {
        super(message);
    }
}