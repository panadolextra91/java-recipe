package com.javarecipe.backend.admin.service;

import com.javarecipe.backend.admin.dto.CategoryDTO;
import com.javarecipe.backend.admin.dto.CategoryRequest;
import com.javarecipe.backend.recipe.entity.Category;
import com.javarecipe.backend.recipe.repository.CategoryRepository;
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
class AdminCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private AdminCategoryServiceImpl adminCategoryService;

    private Category testCategory;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Test Category")
                .description("Test Description")
                .recipes(new HashSet<>())
                .build();

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("New Category");
        categoryRequest.setDescription("New Description");
    }

    @Test
    void testGetAllCategoriesWithPagination() {
        // Given
        List<Category> categories = Arrays.asList(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories);
        Pageable pageable = PageRequest.of(0, 10);

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        // When
        Page<CategoryDTO> result = adminCategoryService.getAllCategories(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Category", result.getContent().get(0).getName());
        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void testGetAllCategoriesWithoutPagination() {
        // Given
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findAll()).thenReturn(categories);

        // When
        List<CategoryDTO> result = adminCategoryService.getAllCategories();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Category", result.get(0).getName());
        verify(categoryRepository).findAll();
    }

    @Test
    void testGetCategoryById_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When
        CategoryDTO result = adminCategoryService.getCategoryById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Category", result.getName());
        assertEquals("Test Description", result.getDescription());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void testGetCategoryById_NotFound() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            adminCategoryService.getCategoryById(1L);
        });
        verify(categoryRepository).findById(1L);
    }

    @Test
    void testCreateCategory_Success() {
        // Given
        when(categoryRepository.existsByName("New Category")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        CategoryDTO result = adminCategoryService.createCategory(categoryRequest);

        // Then
        assertNotNull(result);
        assertEquals("Test Category", result.getName());
        verify(categoryRepository).existsByName("New Category");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testCreateCategory_NameAlreadyExists() {
        // Given
        when(categoryRepository.existsByName("New Category")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            adminCategoryService.createCategory(categoryRequest);
        });
        verify(categoryRepository).existsByName("New Category");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testUpdateCategory_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("New Category")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        CategoryDTO result = adminCategoryService.updateCategory(1L, categoryRequest);

        // Then
        assertNotNull(result);
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testDeleteCategory_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When
        adminCategoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void testDeleteCategory_HasRecipes() {
        // Given
        testCategory.getRecipes().add(null); // Simulate having recipes
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            adminCategoryService.deleteCategory(1L);
        });
        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void testCategoryNameExists() {
        // Given
        when(categoryRepository.existsByName("Test Category")).thenReturn(true);

        // When
        boolean result = adminCategoryService.categoryNameExists("Test Category");

        // Then
        assertTrue(result);
        verify(categoryRepository).existsByName("Test Category");
    }

    @Test
    void testConvertToDTO() {
        // When
        CategoryDTO result = adminCategoryService.convertToDTO(testCategory);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Category", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(0, result.getRecipeCount());
    }
}
