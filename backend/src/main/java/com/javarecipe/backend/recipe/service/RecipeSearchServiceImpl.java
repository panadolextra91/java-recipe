package com.javarecipe.backend.recipe.service;

import com.javarecipe.backend.recipe.dto.IngredientSearchDTO;
import com.javarecipe.backend.recipe.dto.RecipeMatchDTO;
import com.javarecipe.backend.recipe.dto.RecipeSearchRequest;
import com.javarecipe.backend.recipe.entity.Category;
import com.javarecipe.backend.recipe.entity.Ingredient;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.recipe.entity.RecipeImage;
import com.javarecipe.backend.recipe.repository.IngredientRepository;
import com.javarecipe.backend.recipe.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecipeSearchServiceImpl implements RecipeSearchService {

    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;

    @Autowired
    public RecipeSearchServiceImpl(IngredientRepository ingredientRepository, RecipeRepository recipeRepository) {
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
    }

    @Override
    public List<IngredientSearchDTO> searchIngredients(String query) {
        List<String> ingredientNames = ingredientRepository.searchIngredientNames(query);
        return ingredientNames.stream()
                .map(name -> {
                    Long recipeCount = ingredientRepository.countRecipesByIngredientName(name);
                    return IngredientSearchDTO.builder()
                            .name(name)
                            .recipeCount(recipeCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<IngredientSearchDTO> getAllIngredients() {
        List<String> ingredientNames = ingredientRepository.findDistinctIngredientNames();
        return ingredientNames.stream()
                .map(name -> {
                    Long recipeCount = ingredientRepository.countRecipesByIngredientName(name);
                    return IngredientSearchDTO.builder()
                            .name(name)
                            .recipeCount(recipeCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<RecipeMatchDTO> findRecipesByIngredients(RecipeSearchRequest searchRequest, Pageable pageable) {
        // Normalize user ingredients to lowercase for comparison
        List<String> normalizedUserIngredients = searchRequest.getAvailableIngredients().stream()
                .map(ingredient -> ingredient.toLowerCase().trim())
                .collect(Collectors.toList());

        // Get all published recipes
        List<Recipe> allRecipes = recipeRepository.findAll().stream()
                .filter(Recipe::isPublished)
                .collect(Collectors.toList());

        // Filter by categories if specified
        if (searchRequest.getCategoryIds() != null && !searchRequest.getCategoryIds().isEmpty()) {
            allRecipes = allRecipes.stream()
                    .filter(recipe -> recipe.getCategories().stream()
                            .anyMatch(category -> searchRequest.getCategoryIds().contains(category.getId())))
                    .collect(Collectors.toList());
        }

        // Calculate matches for each recipe
        List<RecipeMatchDTO> matches = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            RecipeMatchDTO match = calculateRecipeMatch(recipe, normalizedUserIngredients);
            
            // Apply filters
            if (searchRequest.getExactMatchOnly() && match.getMatchPercentage() < 100.0) {
                continue;
            }
            
            if (match.getMatchPercentage() < searchRequest.getMinMatchPercentage()) {
                continue;
            }
            
            matches.add(match);
        }

        // Sort results
        sortMatches(matches, searchRequest.getSortBy(), searchRequest.getSortDirection());

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), matches.size());
        List<RecipeMatchDTO> pageContent = matches.subList(start, end);

        return new PageImpl<>(pageContent, pageable, matches.size());
    }

    @Override
    public Double calculateMatchPercentage(Long recipeId, List<String> availableIngredients) {
        Recipe recipe = recipeRepository.findById(recipeId).orElse(null);
        if (recipe == null) {
            return 0.0;
        }

        List<String> normalizedUserIngredients = availableIngredients.stream()
                .map(ingredient -> ingredient.toLowerCase().trim())
                .collect(Collectors.toList());

        return calculateRecipeMatch(recipe, normalizedUserIngredients).getMatchPercentage();
    }

    @Override
    public List<String> getMissingIngredients(Long recipeId, List<String> availableIngredients) {
        Recipe recipe = recipeRepository.findById(recipeId).orElse(null);
        if (recipe == null) {
            return Collections.emptyList();
        }

        List<String> normalizedUserIngredients = availableIngredients.stream()
                .map(ingredient -> ingredient.toLowerCase().trim())
                .collect(Collectors.toList());

        return calculateRecipeMatch(recipe, normalizedUserIngredients).getMissingIngredients();
    }

    @Override
    public List<String> getAvailableIngredients(Long recipeId, List<String> userIngredients) {
        Recipe recipe = recipeRepository.findById(recipeId).orElse(null);
        if (recipe == null) {
            return Collections.emptyList();
        }

        List<String> normalizedUserIngredients = userIngredients.stream()
                .map(ingredient -> ingredient.toLowerCase().trim())
                .collect(Collectors.toList());

        return calculateRecipeMatch(recipe, normalizedUserIngredients).getAvailableIngredients();
    }

    private RecipeMatchDTO calculateRecipeMatch(Recipe recipe, List<String> normalizedUserIngredients) {
        List<String> recipeIngredients = recipe.getIngredients().stream()
                .map(ingredient -> ingredient.getName().toLowerCase().trim())
                .collect(Collectors.toList());

        // Calculate matches
        List<String> availableIngredients = recipeIngredients.stream()
                .filter(normalizedUserIngredients::contains)
                .collect(Collectors.toList());

        List<String> missingIngredients = recipeIngredients.stream()
                .filter(ingredient -> !normalizedUserIngredients.contains(ingredient))
                .collect(Collectors.toList());

        double matchPercentage = recipeIngredients.isEmpty() ? 0.0 : 
                (double) availableIngredients.size() / recipeIngredients.size() * 100.0;

        // Get primary image
        String primaryImageUrl = recipe.getImages().stream()
                .filter(RecipeImage::isPrimary)
                .findFirst()
                .map(RecipeImage::getImageUrl)
                .orElse(recipe.getImages().isEmpty() ? null : recipe.getImages().get(0).getImageUrl());

        // Get categories
        List<String> categories = recipe.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toList());

        return RecipeMatchDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .prepTime(recipe.getPrepTime())
                .cookTime(recipe.getCookTime())
                .servings(recipe.getServings())
                .difficulty(recipe.getDifficulty())
                .averageRating(recipe.getAverageRating())
                .reviewCount(recipe.getReviewCount())
                .viewCount(recipe.getViewCount())
                .createdAt(recipe.getCreatedAt())
                .authorUsername(recipe.getUser().getUsername())
                .authorDisplayName(recipe.getUser().getDisplayName())
                .primaryImageUrl(primaryImageUrl)
                .matchPercentage(matchPercentage)
                .totalIngredients(recipeIngredients.size())
                .matchedIngredients(availableIngredients.size())
                .missingIngredients(missingIngredients)
                .availableIngredients(availableIngredients)
                .categories(categories)
                .build();
    }

    private void sortMatches(List<RecipeMatchDTO> matches, String sortBy, String sortDirection) {
        Comparator<RecipeMatchDTO> comparator;

        switch (sortBy.toLowerCase()) {
            case "rating":
                comparator = Comparator.comparing(RecipeMatchDTO::getAverageRating, 
                        Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "viewcount":
                comparator = Comparator.comparing(RecipeMatchDTO::getViewCount);
                break;
            case "createdat":
                comparator = Comparator.comparing(RecipeMatchDTO::getCreatedAt);
                break;
            case "matchpercentage":
            default:
                comparator = Comparator.comparing(RecipeMatchDTO::getMatchPercentage);
                break;
        }

        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        matches.sort(comparator);
    }
}
