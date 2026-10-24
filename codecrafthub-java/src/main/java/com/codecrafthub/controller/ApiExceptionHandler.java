package com.codecrafthub.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.codecrafthub.service.CourseNotFoundException;
import com.codecrafthub.service.StorageException;
import com.codecrafthub.service.ValidationException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    ResponseEntity<Map<String, Object>> validation(ValidationException error) {
        return ResponseEntity.badRequest().body(Map.of("error", "Validation failed", "details", error.getDetails()));
    }

    @ExceptionHandler(CourseNotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(CourseNotFoundException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Course not found"));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<Map<String, String>> badRequest(Exception error) {
        return ResponseEntity.badRequest().body(Map.of("error", "Request body or course ID is invalid"));
    }

    @ExceptionHandler(StorageException.class)
    ResponseEntity<Map<String, String>> storage(StorageException error) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", error.getMessage()));
    }
}
