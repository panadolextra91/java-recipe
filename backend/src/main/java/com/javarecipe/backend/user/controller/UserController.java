package com.javarecipe.backend.user.controller;

import com.javarecipe.backend.user.dto.*;
import com.javarecipe.backend.user.entity.User;
import com.javarecipe.backend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        
        UserDTO userDTO = userService.convertToDTO(user);
        
        return ResponseEntity.ok(userDTO);
    }
    
    @PutMapping("/me/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserProfileDTO profileDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        
        User updatedUser = userService.updateProfile(user.getId(), profileDTO);
        UserDTO userDTO = userService.convertToDTO(updatedUser);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        response.put("user", userDTO);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/me/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        
        boolean success = userService.changePassword(user.getId(), passwordChangeDTO);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to change password. Please check your current password or ensure new passwords match."));
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO requestDTO) {
        userService.requestPasswordReset(requestDTO.getEmail());
        
        // Always return success to prevent email enumeration
        return ResponseEntity.ok(Map.of("message", "If your email exists in our system, you will receive a password reset link shortly."));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetDTO resetDTO) {
        boolean success = userService.resetPassword(resetDTO);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token, or passwords don't match"));
        }
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> adminOnly() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This endpoint is accessible only to admins");
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/admin/{userId}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> setUserStatus(@PathVariable Long userId, @RequestParam boolean active) {
        User user = userService.setUserActiveStatus(userId, active);
        UserDTO userDTO = userService.convertToDTO(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User status updated successfully");
        response.put("user", userDTO);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> getUserDetails(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        UserDTO userDTO = userService.convertToDTO(user);
        return ResponseEntity.ok(userDTO);
    }
} 