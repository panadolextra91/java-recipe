package com.javarecipe.backend.recipe.service;

import com.javarecipe.backend.recipe.dto.IngredientSearchDTO;
import com.javarecipe.backend.recipe.dto.RecipeMatchDTO;
import com.javarecipe.backend.recipe.dto.RecipeSearchRequest;
import com.javarecipe.backend.recipe.entity.*;
import com.javarecipe.backend.recipe.repository.IngredientRepository;
import com.javarecipe.backend.recipe.repository.RecipeRepository;
import com.javarecipe.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeSearchServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeSearchServiceImpl recipeSearchService;

    private Recipe testRecipe;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .displayName("Test User")
                .build();

        testRecipe = Recipe.builder()
                .id(1L)
                .title("Chicken Rice")
                .description("Delicious chicken rice")
                .isPublished(true)
                .user(testUser)
                .viewCount(100L)
                .averageRating(4.5)
                .reviewCount(10)
                .createdAt(LocalDateTime.now())
                .ingredients(new ArrayList<>())
                .images(new ArrayList<>())
                .categories(new HashSet<>())
                .build();

        // Add ingredients to recipe
        Ingredient ingredient1 = Ingredient.builder()
                .id(1L)
                .name("Chicken")
                .quantity("500g")
                .recipe(testRecipe)
                .build();

        Ingredient ingredient2 = Ingredient.builder()
                .id(2L)
                .name("Rice")
                .quantity("2 cups")
                .recipe(testRecipe)
                .build();

        Ingredient ingredient3 = Ingredient.builder()
                .id(3L)
                .name("Onion")
                .quantity("1 medium")
                .recipe(testRecipe)
                .build();

        testRecipe.getIngredients().addAll(Arrays.asList(ingredient1, ingredient2, ingredient3));

        // Add a primary image
        RecipeImage primaryImage = RecipeImage.builder()
                .id(1L)
                .imageUrl("http://example.com/image.jpg")
                .isPrimary(true)
                .recipe(testRecipe)
                .build();

        testRecipe.getImages().add(primaryImage);

        // Add a category
        Category category = Category.builder()
                .id(1L)
                .name("Main Course")
                .build();

        testRecipe.getCategories().add(category);
    }

    @Test
    void testSearchIngredients() {
        // Given
        List<String> ingredientNames = Arrays.asList("chicken", "rice", "onion");
        when(ingredientRepository.searchIngredientNames("chi")).thenReturn(Arrays.asList("chicken"));
        when(ingredientRepository.countRecipesByIngredientName("chicken")).thenReturn(5L);

        // When
        List<IngredientSearchDTO> result = recipeSearchService.searchIngredients("chi");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("chicken", result.get(0).getName());
        assertEquals(5L, result.get(0).getRecipeCount());
        verify(ingredientRepository).searchIngredientNames("chi");
        verify(ingredientRepository).countRecipesByIngredientName("chicken");
    }

    @Test
    void testGetAllIngredients() {
        // Given
        List<String> ingredientNames = Arrays.asList("chicken", "rice", "onion");
        when(ingredientRepository.findDistinctIngredientNames()).thenReturn(ingredientNames);
        when(ingredientRepository.countRecipesByIngredientName("chicken")).thenReturn(5L);
        when(ingredientRepository.countRecipesByIngredientName("rice")).thenReturn(8L);
        when(ingredientRepository.countRecipesByIngredientName("onion")).thenReturn(12L);

        // When
        List<IngredientSearchDTO> result = recipeSearchService.getAllIngredients();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(ingredientRepository).findDistinctIngredientNames();
    }

    @Test
    void testFindRecipesByIngredients_PerfectMatch() {
        // Given
        RecipeSearchRequest searchRequest = new RecipeSearchRequest();
        searchRequest.setAvailableIngredients(Arrays.asList("Chicken", "Rice", "Onion"));
        searchRequest.setMinMatchPercentage(0.0);
        searchRequest.setExactMatchOnly(false);

        when(recipeRepository.findAll()).thenReturn(Arrays.asList(testRecipe));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RecipeMatchDTO> result = recipeSearchService.findRecipesByIngredients(searchRequest, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        RecipeMatchDTO match = result.getContent().get(0);
        assertEquals(1L, match.getId());
        assertEquals("Chicken Rice", match.getTitle());
        assertEquals(100.0, match.getMatchPercentage());
        assertEquals(3, match.getTotalIngredients());
        assertEquals(3, match.getMatchedIngredients());
        assertEquals(0, match.getMissingIngredients().size());
        assertEquals(3, match.getAvailableIngredients().size());
    }

    @Test
    void testFindRecipesByIngredients_PartialMatch() {
        // Given
        RecipeSearchRequest searchRequest = new RecipeSearchRequest();
        searchRequest.setAvailableIngredients(Arrays.asList("Chicken", "Rice")); // Missing onion
        searchRequest.setMinMatchPercentage(0.0);
        searchRequest.setExactMatchOnly(false);

        when(recipeRepository.findAll()).thenReturn(Arrays.asList(testRecipe));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RecipeMatchDTO> result = recipeSearchService.findRecipesByIngredients(searchRequest, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        RecipeMatchDTO match = result.getContent().get(0);
        assertEquals(66.67, match.getMatchPercentage(), 0.01); // 2/3 * 100
        assertEquals(3, match.getTotalIngredients());
        assertEquals(2, match.getMatchedIngredients());
        assertEquals(1, match.getMissingIngredients().size());
        assertTrue(match.getMissingIngredients().contains("onion"));
        assertEquals(2, match.getAvailableIngredients().size());
    }

    @Test
    void testFindRecipesByIngredients_ExactMatchOnly() {
        // Given
        RecipeSearchRequest searchRequest = new RecipeSearchRequest();
        searchRequest.setAvailableIngredients(Arrays.asList("Chicken", "Rice")); // Missing onion
        searchRequest.setMinMatchPercentage(0.0);
        searchRequest.setExactMatchOnly(true); // Only exact matches

        when(recipeRepository.findAll()).thenReturn(Arrays.asList(testRecipe));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RecipeMatchDTO> result = recipeSearchService.findRecipesByIngredients(searchRequest, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements()); // Should be filtered out
    }

    @Test
    void testFindRecipesByIngredients_MinMatchPercentage() {
        // Given
        RecipeSearchRequest searchRequest = new RecipeSearchRequest();
        searchRequest.setAvailableIngredients(Arrays.asList("Chicken")); // Only 1/3 ingredients
        searchRequest.setMinMatchPercentage(50.0); // Require at least 50% match
        searchRequest.setExactMatchOnly(false);

        when(recipeRepository.findAll()).thenReturn(Arrays.asList(testRecipe));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RecipeMatchDTO> result = recipeSearchService.findRecipesByIngredients(searchRequest, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements()); // 33.33% < 50%, should be filtered out
    }

    @Test
    void testCalculateMatchPercentage() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        // When
        Double matchPercentage = recipeSearchService.calculateMatchPercentage(1L, Arrays.asList("Chicken", "Rice"));

        // Then
        assertEquals(66.67, matchPercentage, 0.01); // 2/3 * 100
        verify(recipeRepository).findById(1L);
    }

    @Test
    void testGetMissingIngredients() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        // When
        List<String> missingIngredients = recipeSearchService.getMissingIngredients(1L, Arrays.asList("Chicken", "Rice"));

        // Then
        assertNotNull(missingIngredients);
        assertEquals(1, missingIngredients.size());
        assertTrue(missingIngredients.contains("onion"));
        verify(recipeRepository).findById(1L);
    }

    @Test
    void testGetAvailableIngredients() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        // When
        List<String> availableIngredients = recipeSearchService.getAvailableIngredients(1L, Arrays.asList("Chicken", "Rice", "Garlic"));

        // Then
        assertNotNull(availableIngredients);
        assertEquals(2, availableIngredients.size());
        assertTrue(availableIngredients.contains("chicken"));
        assertTrue(availableIngredients.contains("rice"));
        assertFalse(availableIngredients.contains("garlic")); // Not in recipe
        verify(recipeRepository).findById(1L);
    }

    @Test
    void testCalculateMatchPercentage_RecipeNotFound() {
        // Given
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Double matchPercentage = recipeSearchService.calculateMatchPercentage(999L, Arrays.asList("Chicken"));

        // Then
        assertEquals(0.0, matchPercentage);
        verify(recipeRepository).findById(999L);
    }
}
