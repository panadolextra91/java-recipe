package com.javarecipe.backend.user.service;

import com.javarecipe.backend.user.entity.User;
import com.javarecipe.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvatarManagementTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setAvatarUrl(null);
    }

    @Test
    void testUpdateAvatarUrl_Success() {
        // Given
        String newAvatarUrl = "https://res.cloudinary.com/test/image/upload/v123/avatars/avatar_123.jpg";
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateAvatarUrl(1L, newAvatarUrl);

        // Then
        assertNotNull(result);
        assertEquals(newAvatarUrl, testUser.getAvatarUrl());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateAvatarUrl_RemoveAvatar() {
        // Given - User has an existing avatar
        testUser.setAvatarUrl("https://res.cloudinary.com/test/image/upload/v123/avatars/old_avatar.jpg");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When - Set avatar to null (remove avatar)
        User result = userService.updateAvatarUrl(1L, null);

        // Then
        assertNotNull(result);
        assertNull(testUser.getAvatarUrl());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateAvatarUrl_ReplaceExistingAvatar() {
        // Given - User has an existing avatar
        String oldAvatarUrl = "https://res.cloudinary.com/test/image/upload/v123/avatars/old_avatar.jpg";
        String newAvatarUrl = "https://res.cloudinary.com/test/image/upload/v456/avatars/new_avatar.jpg";
        testUser.setAvatarUrl(oldAvatarUrl);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When - Update to new avatar
        User result = userService.updateAvatarUrl(1L, newAvatarUrl);

        // Then
        assertNotNull(result);
        assertEquals(newAvatarUrl, testUser.getAvatarUrl());
        assertNotEquals(oldAvatarUrl, testUser.getAvatarUrl());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateAvatarUrl_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            userService.updateAvatarUrl(1L, "https://example.com/avatar.jpg");
        });

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateAvatarUrl_EmptyString() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When - Set avatar to empty string
        User result = userService.updateAvatarUrl(1L, "");

        // Then
        assertNotNull(result);
        assertEquals("", testUser.getAvatarUrl());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }
}
