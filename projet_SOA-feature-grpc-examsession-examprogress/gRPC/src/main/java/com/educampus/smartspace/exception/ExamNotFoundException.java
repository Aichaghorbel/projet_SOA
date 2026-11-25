package com.educampus.smartspace.exception;

public class ExamNotFoundException extends RuntimeException {
    public ExamNotFoundException(String message) {
        super(message);
    }
    public ExamNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
