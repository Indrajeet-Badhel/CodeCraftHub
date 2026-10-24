package com.codecrafthub.service;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(long id) {
        super("Course not found: " + id);
    }
}
