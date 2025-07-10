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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User testUser;
    private UserRole userRole;
    private UserRole adminRole;

    @BeforeEach
    void setUp() {
        userRole = new UserRole("ROLE_USER");
        userRole.setId(1L);
        
        adminRole = new UserRole("ROLE_ADMIN");
        adminRole.setId(2L);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .displayName("Test User")
                .bio("Test bio")
                .avatarUrl("http://example.com/avatar.jpg")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .roles(new HashSet<>(Arrays.asList(userRole)))
                .build();
    }

    @Test
    void testGetAllUsersWithPagination() {
        // Given
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(recipeRepository.countByUser(testUser)).thenReturn(5L);
        when(commentRepository.countByUser(testUser)).thenReturn(10L);
        when(reviewRepository.countByUser(testUser)).thenReturn(3L);
        when(likeRepository.countByUser(testUser)).thenReturn(15L);
        when(favoriteRepository.countByUser(testUser)).thenReturn(8L);

        // When
        Page<AdminUserDTO> result = adminUserService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        AdminUserDTO userDTO = result.getContent().get(0);
        assertEquals("testuser", userDTO.getUsername());
        assertEquals(5L, userDTO.getRecipeCount());
        assertEquals(10L, userDTO.getCommentCount());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void testGetUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recipeRepository.countByUser(testUser)).thenReturn(5L);
        when(commentRepository.countByUser(testUser)).thenReturn(10L);
        when(reviewRepository.countByUser(testUser)).thenReturn(3L);
        when(likeRepository.countByUser(testUser)).thenReturn(15L);
        when(favoriteRepository.countByUser(testUser)).thenReturn(8L);

        // When
        AdminUserDTO result = adminUserService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertTrue(result.isActive());
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserById_NotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            adminUserService.getUserById(1L);
        });
        verify(userRepository).findById(1L);
    }

    @Test
    void testUpdateUserStatus_Success() {
        // Given
        UserStatusRequest statusRequest = new UserStatusRequest(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(recipeRepository.countByUser(testUser)).thenReturn(5L);
        when(commentRepository.countByUser(testUser)).thenReturn(10L);
        when(reviewRepository.countByUser(testUser)).thenReturn(3L);
        when(likeRepository.countByUser(testUser)).thenReturn(15L);
        when(favoriteRepository.countByUser(testUser)).thenReturn(8L);

        // When
        AdminUserDTO result = adminUserService.updateUserStatus(1L, statusRequest);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserRoles_Success() {
        // Given
        UserRoleRequest roleRequest = new UserRoleRequest(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRoleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(recipeRepository.countByUser(testUser)).thenReturn(5L);
        when(commentRepository.countByUser(testUser)).thenReturn(10L);
        when(reviewRepository.countByUser(testUser)).thenReturn(3L);
        when(likeRepository.countByUser(testUser)).thenReturn(15L);
        when(favoriteRepository.countByUser(testUser)).thenReturn(8L);

        // When
        AdminUserDTO result = adminUserService.updateUserRoles(1L, roleRequest);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRoleRepository).findByName("ROLE_USER");
        verify(userRoleRepository).findByName("ROLE_ADMIN");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testDeleteUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recipeRepository.countByUser(testUser)).thenReturn(0L);

        // When
        adminUserService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(recipeRepository).countByUser(testUser);
        verify(userRepository).delete(testUser);
    }

    @Test
    void testDeleteUser_HasRecipes() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recipeRepository.countByUser(testUser)).thenReturn(5L);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            adminUserService.deleteUser(1L);
        });
        verify(userRepository).findById(1L);
        verify(recipeRepository).countByUser(testUser);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void testSearchUsers() {
        // Given
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.searchUsers("test", pageable)).thenReturn(userPage);
        when(recipeRepository.countByUser(testUser)).thenReturn(5L);
        when(commentRepository.countByUser(testUser)).thenReturn(10L);
        when(reviewRepository.countByUser(testUser)).thenReturn(3L);
        when(likeRepository.countByUser(testUser)).thenReturn(15L);
        when(favoriteRepository.countByUser(testUser)).thenReturn(8L);

        // When
        Page<AdminUserDTO> result = adminUserService.searchUsers("test", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).searchUsers("test", pageable);
    }

    @Test
    void testGetAvailableRoles() {
        // When
        List<String> roles = adminUserService.getAvailableRoles();

        // Then
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void testConvertToAdminDTO() {
        // Given
        when(recipeRepository.countByUser(testUser)).thenReturn(5L);
        when(commentRepository.countByUser(testUser)).thenReturn(10L);
        when(reviewRepository.countByUser(testUser)).thenReturn(3L);
        when(likeRepository.countByUser(testUser)).thenReturn(15L);
        when(favoriteRepository.countByUser(testUser)).thenReturn(8L);

        // When
        AdminUserDTO result = adminUserService.convertToAdminDTO(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("Test User", result.getDisplayName());
        assertTrue(result.isActive());
        assertEquals(5L, result.getRecipeCount());
        assertEquals(10L, result.getCommentCount());
        assertEquals(3L, result.getReviewCount());
        assertEquals(15L, result.getLikeCount());
        assertEquals(8L, result.getFavoriteCount());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains("ROLE_USER"));
    }
}
