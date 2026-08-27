package com.vsu.researchapp.domain.exception;

public class OpportunityNotFoundException extends RuntimeException {
    public OpportunityNotFoundException(String message) {
        super(message);
    }
}
