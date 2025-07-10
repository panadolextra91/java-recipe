package com.javarecipe.backend.user.service;

import com.javarecipe.backend.user.dto.PasswordChangeDTO;
import com.javarecipe.backend.user.dto.PasswordResetDTO;
import com.javarecipe.backend.user.dto.UserDTO;
import com.javarecipe.backend.user.dto.UserProfileDTO;
import com.javarecipe.backend.user.entity.User;

import java.util.List;

public interface UserService {
    
    /**
     * Convert User entity to UserDTO
     */
    UserDTO convertToDTO(User user);
    
    /**
     * Get user by ID
     */
    User getUserById(Long id);
    
    /**
     * Get user by username
     */
    User getUserByUsername(String username);
    
    /**
     * Get user by email
     */
    User getUserByEmail(String email);
    
    /**
     * Update user profile
     */
    User updateProfile(Long userId, UserProfileDTO profileDTO);
    
    /**
     * Change user password
     */
    boolean changePassword(Long userId, PasswordChangeDTO passwordChangeDTO);
    
    /**
     * Request password reset (generates token and sends email)
     */
    void requestPasswordReset(String email);
    
    /**
     * Reset password using token
     */
    boolean resetPassword(PasswordResetDTO resetDTO);
    
    /**
     * Update user's last login timestamp
     */
    void updateLastLogin(Long userId);
    
    /**
     * Activate/deactivate user account
     */
    User setUserActiveStatus(Long userId, boolean isActive);
    
    /**
     * Get all users (for admin)
     */
    List<UserDTO> getAllUsers();
} 