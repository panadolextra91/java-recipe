package com.javarecipe.backend.user.controller;

import com.javarecipe.backend.common.service.CloudinaryService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    public UserController(UserService userService, CloudinaryService cloudinaryService) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
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

    @PostMapping("/me/change-password-simple")
    public ResponseEntity<?> changePasswordSimple(@Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        boolean success = userService.changePasswordSimple(user.getId(), passwordChangeRequest);

        if (success) {
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to change password. Please check your current password."));
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

    // Avatar Management Endpoints

    @PostMapping("/me/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        try {
            // Delete old avatar if exists
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                String oldPublicId = cloudinaryService.extractPublicIdFromUrl(user.getAvatarUrl());
                if (oldPublicId != null) {
                    cloudinaryService.deleteImage(oldPublicId);
                }
            }

            // Upload new avatar
            CloudinaryService.CloudinaryUploadResult uploadResult = cloudinaryService.uploadAvatarImage(file);

            // Update user's avatar URL
            User updatedUser = userService.updateAvatarUrl(user.getId(), uploadResult.getUrl());
            UserDTO userDTO = userService.convertToDTO(updatedUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Avatar uploaded successfully");
            response.put("avatarUrl", uploadResult.getUrl());
            response.put("user", userDTO);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid file: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<?> deleteAvatar() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        try {
            if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "No avatar to delete"));
            }

            // Delete from Cloudinary
            String publicId = cloudinaryService.extractPublicIdFromUrl(user.getAvatarUrl());
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
            }

            // Remove avatar URL from user profile
            User updatedUser = userService.updateAvatarUrl(user.getId(), null);
            UserDTO userDTO = userService.convertToDTO(updatedUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Avatar deleted successfully");
            response.put("user", userDTO);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to delete avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}