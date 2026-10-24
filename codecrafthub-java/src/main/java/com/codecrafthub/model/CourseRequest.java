package com.codecrafthub.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Request body used for both course creation and updates. */
public class CourseRequest {
    private String name;
    private String description;
    @JsonProperty("target_date")
    private String targetDate;
    private String status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
