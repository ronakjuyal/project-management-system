package com.pixelforge.nexus.service;

import com.pixelforge.nexus.entity.Project;
import com.pixelforge.nexus.entity.User;
import com.pixelforge.nexus.entity.Role;
import com.pixelforge.nexus.repository.ProjectRepository;
import com.pixelforge.nexus.repository.DocumentRepository;
import com.pixelforge.nexus.dto.ProjectCreateDTO;
import com.pixelforge.nexus.dto.ProjectResponseDTO;
import com.pixelforge.nexus.dto.ProjectAssignmentDTO;
import com.pixelforge.nexus.dto.UserResponseDTO;
import com.pixelforge.nexus.exception.ResourceNotFoundException;
import com.pixelforge.nexus.exception.InvalidOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for Project management operations
 * Handles project CRUD, assignments, and business logic
 */
@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserService userService;

    /**
     * Create a new project (Admin only)
     * @param projectCreateDTO Project creation data
     * @param creatorUsername Username of the creator
     * @return Created project response
     */
    @PreAuthorize("hasRole('ADMIN')")
    public ProjectResponseDTO createProject(ProjectCreateDTO projectCreateDTO, String creatorUsername) {
        // Get project lead if specified
        User lead = null;
        if (projectCreateDTO.getLeadId() != null) {
            lead = userService.getUserEntityById(projectCreateDTO.getLeadId());

            // Validate that the lead has appropriate role
            if (!lead.getRole().canLeadProjects()) {
                throw new InvalidOperationException("User must be a Project Lead or Admin to lead projects");
            }
        }

        // Create new project
        Project project = new Project();
        project.setName(projectCreateDTO.getName());
        project.setDescription(projectCreateDTO.getDescription());
        project.setDeadline(projectCreateDTO.getDeadline());
        project.setLead(lead);

        Project savedProject = projectRepository.save(project);
        return convertToResponseDTO(savedProject);
    }

    /**
     * Get project by ID
     * @param projectId Project ID
     * @param currentUsername Current user's username
     * @return Project response
     */
    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(Long projectId, String currentUsername) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        // Check if user has access to this project
        User currentUser = userService.getUserEntityByUsername(currentUsername);
        if (!hasAccessToProject(project, currentUser)) {
            throw new InvalidOperationException("You don't have access to this project");
        }

        return convertToResponseDTO(project);
    }

    /**
     * Get all projects visible to the current user
     * @param currentUsername Current user's username
     * @return List of project responses
     */
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getProjectsForUser(String currentUsername) {
        User currentUser = userService.getUserEntityByUsername(currentUsername);
        List<Project> projects;

        switch (currentUser.getRole()) {
            case ADMIN:
                // Admins can see all projects
                projects = projectRepository.findAll();
                break;
            case PROJECT_LEAD:
                // Project leads can see projects they lead
                projects = projectRepository.findByLead(currentUser);
                break;
            case DEVELOPER:
                // Developers can see projects they're assigned to
                projects = projectRepository.findProjectsAssignedToDeveloper(currentUser);
                break;
            default:
                projects = List.of();
        }

        return projects.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all active projects
     * @return List of active projects
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProjectResponseDTO> getActiveProjects() {
        return projectRepository.findActiveProjects()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Assign developers to a project
     * @param projectId Project ID
     * @param assignmentDTO Assignment data
     * @param currentUsername Current user's username
     * @return Updated project response
     */
    public ProjectResponseDTO assignDevelopersToProject(Long projectId, ProjectAssignmentDTO assignmentDTO,
                                                        String currentUsername) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        User currentUser = userService.getUserEntityByUsername(currentUsername);

        // Check if user can assign developers to this project
        if (!canAssignDevelopers(project, currentUser)) {
            throw new InvalidOperationException("You don't have permission to assign developers to this project");
        }

        // Get developer users
        Set<User> developers = assignmentDTO.getDeveloperIds().stream()
                .map(userService::getUserEntityById)
                .collect(Collectors.toSet());

        // Validate all users are developers
        developers.forEach(developer -> {
            if (developer.getRole() != Role.DEVELOPER) {
                throw new InvalidOperationException("User " + developer.getUsername() + " is not a developer");
            }
        });

        project.setAssignedDevelopers(developers);
        Project updatedProject = projectRepository.save(project);
        return convertToResponseDTO(updatedProject);
    }

    /**
     * Mark project as completed (Admin only)
     * @param projectId Project ID
     * @return Updated project response
     */
    @PreAuthorize("hasRole('ADMIN')")
    public ProjectResponseDTO markProjectCompleted(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        project.markAsCompleted();
        Project updatedProject = projectRepository.save(project);
        return convertToResponseDTO(updatedProject);
    }

    /**
     * Reactivate completed project (Admin only)
     * @param projectId Project ID
     * @return Updated project response
     */
    @PreAuthorize("hasRole('ADMIN')")
    public ProjectResponseDTO reactivateProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        project.reactivate();
        Project updatedProject = projectRepository.save(project);
        return convertToResponseDTO(updatedProject);
    }

    /**
     * Update project details (Admin only)
     * @param projectId Project ID
     * @param projectCreateDTO Updated project data
     * @return Updated project response
     */
    @PreAuthorize("hasRole('ADMIN')")
    public ProjectResponseDTO updateProject(Long projectId, ProjectCreateDTO projectCreateDTO) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        // Update project fields
        project.setName(projectCreateDTO.getName());
        project.setDescription(projectCreateDTO.getDescription());
        project.setDeadline(projectCreateDTO.getDeadline());

        // Update lead if specified
        if (projectCreateDTO.getLeadId() != null) {
            User lead = userService.getUserEntityById(projectCreateDTO.getLeadId());
            if (!lead.getRole().canLeadProjects()) {
                throw new InvalidOperationException("User must be a Project Lead or Admin to lead projects");
            }
            project.setLead(lead);
        }

        Project updatedProject = projectRepository.save(project);
        return convertToResponseDTO(updatedProject);
    }

    /**
     * Delete project (Admin only)
     * @param projectId Project ID
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        projectRepository.delete(project);
    }

    /**
     * Get overdue projects
     * @return List of overdue projects
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProjectResponseDTO> getOverdueProjects() {
        return projectRepository.findOverdueProjects(LocalDate.now())
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if user has access to a project
     * @param project Project
     * @param user User
     * @return true if user has access
     */
    private boolean hasAccessToProject(Project project, User user) {
        switch (user.getRole()) {
            case ADMIN:
                return true;
            case PROJECT_LEAD:
                return project.getLead() != null && project.getLead().getId().equals(user.getId());
            case DEVELOPER:
                return project.getAssignedDevelopers() != null &&
                        project.getAssignedDevelopers().stream()
                                .anyMatch(dev -> dev.getId().equals(user.getId()));
            default:
                return false;
        }
    }

    /**
     * Check if user can assign developers to a project
     * @param project Project
     * @param user User
     * @return true if user can assign developers
     */
    private boolean canAssignDevelopers(Project project, User user) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        if (user.getRole() == Role.PROJECT_LEAD) {
            return project.getLead() != null && project.getLead().getId().equals(user.getId());
        }
        return false;
    }

    /**
     * Convert Project entity to ProjectResponseDTO
     * @param project Project entity
     * @return ProjectResponseDTO
     */
    private ProjectResponseDTO convertToResponseDTO(Project project) {
        // Convert lead to DTO
        UserResponseDTO leadDTO = null;
        if (project.getLead() != null) {
            User lead = project.getLead();
            leadDTO = UserResponseDTO.builder()
                    .id(lead.getId())
                    .username(lead.getUsername())
                    .firstName(lead.getFirstName())
                    .lastName(lead.getLastName())
                    .role(lead.getRole())
                    .build();
        }

        // Convert assigned developers to DTOs
        Set<UserResponseDTO> assignedDeveloperDTOs = null;
        if (project.getAssignedDevelopers() != null) {
            assignedDeveloperDTOs = project.getAssignedDevelopers().stream()
                    .map(dev -> UserResponseDTO.builder()
                            .id(dev.getId())
                            .username(dev.getUsername())
                            .firstName(dev.getFirstName())
                            .lastName(dev.getLastName())
                            .role(dev.getRole())
                            .build())
                    .collect(Collectors.toSet());
        }

        // Get document count
        int documentCount = (int) documentRepository.countByProject(project);

        return ProjectResponseDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .deadline(project.getDeadline())
                .status(String.valueOf(project.getStatus()))
                .createdAt(project.getCreatedAt())
                .completedAt(project.getCompletedAt())
                .lead(leadDTO)
                .assignedDevelopers(assignedDeveloperDTOs)
                .documentCount(documentCount)
                .overdue(project.isOverdue())
                .build();
    }

    /**
     * Get Project entity by ID (internal use)
     * @param projectId Project ID
     * @return Project entity
     */
    public Project getProjectEntityById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }
}