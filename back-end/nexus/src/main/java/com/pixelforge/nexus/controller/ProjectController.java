package com.pixelforge.nexus.controller;

import com.pixelforge.nexus.dto.ProjectCreateDTO;
import com.pixelforge.nexus.dto.ProjectResponseDTO;
import com.pixelforge.nexus.dto.ProjectAssignmentDTO;
import com.pixelforge.nexus.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Project management controller
 * Handles project CRUD operations and assignments
 */
@RestController
@RequestMapping("/projects")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * Create new project (Admin only)
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponseDTO> createProject(@Valid @RequestBody ProjectCreateDTO projectCreateDTO,
                                                            Authentication authentication) {
        String creatorUsername = authentication.getName();
        ProjectResponseDTO createdProject = projectService.createProject(projectCreateDTO, creatorUsername);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    /**
     * Get all projects visible to current user
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsForUser(Authentication authentication) {
        String username = authentication.getName();
        List<ProjectResponseDTO> projects = projectService.getProjectsForUser(username);
        return ResponseEntity.ok(projects);
    }

    /**
     * Get all active projects (Admin only)
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProjectResponseDTO>> getActiveProjects() {
        List<ProjectResponseDTO> projects = projectService.getActiveProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Get overdue projects (Admin only)
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProjectResponseDTO>> getOverdueProjects() {
        List<ProjectResponseDTO> projects = projectService.getOverdueProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Get project by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long id,
                                                             Authentication authentication) {
        String username = authentication.getName();
        ProjectResponseDTO project = projectService.getProjectById(id, username);
        return ResponseEntity.ok(project);
    }

    /**
     * Update project (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponseDTO> updateProject(@PathVariable Long id,
                                                            @Valid @RequestBody ProjectCreateDTO projectCreateDTO) {
        ProjectResponseDTO updatedProject = projectService.updateProject(id, projectCreateDTO);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Assign developers to project
     */
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_LEAD')")
    public ResponseEntity<ProjectResponseDTO> assignDevelopersToProject(@PathVariable Long id,
                                                                        @Valid @RequestBody ProjectAssignmentDTO assignmentDTO,
                                                                        Authentication authentication) {
        String username = authentication.getName();
        ProjectResponseDTO updatedProject = projectService.assignDevelopersToProject(id, assignmentDTO, username);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Mark project as completed (Admin only)
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponseDTO> markProjectCompleted(@PathVariable Long id) {
        ProjectResponseDTO updatedProject = projectService.markProjectCompleted(id);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Reactivate completed project (Admin only)
     */
    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponseDTO> reactivateProject(@PathVariable Long id) {
        ProjectResponseDTO updatedProject = projectService.reactivateProject(id);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Delete project (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }
}