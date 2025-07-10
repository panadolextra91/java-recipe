package com.javarecipe.backend.admin.service;

import com.javarecipe.backend.admin.dto.AdminUserDTO;
import com.javarecipe.backend.admin.dto.UserRoleRequest;
import com.javarecipe.backend.admin.dto.UserStatusRequest;
import com.javarecipe.backend.comment.repository.CommentRepository;
import com.javarecipe.backend.interaction.repository.FavoriteRepository;
import com.javarecipe.backend.interaction.repository.LikeRepository;
import com.javarecipe.backend.interaction.repository.ReviewRepository;
import com.javarecipe.backend.recipe.repository.RecipeRepository;
import com.javarecipe.backend.user.entity.User;
import com.javarecipe.backend.user.entity.UserRole;
import com.javarecipe.backend.user.repository.UserRepository;
import com.javarecipe.backend.user.repository.UserRoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RecipeRepository recipeRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;

    @Autowired
    public AdminUserServiceImpl(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RecipeRepository recipeRepository,
            CommentRepository commentRepository,
            ReviewRepository reviewRepository,
            LikeRepository likeRepository,
            FavoriteRepository favoriteRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.recipeRepository = recipeRepository;
        this.commentRepository = commentRepository;
        this.reviewRepository = reviewRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public Page<AdminUserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToAdminDTO);
    }

    @Override
    public List<AdminUserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToAdminDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdminUserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return convertToAdminDTO(user);
    }

    @Override
    public Page<AdminUserDTO> searchUsers(String query, Pageable pageable) {
        return userRepository.searchUsers(query, pageable)
                .map(this::convertToAdminDTO);
    }

    @Override
    public Page<AdminUserDTO> getUsersByStatus(boolean active, Pageable pageable) {
        return userRepository.findByIsActive(active, pageable)
                .map(this::convertToAdminDTO);
    }

    @Override
    public Page<AdminUserDTO> getUsersByRole(String role, Pageable pageable) {
        return userRepository.findByRoleName(role, pageable)
                .map(this::convertToAdminDTO);
    }

    @Override
    @Transactional
    public AdminUserDTO updateUserStatus(Long userId, UserStatusRequest statusRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        user.setActive(statusRequest.getActive());
        User updatedUser = userRepository.save(user);
        return convertToAdminDTO(updatedUser);
    }

    @Override
    @Transactional
    public AdminUserDTO updateUserRoles(Long userId, UserRoleRequest roleRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Get the roles from the database
        Set<UserRole> newRoles = new HashSet<>();
        for (String roleName : roleRequest.getRoles()) {
            UserRole role = userRoleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
            newRoles.add(role);
        }

        user.setRoles(newRoles);
        User updatedUser = userRepository.save(user);
        return convertToAdminDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Check if user has any content that would prevent deletion
        long recipeCount = recipeRepository.countByUser(user);
        if (recipeCount > 0) {
            throw new IllegalStateException("Cannot delete user with " + recipeCount + " recipes. " +
                    "Please transfer or delete user's content first.");
        }

        userRepository.delete(user);
    }

    @Override
    public AdminUserDTO getUserActivityStats(Long userId) {
        return getUserById(userId); // Already includes activity stats
    }

    @Override
    public AdminUserDTO convertToAdminDTO(User user) {
        // Calculate activity metrics
        Long recipeCount = recipeRepository.countByUser(user);
        Long commentCount = commentRepository.countByUser(user);
        Long reviewCount = reviewRepository.countByUser(user);
        Long likeCount = likeRepository.countByUser(user);
        Long favoriteCount = favoriteRepository.countByUser(user);

        return AdminUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .roles(user.getRoles().stream().map(UserRole::getName).collect(Collectors.toList()))
                .recipeCount(recipeCount)
                .commentCount(commentCount)
                .reviewCount(reviewCount)
                .likeCount(likeCount)
                .favoriteCount(favoriteCount)
                .build();
    }

    @Override
    public List<String> getAvailableRoles() {
        return Arrays.asList("ROLE_USER", "ROLE_ADMIN");
    }
}
