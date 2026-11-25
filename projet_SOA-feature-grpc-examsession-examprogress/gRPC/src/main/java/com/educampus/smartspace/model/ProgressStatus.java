package com.educampus.smartspace.model;

/**
 * Status of question correction progress events.
 */
public enum ProgressStatus {
    CORRECTING,    // Question is actively being evaluated
    CORRECTED,     // Question grading completed
    ERROR          // Error encountered during evaluation
}
