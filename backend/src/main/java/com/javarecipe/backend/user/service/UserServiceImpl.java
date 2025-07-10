package com.javarecipe.backend.user.service;

import com.javarecipe.backend.common.service.EmailService;
import com.javarecipe.backend.user.dto.PasswordChangeDTO;
import com.javarecipe.backend.user.dto.PasswordChangeRequest;
import com.javarecipe.backend.user.dto.PasswordResetDTO;
import com.javarecipe.backend.user.dto.UserDTO;
import com.javarecipe.backend.user.dto.UserProfileDTO;
import com.javarecipe.backend.user.entity.User;
import com.javarecipe.backend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    
    @Override
    public UserDTO convertToDTO(User user) {
        return UserDTO.builder()
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
                .lastLogin(user.getLastLogin())
                .roles(user.getRoles().stream().map(role -> role.getName()).toList())
                .build();
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    // Token expiration time in hours
    @Value("${app.reset-token.expiration:24}")
    private int resetTokenExpirationHours;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, UserProfileDTO profileDTO) {
        User user = getUserById(userId);
        
        user.setFirstName(profileDTO.getFirstName());
        user.setLastName(profileDTO.getLastName());
        user.setDisplayName(profileDTO.getDisplayName());
        user.setBio(profileDTO.getBio());
        
        // Only update avatar URL if provided
        if (profileDTO.getAvatarUrl() != null && !profileDTO.getAvatarUrl().isEmpty()) {
            user.setAvatarUrl(profileDTO.getAvatarUrl());
        }
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, PasswordChangeDTO passwordChangeDTO) {
        User user = getUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(passwordChangeDTO.getCurrentPassword(), user.getPassword())) {
            return false;
        }

        // Verify that new password and confirmation match
        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
            return false;
        }

        // Update password
        user.setPassword(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
        userRepository.save(user);

        return true;
    }

    @Override
    @Transactional
    public boolean changePasswordSimple(Long userId, PasswordChangeRequest passwordChangeRequest) {
        User user = getUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), user.getPassword())) {
            return false;
        }

        // Update password (no constraints, no confirmation required)
        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        userRepository.save(user);

        return true;
    }

    @Override
    @Transactional
    public User updateAvatarUrl(Long userId, String avatarUrl) {
        User user = getUserById(userId);
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        // Even if user is not found, we don't reveal that to prevent email enumeration attacks
        if (user != null) {
            // Generate a random token
            String token = UUID.randomUUID().toString();
            
            // Set token and expiration
            user.setResetToken(token);
            user.setResetTokenExpires(LocalDateTime.now().plusHours(resetTokenExpirationHours));
            userRepository.save(user);
            
            // Send password reset email
            emailService.sendPasswordResetEmail(email, token, user.getUsername());
        }
    }

    @Override
    @Transactional
    public boolean resetPassword(PasswordResetDTO resetDTO) {
        User user = userRepository.findByResetToken(resetDTO.getToken())
                .orElse(null);
        
        if (user == null || user.getResetTokenExpires().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // Verify that new password and confirmation match
        if (!resetDTO.getNewPassword().equals(resetDTO.getConfirmPassword())) {
            return false;
        }
        
        // Update password and clear reset token
        user.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpires(null);
        userRepository.save(user);
        
        return true;
    }

    @Override
    @Transactional
    public void updateLastLogin(Long userId) {
        User user = getUserById(userId);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User setUserActiveStatus(Long userId, boolean isActive) {
        User user = getUserById(userId);
        user.setActive(isActive);
        return userRepository.save(user);
    }
    
    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
} 