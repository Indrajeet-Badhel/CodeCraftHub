package com.codecrafthub;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.codecrafthub.service.CourseService;

@SpringBootApplication
public class CodeCraftHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeCraftHubApplication.class, args);
    }

    @Bean
    CommandLineRunner showStartupMessage(CourseService courseService) {
        return args -> {
            courseService.ensureDataFile();
            System.out.println("CodeCraftHub API is starting...");
            System.out.println("Data will be stored in: " + courseService.getDataFile());
            System.out.println("API will be available at: http://localhost:8080");
        };
    }
}
