package com.javarecipe.backend.admin.controller;

import com.javarecipe.backend.admin.dto.AdminUserDTO;
import com.javarecipe.backend.admin.dto.UserRoleRequest;
import com.javarecipe.backend.admin.dto.UserStatusRequest;
import com.javarecipe.backend.admin.service.AdminUserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Autowired
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Get all users with pagination and activity metrics
     */
    @GetMapping
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminUserDTO> users = adminUserService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get all users without pagination (for dropdowns)
     */
    @GetMapping("/all")
    public ResponseEntity<List<AdminUserDTO>> getAllUsersNoPagination() {
        List<AdminUserDTO> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID with activity metrics
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            AdminUserDTO user = adminUserService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Search users by username, email, or name
     */
    @GetMapping("/search")
    public ResponseEntity<Page<AdminUserDTO>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminUserDTO> users = adminUserService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Filter users by status (active/inactive)
     */
    @GetMapping("/status/{active}")
    public ResponseEntity<Page<AdminUserDTO>> getUsersByStatus(
            @PathVariable boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminUserDTO> users = adminUserService.getUsersByStatus(active, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Filter users by role
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<Page<AdminUserDTO>> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminUserDTO> users = adminUserService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Update user status (activate/deactivate)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, 
                                            @Valid @RequestBody UserStatusRequest statusRequest) {
        try {
            AdminUserDTO updatedUser = adminUserService.updateUserStatus(id, statusRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User status updated successfully");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update user status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update user roles
     */
    @PutMapping("/{id}/roles")
    public ResponseEntity<?> updateUserRoles(@PathVariable Long id, 
                                           @Valid @RequestBody UserRoleRequest roleRequest) {
        try {
            AdminUserDTO updatedUser = adminUserService.updateUserRoles(id, roleRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User roles updated successfully");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update user roles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete user (careful operation)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            adminUserService.deleteUser(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get user activity statistics
     */
    @GetMapping("/{id}/activity")
    public ResponseEntity<?> getUserActivityStats(@PathVariable Long id) {
        try {
            AdminUserDTO userStats = adminUserService.getUserActivityStats(id);
            return ResponseEntity.ok(userStats);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get available roles
     */
    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAvailableRoles() {
        List<String> roles = adminUserService.getAvailableRoles();
        return ResponseEntity.ok(roles);
    }
}
