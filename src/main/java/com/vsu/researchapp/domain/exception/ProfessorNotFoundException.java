package com.vsu.researchapp.domain.exception;

public class ProfessorNotFoundException extends RuntimeException {
    public ProfessorNotFoundException(String message) {
        super(message);
    }
}
