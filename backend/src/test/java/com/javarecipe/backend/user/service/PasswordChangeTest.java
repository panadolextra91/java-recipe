package com.javarecipe.backend.user.service;

import com.javarecipe.backend.user.dto.PasswordChangeRequest;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordChangeTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private PasswordChangeRequest passwordChangeRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedOldPassword");

        passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setCurrentPassword("oldPassword");
        passwordChangeRequest.setNewPassword("newPassword123");
    }

    @Test
    void testChangePasswordSimple_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        boolean result = userService.changePasswordSimple(1L, passwordChangeRequest);

        // Then
        assertTrue(result);
        verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(testUser);
        assertEquals("encodedNewPassword", testUser.getPassword());
    }

    @Test
    void testChangePasswordSimple_WrongCurrentPassword() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(false);

        // When
        boolean result = userService.changePasswordSimple(1L, passwordChangeRequest);

        // Then
        assertFalse(result);
        verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        assertEquals("encodedOldPassword", testUser.getPassword()); // Password should remain unchanged
    }

    @Test
    void testChangePasswordSimple_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            userService.changePasswordSimple(1L, passwordChangeRequest);
        });

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testChangePasswordSimple_NoConstraints() {
        // Test that any password is accepted (no constraints)
        passwordChangeRequest.setNewPassword("a"); // Very short password
        
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("a")).thenReturn("encodedA");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        boolean result = userService.changePasswordSimple(1L, passwordChangeRequest);

        // Then
        assertTrue(result);
        verify(passwordEncoder).encode("a");
        assertEquals("encodedA", testUser.getPassword());
    }

    @Test
    void testChangePasswordSimple_EmptyPassword() {
        // Test that even empty password is accepted (no constraints)
        passwordChangeRequest.setNewPassword("");
        
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("")).thenReturn("encodedEmpty");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        boolean result = userService.changePasswordSimple(1L, passwordChangeRequest);

        // Then
        assertTrue(result);
        verify(passwordEncoder).encode("");
        assertEquals("encodedEmpty", testUser.getPassword());
    }

    @Test
    void testChangePasswordSimple_SpecialCharacters() {
        // Test that password with special characters is accepted
        passwordChangeRequest.setNewPassword("!@#$%^&*()");
        
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("!@#$%^&*()")).thenReturn("encodedSpecial");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        boolean result = userService.changePasswordSimple(1L, passwordChangeRequest);

        // Then
        assertTrue(result);
        verify(passwordEncoder).encode("!@#$%^&*()");
        assertEquals("encodedSpecial", testUser.getPassword());
    }
}
