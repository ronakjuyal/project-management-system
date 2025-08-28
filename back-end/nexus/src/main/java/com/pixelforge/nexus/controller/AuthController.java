package com.pixelforge.nexus.controller;

import com.pixelforge.nexus.dto.LoginRequestDTO;
import com.pixelforge.nexus.dto.LoginResponseDTO;
import com.pixelforge.nexus.dto.UserResponseDTO;
import com.pixelforge.nexus.security.JwtUtil;
import com.pixelforge.nexus.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller for login and user authentication
 * Handles JWT token generation and validation
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:3000"})
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Authenticate user and generate JWT token
     * @param loginRequest Login credentials
     * @return JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Get user details
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Generate JWT token
        String token = jwtUtil.generateToken(userDetails);
        System.out.println("works 1");
        // Get user response data
        UserResponseDTO userResponse = userService.getUserByUsername(userDetails.getUsername());
       // log.info(userResponse.getEmail());
        // Create response
        LoginResponseDTO response = new LoginResponseDTO(token, userResponse);
        System.out.println("works 3");

        return ResponseEntity.ok(response);
    }

    /**
     * Validate JWT token and return user details
     * @param token Authorization header with Bearer token
     * @return User details if token is valid
     */
    @GetMapping("/validate")
    public ResponseEntity<UserResponseDTO> validateToken(@RequestHeader("Authorization") String token) {

        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);

            try {
                String username = jwtUtil.extractUsername(jwtToken);
                UserDetails userDetails = userService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    UserResponseDTO userResponse = userService.getUserByUsername(username);
                    return ResponseEntity.ok(userResponse);
                }
            } catch (Exception e) {
                return ResponseEntity.status(401).build();
            }
        }

        return ResponseEntity.status(401).build();
    }

    /**
     * Refresh JWT token
     * @param token Authorization header with Bearer token
     * @return New JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestHeader("Authorization") String token) {

        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);

            try {
                String username = jwtUtil.extractUsername(jwtToken);
                UserDetails userDetails = userService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    // Generate new token
                    String newToken = jwtUtil.generateToken(userDetails);
                    UserResponseDTO userResponse = userService.getUserByUsername(username);

                    LoginResponseDTO response = new LoginResponseDTO(newToken, userResponse);
                    return ResponseEntity.ok(response);
                }
            } catch (Exception e) {
                return ResponseEntity.status(401).build();
            }
        }

        return ResponseEntity.status(401).build();
    }

    /**
     * Get current user profile
     * @param authentication Current authentication
     * @return Current user details
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            UserResponseDTO userResponse = userService.getUserByUsername(username);
            return ResponseEntity.ok(userResponse);
        }

        return ResponseEntity.status(401).build();
    }
}