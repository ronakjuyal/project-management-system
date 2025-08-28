package com.pixelforge.nexus.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for project response data
 */
public class ProjectResponseDTO {

    private Long id;
    private String name;
    private String description;
    private LocalDate deadline;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private UserResponseDTO lead;
    private Set<UserResponseDTO> assignedDevelopers;
    private int documentCount;
    private boolean overdue;

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ProjectResponseDTO dto = new ProjectResponseDTO();

        public Builder id(Long id) { dto.id = id; return this; }
        public Builder name(String name) { dto.name = name; return this; }
        public Builder description(String description) { dto.description = description; return this; }
        public Builder deadline(LocalDate deadline) { dto.deadline = deadline; return this; }
        public Builder status(String status) { dto.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public Builder completedAt(LocalDateTime completedAt) { dto.completedAt = completedAt; return this; }
        public Builder lead(UserResponseDTO lead) { dto.lead = lead; return this; }
        public Builder assignedDevelopers(Set<UserResponseDTO> assignedDevelopers) {
            dto.assignedDevelopers = assignedDevelopers; return this;
        }
        public Builder documentCount(int documentCount) { dto.documentCount = documentCount; return this; }
        public Builder overdue(boolean overdue) { dto.overdue = overdue; return this; }

        public ProjectResponseDTO build() { return dto; }
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getDeadline() { return deadline; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public UserResponseDTO getLead() { return lead; }
    public Set<UserResponseDTO> getAssignedDevelopers() { return assignedDevelopers; }
    public int getDocumentCount() { return documentCount; }
    public boolean isOverdue() { return overdue; }
}
