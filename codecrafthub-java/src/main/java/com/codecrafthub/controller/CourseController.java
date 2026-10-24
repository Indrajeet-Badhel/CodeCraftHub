package com.codecrafthub.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.codecrafthub.model.Course;
import com.codecrafthub.model.CourseRequest;
import com.codecrafthub.service.CourseService;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) { this.courseService = courseService; }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody CourseRequest request) {
        Course course = courseService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Course created successfully", "course", course));
    }

    @GetMapping
    public Map<String, Object> getAll() {
        List<Course> courses = courseService.getAll();
        return Map.of("count", courses.size(), "courses", courses);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() { return courseService.stats(); }

    @GetMapping("/{id}")
    public Course getOne(@PathVariable long id) { return courseService.getById(id); }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable long id, @RequestBody CourseRequest request) {
        Course course = courseService.update(id, request);
        return Map.of("message", "Course updated successfully", "course", course);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable long id) {
        Course course = courseService.delete(id);
        return Map.of("message", "Course deleted successfully", "course", course);
    }
}
