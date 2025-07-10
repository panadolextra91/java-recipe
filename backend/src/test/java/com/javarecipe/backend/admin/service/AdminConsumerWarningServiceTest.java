package com.javarecipe.backend.admin.service;

import com.javarecipe.backend.admin.dto.ConsumerWarningDTO;
import com.javarecipe.backend.admin.dto.ConsumerWarningRequest;
import com.javarecipe.backend.recipe.entity.ConsumerWarning;
import com.javarecipe.backend.recipe.repository.ConsumerWarningRepository;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminConsumerWarningServiceTest {

    @Mock
    private ConsumerWarningRepository consumerWarningRepository;

    @InjectMocks
    private AdminConsumerWarningServiceImpl adminConsumerWarningService;

    private ConsumerWarning testWarning;
    private ConsumerWarningRequest warningRequest;

    @BeforeEach
    void setUp() {
        testWarning = ConsumerWarning.builder()
                .id(1L)
                .name("Contains Nuts")
                .description("This product contains nuts")
                .recipes(new HashSet<>())
                .build();

        warningRequest = new ConsumerWarningRequest();
        warningRequest.setName("Contains Dairy");
        warningRequest.setDescription("This product contains dairy");
    }

    @Test
    void testGetAllConsumerWarningsWithPagination() {
        // Given
        List<ConsumerWarning> warnings = Arrays.asList(testWarning);
        Page<ConsumerWarning> warningPage = new PageImpl<>(warnings);
        Pageable pageable = PageRequest.of(0, 10);

        when(consumerWarningRepository.findAll(pageable)).thenReturn(warningPage);

        // When
        Page<ConsumerWarningDTO> result = adminConsumerWarningService.getAllConsumerWarnings(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Contains Nuts", result.getContent().get(0).getName());
        verify(consumerWarningRepository).findAll(pageable);
    }

    @Test
    void testGetAllConsumerWarningsWithoutPagination() {
        // Given
        List<ConsumerWarning> warnings = Arrays.asList(testWarning);
        when(consumerWarningRepository.findAll()).thenReturn(warnings);

        // When
        List<ConsumerWarningDTO> result = adminConsumerWarningService.getAllConsumerWarnings();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Contains Nuts", result.get(0).getName());
        verify(consumerWarningRepository).findAll();
    }

    @Test
    void testGetConsumerWarningById_Success() {
        // Given
        when(consumerWarningRepository.findById(1L)).thenReturn(Optional.of(testWarning));

        // When
        ConsumerWarningDTO result = adminConsumerWarningService.getConsumerWarningById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Contains Nuts", result.getName());
        assertEquals("This product contains nuts", result.getDescription());
        verify(consumerWarningRepository).findById(1L);
    }

    @Test
    void testGetConsumerWarningById_NotFound() {
        // Given
        when(consumerWarningRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            adminConsumerWarningService.getConsumerWarningById(1L);
        });
        verify(consumerWarningRepository).findById(1L);
    }

    @Test
    void testCreateConsumerWarning_Success() {
        // Given
        when(consumerWarningRepository.existsByName("Contains Dairy")).thenReturn(false);
        when(consumerWarningRepository.save(any(ConsumerWarning.class))).thenReturn(testWarning);

        // When
        ConsumerWarningDTO result = adminConsumerWarningService.createConsumerWarning(warningRequest);

        // Then
        assertNotNull(result);
        assertEquals("Contains Nuts", result.getName());
        verify(consumerWarningRepository).existsByName("Contains Dairy");
        verify(consumerWarningRepository).save(any(ConsumerWarning.class));
    }

    @Test
    void testCreateConsumerWarning_NameAlreadyExists() {
        // Given
        when(consumerWarningRepository.existsByName("Contains Dairy")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            adminConsumerWarningService.createConsumerWarning(warningRequest);
        });
        verify(consumerWarningRepository).existsByName("Contains Dairy");
        verify(consumerWarningRepository, never()).save(any(ConsumerWarning.class));
    }

    @Test
    void testUpdateConsumerWarning_Success() {
        // Given
        when(consumerWarningRepository.findById(1L)).thenReturn(Optional.of(testWarning));
        when(consumerWarningRepository.existsByName("Contains Dairy")).thenReturn(false);
        when(consumerWarningRepository.save(any(ConsumerWarning.class))).thenReturn(testWarning);

        // When
        ConsumerWarningDTO result = adminConsumerWarningService.updateConsumerWarning(1L, warningRequest);

        // Then
        assertNotNull(result);
        verify(consumerWarningRepository).findById(1L);
        verify(consumerWarningRepository).save(any(ConsumerWarning.class));
    }

    @Test
    void testDeleteConsumerWarning_Success() {
        // Given
        when(consumerWarningRepository.findById(1L)).thenReturn(Optional.of(testWarning));

        // When
        adminConsumerWarningService.deleteConsumerWarning(1L);

        // Then
        verify(consumerWarningRepository).findById(1L);
        verify(consumerWarningRepository).delete(testWarning);
    }

    @Test
    void testDeleteConsumerWarning_HasRecipes() {
        // Given
        testWarning.getRecipes().add(null); // Simulate having recipes
        when(consumerWarningRepository.findById(1L)).thenReturn(Optional.of(testWarning));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            adminConsumerWarningService.deleteConsumerWarning(1L);
        });
        verify(consumerWarningRepository).findById(1L);
        verify(consumerWarningRepository, never()).delete(any(ConsumerWarning.class));
    }

    @Test
    void testWarningNameExists() {
        // Given
        when(consumerWarningRepository.existsByName("Contains Nuts")).thenReturn(true);

        // When
        boolean result = adminConsumerWarningService.warningNameExists("Contains Nuts");

        // Then
        assertTrue(result);
        verify(consumerWarningRepository).existsByName("Contains Nuts");
    }

    @Test
    void testConvertToDTO() {
        // When
        ConsumerWarningDTO result = adminConsumerWarningService.convertToDTO(testWarning);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Contains Nuts", result.getName());
        assertEquals("This product contains nuts", result.getDescription());
        assertEquals(0, result.getRecipeCount());
    }
}
