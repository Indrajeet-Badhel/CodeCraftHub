package com.codecrafthub;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "courses.file-path=target/test-courses.json")
class CourseApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanDataFile() throws Exception {
        Files.deleteIfExists(Path.of("target/test-courses.json"));
    }

    @Test
    void crudStatisticsAndErrorsWork() throws Exception {
        String course = "{\"name\":\"Python Basics\",\"description\":\"Learn Python fundamentals\",\"target_date\":\"2026-12-31\",\"status\":\"Not Started\"}";
        mockMvc.perform(post("/api/courses").contentType(MediaType.APPLICATION_JSON).content(course))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.course.id").value(1));
        mockMvc.perform(get("/api/courses")).andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1));
        mockMvc.perform(get("/api/courses/1")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Python Basics"));
        mockMvc.perform(put("/api/courses/1").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"In Progress\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.course.status").value("In Progress"));
        mockMvc.perform(get("/api/courses/stats")).andExpect(status().isOk())
            .andExpect(jsonPath("$.total_courses").value(1)).andExpect(jsonPath("$.courses_by_status['In Progress']").value(1));
        mockMvc.perform(post("/api/courses").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/courses/999")).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/courses/1")).andExpect(status().isOk());
    }
}
