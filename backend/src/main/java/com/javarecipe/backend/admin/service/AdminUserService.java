package com.javarecipe.backend.admin.service;

import com.javarecipe.backend.admin.dto.AdminUserDTO;
import com.javarecipe.backend.admin.dto.UserRoleRequest;
import com.javarecipe.backend.admin.dto.UserStatusRequest;
import com.javarecipe.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminUserService {
    
    /**
     * Get all users with pagination and activity metrics
     */
    Page<AdminUserDTO> getAllUsers(Pageable pageable);
    
    /**
     * Get all users without pagination
     */
    List<AdminUserDTO> getAllUsers();
    
    /**
     * Get user by ID with activity metrics
     */
    AdminUserDTO getUserById(Long id);
    
    /**
     * Search users by username, email, or name
     */
    Page<AdminUserDTO> searchUsers(String query, Pageable pageable);
    
    /**
     * Filter users by status (active/inactive)
     */
    Page<AdminUserDTO> getUsersByStatus(boolean active, Pageable pageable);
    
    /**
     * Filter users by role
     */
    Page<AdminUserDTO> getUsersByRole(String role, Pageable pageable);
    
    /**
     * Update user status (activate/deactivate)
     */
    AdminUserDTO updateUserStatus(Long userId, UserStatusRequest statusRequest);
    
    /**
     * Update user roles
     */
    AdminUserDTO updateUserRoles(Long userId, UserRoleRequest roleRequest);
    
    /**
     * Delete user (admin only - careful operation)
     */
    void deleteUser(Long userId);
    
    /**
     * Get user activity statistics
     */
    AdminUserDTO getUserActivityStats(Long userId);
    
    /**
     * Convert User entity to AdminUserDTO with activity metrics
     */
    AdminUserDTO convertToAdminDTO(User user);
    
    /**
     * Get available roles
     */
    List<String> getAvailableRoles();
}
