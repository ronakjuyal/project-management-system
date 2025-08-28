package com.pixelforge.nexus.dto;

import com.pixelforge.nexus.entity.Role;
import java.time.LocalDateTime;

/**
 * DTO for user response data
 */
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UserResponseDTO dto = new UserResponseDTO();

        public Builder id(Long id) { dto.id = id; return this; }
        public Builder username(String username) { dto.username = username; return this; }
        public Builder email(String email) { dto.email = email; return this; }
        public Builder firstName(String firstName) { dto.firstName = firstName; return this; }
        public Builder lastName(String lastName) { dto.lastName = lastName; return this; }
        public Builder role(Role role) { dto.role = role; return this; }
        public Builder enabled(boolean enabled) { dto.enabled = enabled; return this; }
        public Builder createdAt(LocalDateTime createdAt) { dto.createdAt = createdAt; return this; }

        public UserResponseDTO build() { return dto; }
    }

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Role getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}