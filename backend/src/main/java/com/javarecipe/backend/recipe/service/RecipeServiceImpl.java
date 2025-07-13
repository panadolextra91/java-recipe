package com.javarecipe.backend.recipe.service;

import com.javarecipe.backend.recipe.dto.RecipeRequest;
import com.javarecipe.backend.recipe.entity.*;
import com.javarecipe.backend.recipe.repository.*;
import com.javarecipe.backend.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final ConsumerWarningRepository consumerWarningRepository;
    private final IngredientRepository ingredientRepository;
    private final InstructionRepository instructionRepository;
    private final RecipeImageRepository recipeImageRepository;
    private final EntityManager entityManager;

    @Autowired
    public RecipeServiceImpl(
            RecipeRepository recipeRepository,
            CategoryRepository categoryRepository,
            ConsumerWarningRepository consumerWarningRepository,
            IngredientRepository ingredientRepository,
            InstructionRepository instructionRepository,
            RecipeImageRepository recipeImageRepository,
            EntityManager entityManager) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.consumerWarningRepository = consumerWarningRepository;
        this.ingredientRepository = ingredientRepository;
        this.instructionRepository = instructionRepository;
        this.recipeImageRepository = recipeImageRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Page<Recipe> getAllPublishedRecipes(Pageable pageable) {
        return recipeRepository.findAllByIsPublishedTrue(pageable);
    }

    @Override
    public Page<Recipe> searchRecipes(String query, Pageable pageable) {
        return recipeRepository.searchByTitleOrDescription(query, pageable);
    }

    @Override
    @Transactional
    public Recipe getRecipeById(Long id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);
        
        if (recipe.isPresent() && recipe.get().isPublished()) {
            // Increment view count
            Recipe foundRecipe = recipe.get();
            foundRecipe.setViewCount(foundRecipe.getViewCount() + 1);
            return recipeRepository.save(foundRecipe);
        } else {
            throw new EntityNotFoundException("Recipe not found with id: " + id);
        }
    }

    @Override
    @Transactional
    public Recipe createRecipe(RecipeRequest recipeRequest, User user) {
        Recipe recipe = new Recipe();
        recipe.setTitle(recipeRequest.getTitle());
        recipe.setDescription(recipeRequest.getDescription());
        recipe.setPrepTime(recipeRequest.getPrepTime());
        recipe.setCookTime(recipeRequest.getCookTime());
        recipe.setServings(recipeRequest.getServings());
        recipe.setDifficulty(recipeRequest.getDifficulty());
        recipe.setPublished(recipeRequest.isPublished());
        recipe.setViewCount(0L);
        recipe.setUser(user);
        
        // Process categories
        if (recipeRequest.getCategoryIds() != null && !recipeRequest.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : recipeRequest.getCategoryIds()) {
                Optional<Category> existingCategory = categoryRepository.findById(categoryId);
                existingCategory.ifPresent(categories::add);
            }
            recipe.setCategories(categories);
        }
        
        // Process consumer warnings
        if (recipeRequest.getConsumerWarningIds() != null && !recipeRequest.getConsumerWarningIds().isEmpty()) {
            Set<ConsumerWarning> warnings = new HashSet<>();
            for (Long warningId : recipeRequest.getConsumerWarningIds()) {
                Optional<ConsumerWarning> existingWarning = consumerWarningRepository.findById(warningId);
                existingWarning.ifPresent(warnings::add);
            }
            recipe.setConsumerWarnings(warnings);
        }
        
        // Save the recipe first to get an ID
        Recipe savedRecipe = recipeRepository.save(recipe);
        
        // Process ingredients
        if (recipeRequest.getIngredients() != null) {
            for (RecipeRequest.IngredientRequest ingredientReq : recipeRequest.getIngredients()) {
                Ingredient ingredient = Ingredient.builder()
                    .name(ingredientReq.getName())
                    .quantity(ingredientReq.getQuantity())
                    .unit(ingredientReq.getUnit())
                    .displayOrder(ingredientReq.getDisplayOrder())
                    .recipe(savedRecipe)
                    .build();
                ingredientRepository.save(ingredient);
            }
        }
        
        // Process instructions
        if (recipeRequest.getInstructions() != null) {
            for (RecipeRequest.InstructionRequest instructionReq : recipeRequest.getInstructions()) {
                Instruction instruction = Instruction.builder()
                    .description(instructionReq.getDescription())
                    .stepNumber(instructionReq.getStepNumber())
                    .recipe(savedRecipe)
                    .build();
                instructionRepository.save(instruction);
            }
        }
        
        // Process images
        if (recipeRequest.getImages() != null) {
            for (RecipeRequest.RecipeImageRequest imageReq : recipeRequest.getImages()) {
                RecipeImage image = RecipeImage.builder()
                    .imageUrl(imageReq.getImageUrl())
                    .isPrimary(imageReq.isPrimary())
                    .displayOrder(imageReq.getDisplayOrder())
                    .recipe(savedRecipe)
                    .build();
                recipeImageRepository.save(image);
            }
        }

        // Use EntityManager to refresh and fetch all relationships
        entityManager.refresh(savedRecipe);
        entityManager.flush();

        // Manually load all relationships to ensure they're available for JSON serialization
        savedRecipe.getIngredients().size();
        savedRecipe.getInstructions().size();
        savedRecipe.getCategories().size();
        savedRecipe.getConsumerWarnings().size();
        savedRecipe.getImages().size();

        return savedRecipe;
    }

    @Override
    @Transactional
    public Recipe updateRecipe(Long id, RecipeRequest recipeRequest, User user) {
        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + id));
        
        if (!isUserAuthorized(existingRecipe, user)) {
            throw new AccessDeniedException("User not authorized to update this recipe");
        }
        
        // Update basic recipe information
        existingRecipe.setTitle(recipeRequest.getTitle());
        existingRecipe.setDescription(recipeRequest.getDescription());
        existingRecipe.setPrepTime(recipeRequest.getPrepTime());
        existingRecipe.setCookTime(recipeRequest.getCookTime());
        existingRecipe.setServings(recipeRequest.getServings());
        existingRecipe.setDifficulty(recipeRequest.getDifficulty());
        existingRecipe.setPublished(recipeRequest.isPublished());
        
        // Update categories
        if (recipeRequest.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : recipeRequest.getCategoryIds()) {
                Optional<Category> existingCategory = categoryRepository.findById(categoryId);
                existingCategory.ifPresent(categories::add);
            }
            existingRecipe.setCategories(categories);
        }
        
        // Update consumer warnings
        if (recipeRequest.getConsumerWarningIds() != null) {
            Set<ConsumerWarning> warnings = new HashSet<>();
            for (Long warningId : recipeRequest.getConsumerWarningIds()) {
                Optional<ConsumerWarning> existingWarning = consumerWarningRepository.findById(warningId);
                existingWarning.ifPresent(warnings::add);
            }
            existingRecipe.setConsumerWarnings(warnings);
        }
        
        // Save the updated recipe
        Recipe savedRecipe = recipeRepository.save(existingRecipe);
        
        // Update ingredients - remove existing and add new ones
        ingredientRepository.deleteByRecipe(existingRecipe);
        if (recipeRequest.getIngredients() != null) {
            for (RecipeRequest.IngredientRequest ingredientReq : recipeRequest.getIngredients()) {
                Ingredient ingredient = Ingredient.builder()
                    .name(ingredientReq.getName())
                    .quantity(ingredientReq.getQuantity())
                    .unit(ingredientReq.getUnit())
                    .displayOrder(ingredientReq.getDisplayOrder())
                    .recipe(savedRecipe)
                    .build();
                ingredientRepository.save(ingredient);
            }
        }
        
        // Update instructions - remove existing and add new ones
        instructionRepository.deleteByRecipe(existingRecipe);
        if (recipeRequest.getInstructions() != null) {
            for (RecipeRequest.InstructionRequest instructionReq : recipeRequest.getInstructions()) {
                Instruction instruction = Instruction.builder()
                    .description(instructionReq.getDescription())
                    .stepNumber(instructionReq.getStepNumber())
                    .recipe(savedRecipe)
                    .build();
                instructionRepository.save(instruction);
            }
        }
        
        // Update images - only if new images are provided
        if (recipeRequest.getImages() != null && !recipeRequest.getImages().isEmpty()) {
            recipeImageRepository.deleteByRecipe(existingRecipe);
            for (RecipeRequest.RecipeImageRequest imageReq : recipeRequest.getImages()) {
                RecipeImage image = RecipeImage.builder()
                    .imageUrl(imageReq.getImageUrl())
                    .isPrimary(imageReq.isPrimary())
                    .displayOrder(imageReq.getDisplayOrder())
                    .recipe(savedRecipe)
                    .build();
                recipeImageRepository.save(image);
            }
        }
        
        return savedRecipe;
    }

    @Override
    @Transactional
    public void deleteRecipe(Long id, User user) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + id));
        
        if (!isUserAuthorized(recipe, user)) {
            throw new AccessDeniedException("User not authorized to delete this recipe");
        }
        
        recipeRepository.delete(recipe);
    }

    @Override
    public Page<Recipe> getRecipesByUser(User user, Pageable pageable) {
        return recipeRepository.findAllByUser(user, pageable);
    }

    @Override
    public Page<Recipe> getRecipesByCategory(String categoryName, Pageable pageable) {
        return recipeRepository.findByCategoryName(categoryName, pageable);
    }

    @Override
    public boolean isUserAuthorized(Recipe recipe, User user) {
        return recipe.getUser().getId().equals(user.getId());
    }
    
    // Admin-specific methods
    
    @Override
    public Page<Recipe> getAllRecipes(Pageable pageable) {
        return recipeRepository.findAll(pageable);
    }
    
    @Override
    public Page<Recipe> getAllRecipesByPublishedStatus(boolean published, Pageable pageable) {
        if (published) {
            return recipeRepository.findAllByIsPublishedTrue(pageable);
        } else {
            return recipeRepository.findAllByIsPublishedFalse(pageable);
        }
    }
    
    @Override
    @Transactional
    public Recipe setRecipePublishedStatus(Long id, boolean publish) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + id));

        recipe.setPublished(publish);
        return recipeRepository.save(recipe);
    }

    @Override
    @Transactional
    public Recipe setUserRecipePublishedStatus(Long id, boolean publish, User user) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + id));

        // Check if user is authorized to modify this recipe
        if (!isUserAuthorized(recipe, user)) {
            throw new AccessDeniedException("You are not authorized to modify this recipe");
        }

        recipe.setPublished(publish);
        return recipeRepository.save(recipe);
    }
    
    @Override
    @Transactional
    public void deleteRecipeByAdmin(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + id));
        
        recipeRepository.delete(recipe);
    }
} 