package com.javarecipe.backend.admin.service;

import com.javarecipe.backend.admin.dto.CategoryDTO;
import com.javarecipe.backend.admin.dto.CategoryRequest;
import com.javarecipe.backend.recipe.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminCategoryService {
    
    /**
     * Get all categories with pagination
     */
    Page<CategoryDTO> getAllCategories(Pageable pageable);
    
    /**
     * Get all categories without pagination
     */
    List<CategoryDTO> getAllCategories();
    
    /**
     * Get category by ID
     */
    CategoryDTO getCategoryById(Long id);
    
    /**
     * Create new category
     */
    CategoryDTO createCategory(CategoryRequest categoryRequest);
    
    /**
     * Update existing category
     */
    CategoryDTO updateCategory(Long id, CategoryRequest categoryRequest);
    
    /**
     * Delete category
     */
    void deleteCategory(Long id);
    
    /**
     * Check if category name exists
     */
    boolean categoryNameExists(String name);
    
    /**
     * Convert Category entity to CategoryDTO
     */
    CategoryDTO convertToDTO(Category category);
}
