package com.javarecipe.backend.user.controller;

import com.javarecipe.backend.common.config.JwtTokenUtil;
import com.javarecipe.backend.user.dto.AuthRequest;
import com.javarecipe.backend.user.dto.AuthResponse;
import com.javarecipe.backend.user.dto.RegisterRequest;
import com.javarecipe.backend.user.entity.User;
import com.javarecipe.backend.user.entity.UserRole;
import com.javarecipe.backend.user.repository.UserRepository;
import com.javarecipe.backend.user.repository.UserRoleRepository;
import com.javarecipe.backend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, 
                          UserDetailsService userDetailsService, 
                          JwtTokenUtil jwtTokenUtil, 
                          UserRepository userRepository,
                          UserRoleRepository userRoleRepository,
                          PasswordEncoder passwordEncoder,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid username or password");
            return ResponseEntity.status(401).body(errorResponse);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);

        User user = userRepository.findByUsername(authRequest.getUsername()).orElseThrow();
        
        // Update last login timestamp
        userService.updateLastLogin(user.getId());
        
        return ResponseEntity.ok(new AuthResponse(
                jwt, 
                user.getId(), 
                user.getEmail(), 
                user.getUsername(), 
                user.getFirstName(), 
                user.getLastName(),
                user.getRoles().stream().map(UserRole::getName).toList()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Email is already in use");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Username is already taken");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        UserRole userRole = userRoleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("User role not found"));

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setRoles(Collections.singleton(userRole));
        user.setActive(true);

        userRepository.save(user);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream().map(UserRole::getName).toList()
        ));
    }
} 