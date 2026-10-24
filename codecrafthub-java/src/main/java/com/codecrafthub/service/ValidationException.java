package com.codecrafthub.service;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<String> details;

    public ValidationException(List<String> details) {
        super("Validation failed");
        this.details = details;
    }

    public List<String> getDetails() { return details; }
}
