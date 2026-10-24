package com.codecrafthub.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.codecrafthub.model.Course;
import com.codecrafthub.model.CourseRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CourseService {
    private static final List<String> VALID_STATUSES = List.of("Not Started", "In Progress", "Completed");
    private final ObjectMapper objectMapper;
    private final Path dataFile;

    public CourseService(ObjectMapper objectMapper, @Value("${courses.file-path:courses.json}") String filePath) {
        this.objectMapper = objectMapper;
        this.dataFile = Path.of(filePath).toAbsolutePath();
    }

    public Path getDataFile() { return dataFile; }

    public synchronized void ensureDataFile() {
        try {
            Path parent = dataFile.getParent();
            if (parent != null) Files.createDirectories(parent);
            if (Files.notExists(dataFile)) Files.writeString(dataFile, "[]\n");
        } catch (IOException error) {
            throw new StorageException("Unable to create courses.json", error);
        }
    }

    public synchronized List<Course> getAll() { return readCourses(); }

    public synchronized Course getById(long id) {
        return readCourses().stream().filter(course -> course.getId() == id).findFirst()
            .orElseThrow(() -> new CourseNotFoundException(id));
    }

    public synchronized Course create(CourseRequest request) {
        validate(request, true);
        List<Course> courses = readCourses();
        Course course = new Course();
        course.setId(courses.stream().map(Course::getId).max(Comparator.naturalOrder()).orElse(0L) + 1);
        apply(course, request);
        course.setCreatedAt(Instant.now().toString());
        courses.add(course);
        writeCourses(courses);
        return course;
    }

    public synchronized Course update(long id, CourseRequest request) {
        validate(request, false);
        List<Course> courses = readCourses();
        Course course = courses.stream().filter(item -> item.getId() == id).findFirst()
            .orElseThrow(() -> new CourseNotFoundException(id));
        apply(course, request);
        course.setUpdatedAt(Instant.now().toString());
        writeCourses(courses);
        return course;
    }

    public synchronized Course delete(long id) {
        List<Course> courses = readCourses();
        Course course = courses.stream().filter(item -> item.getId() == id).findFirst()
            .orElseThrow(() -> new CourseNotFoundException(id));
        courses.remove(course);
        writeCourses(courses);
        return course;
    }

    public synchronized Map<String, Object> stats() {
        List<Course> courses = readCourses();
        Map<String, Long> byStatus = VALID_STATUSES.stream().collect(Collectors.toMap(
            status -> status,
            status -> courses.stream().filter(course -> status.equals(course.getStatus())).count(),
            (left, right) -> left,
            java.util.LinkedHashMap::new
        ));
        return Map.of("total_courses", courses.size(), "courses_by_status", byStatus);
    }

    private void validate(CourseRequest request, boolean creating) {
        List<String> errors = new ArrayList<>();
        if (creating && blank(request.getName())) errors.add("name is required");
        if (creating && blank(request.getDescription())) errors.add("description is required");
        if (creating && blank(request.getTargetDate())) errors.add("target_date is required");
        if (creating && blank(request.getStatus())) errors.add("status is required");
        if (!creating && blank(request.getName()) && blank(request.getDescription()) && blank(request.getTargetDate()) && blank(request.getStatus())) {
            errors.add("provide at least one field to update");
        }
        if (!blank(request.getTargetDate()) && !validDate(request.getTargetDate())) {
            errors.add("target_date must be a real date in YYYY-MM-DD format");
        }
        if (!blank(request.getStatus()) && !VALID_STATUSES.contains(request.getStatus())) {
            errors.add("status must be one of: " + String.join(", ", VALID_STATUSES));
        }
        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }

    private boolean validDate(String value) {
        try { LocalDate.parse(value); return true; }
        catch (DateTimeParseException error) { return false; }
    }

    private void apply(Course course, CourseRequest request) {
        if (!blank(request.getName())) course.setName(request.getName().trim());
        if (!blank(request.getDescription())) course.setDescription(request.getDescription().trim());
        if (!blank(request.getTargetDate())) course.setTargetDate(request.getTargetDate());
        if (!blank(request.getStatus())) course.setStatus(request.getStatus());
    }

    private List<Course> readCourses() {
        ensureDataFile();
        try {
            return objectMapper.readValue(dataFile.toFile(), new TypeReference<List<Course>>() { });
        } catch (IOException error) {
            throw new StorageException("Unable to read courses.json", error);
        }
    }

    private void writeCourses(List<Course> courses) {
        try {
            Path temporaryFile = Files.createTempFile(dataFile.getParent(), "courses-", ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporaryFile.toFile(), courses);
            Files.move(temporaryFile, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException error) {
            throw new StorageException("Unable to write courses.json", error);
        }
    }
}
