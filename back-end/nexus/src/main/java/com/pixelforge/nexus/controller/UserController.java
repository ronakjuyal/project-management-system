package com.pixelforge.nexus.controller;

import com.pixelforge.nexus.dto.UserCreateDTO;
import com.pixelforge.nexus.dto.UserResponseDTO;
import com.pixelforge.nexus.dto.PasswordChangeDTO;
import com.pixelforge.nexus.entity.Role;
import com.pixelforge.nexus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User management controller
 * Handles user CRUD operations with role-based access control
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Create new user (Admin only)
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserResponseDTO createdUser = userService.createUser(userCreateDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    /**
     * Get all users (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by role
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_LEAD')")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@PathVariable Role role) {
        List<UserResponseDTO> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    /**
     * Get available developers for assignment
     */
    @GetMapping("/developers")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_LEAD')")
    public ResponseEntity<List<UserResponseDTO>> getAvailableDevelopers() {
        List<UserResponseDTO> developers = userService.getAvailableDevelopers();
        return ResponseEntity.ok(developers);
    }

    /**
     * Get project leads
     */
    @GetMapping("/project-leads")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getProjectLeads() {
        List<UserResponseDTO> leads = userService.getProjectLeads();
        return ResponseEntity.ok(leads);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> getCurrentUserProfile(Authentication authentication) {
        String username = authentication.getName();
        UserResponseDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user role (Admin only)
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserRole(@PathVariable Long id, @RequestBody Role role) {
        UserResponseDTO updatedUser = userService.updateUserRole(id, role);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Change password
     */
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDTO,
                                               Authentication authentication) {
        // Validate password confirmation
        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().build();
        }

        String username = authentication.getName();
        userService.changePassword(username, passwordChangeDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * Disable user (Admin only)
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Enable user (Admin only)
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return ResponseEntity.ok().build();
    }
}