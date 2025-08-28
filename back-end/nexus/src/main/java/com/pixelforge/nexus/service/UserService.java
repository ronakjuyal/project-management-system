package com.pixelforge.nexus.service;

import com.pixelforge.nexus.entity.Role;
import com.pixelforge.nexus.entity.User;
import com.pixelforge.nexus.repository.UserRepository;
import com.pixelforge.nexus.dto.UserCreateDTO;
import com.pixelforge.nexus.dto.UserResponseDTO;
import com.pixelforge.nexus.dto.PasswordChangeDTO;
import com.pixelforge.nexus.exception.ResourceNotFoundException;
import com.pixelforge.nexus.exception.DuplicateResourceException;
import com.pixelforge.nexus.exception.InvalidOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for User management operations
 * Implements UserDetailsService for Spring Security integration
 */
@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Load user by username for Spring Security
     * @param username Username
     * @return UserDetails
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Create a new user (Admin only)
     * @param userCreateDTO User creation data
     * @return Created user response
     */
    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {
        // Validate uniqueness
        if (userRepository.existsByUsername(userCreateDTO.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + userCreateDTO.getUsername());
        }

        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + userCreateDTO.getEmail());
        }

        // Create new user
        User user = new User();
        user.setUsername(userCreateDTO.getUsername());
        user.setEmail(userCreateDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        user.setFirstName(userCreateDTO.getFirstName());
        user.setLastName(userCreateDTO.getLastName());
        user.setRole(userCreateDTO.getRole());

        User savedUser = userRepository.save(user);
        return convertToResponseDTO(savedUser);
    }

    /**
     * Get user by ID
     * @param id User ID
     * @return User response
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToResponseDTO(user);
    }

    /**
     * Get user by username
     * @param username Username
     * @return User response
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return convertToResponseDTO(user);
    }

    /**
     * Get all users
     * @return List of user responses
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get users by role
     * @param role User role
     * @return List of users with specified role
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(Role role) {
        return userRepository.findActiveUsersByRole(role)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update user role (Admin only)
     * @param userId User ID
     * @param newRole New role
     * @return Updated user response
     */
    public UserResponseDTO updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        return convertToResponseDTO(updatedUser);
    }

    /**
     * Change user password
     * @param username Username
     * @param passwordChangeDTO Password change data
     */
    public void changePassword(String username, PasswordChangeDTO passwordChangeDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Verify current password
        if (!passwordEncoder.matches(passwordChangeDTO.getCurrentPassword(), user.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Disable user account (Admin only)
     * @param userId User ID
     */
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setEnabled(false);
        userRepository.save(user);
    }

    /**
     * Enable user account (Admin only)
     * @param userId User ID
     */
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setEnabled(true);
        userRepository.save(user);
    }

    /**
     * Get developers available for project assignment
     * @return List of available developers
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAvailableDevelopers() {
        return userRepository.findActiveUsersByRole(Role.DEVELOPER)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get project leads
     * @return List of project leads
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getProjectLeads() {
        return userRepository.findActiveUsersByRole(Role.PROJECT_LEAD)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert User entity to UserResponseDTO
     * @param user User entity
     * @return UserResponseDTO
     */
    private UserResponseDTO convertToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Get User entity by username (internal use)
     * @param username Username
     * @return User entity
     */
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    /**
     * Get User entity by ID (internal use)
     * @param id User ID
     * @return User entity
     */
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}