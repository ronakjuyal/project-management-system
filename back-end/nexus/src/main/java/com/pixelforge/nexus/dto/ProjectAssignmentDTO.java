package com.pixelforge.nexus.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * DTO for project assignment requests
 */
public class ProjectAssignmentDTO {

    @NotEmpty(message = "At least one developer must be assigned")
    private Set<Long> developerIds;

    public ProjectAssignmentDTO() {}

    public Set<Long> getDeveloperIds() { return developerIds; }
    public void setDeveloperIds(Set<Long> developerIds) { this.developerIds = developerIds; }
}