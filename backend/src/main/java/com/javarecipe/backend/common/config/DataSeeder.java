package com.javarecipe.backend.common.config;

import com.javarecipe.backend.recipe.entity.Category;
import com.javarecipe.backend.recipe.entity.ConsumerWarning;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.recipe.repository.CategoryRepository;
import com.javarecipe.backend.recipe.repository.ConsumerWarningRepository;
import com.javarecipe.backend.recipe.repository.RecipeRepository;
import com.javarecipe.backend.user.entity.User;
import com.javarecipe.backend.user.entity.UserRole;
import com.javarecipe.backend.user.repository.UserRepository;
import com.javarecipe.backend.user.repository.UserRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ConsumerWarningRepository consumerWarningRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoryRepository categoryRepository, 
                      ConsumerWarningRepository consumerWarningRepository,
                      RecipeRepository recipeRepository,
                      UserRepository userRepository,
                      UserRoleRepository userRoleRepository,
                      PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.consumerWarningRepository = consumerWarningRepository;
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed roles if they don't exist
        seedRoles();
        
        // Seed admin user if it doesn't exist
        seedAdminUser();
        
        // Seed categories if they don't exist
        seedCategories();
        
        // Seed consumer warnings if they don't exist
        seedConsumerWarnings();
        
        // Seed a sample recipe if none exist
        seedSampleRecipe();
    }
    
    private void seedRoles() {
        if (userRoleRepository.count() == 0) {
            List<UserRole> roles = Arrays.asList(
                new UserRole("ROLE_ADMIN"),
                new UserRole("ROLE_USER")
            );
            userRoleRepository.saveAll(roles);
            System.out.println("User roles seeded successfully");
        }
    }
    
    private void seedAdminUser() {
        if (userRepository.count() == 0) {
            UserRole adminRole = userRoleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            
            User adminUser = new User();
            adminUser.setEmail("admin@javarecipe.com");
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setRoles(new HashSet<>(Collections.singletonList(adminRole)));
            adminUser.setActive(true);
            
            userRepository.save(adminUser);
            System.out.println("Admin user seeded successfully");
        }
    }
    
    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = Arrays.asList(
                new Category("Breakfast"),
                new Category("Lunch"),
                new Category("Dinner"),
                new Category("Dessert"),
                new Category("Appetizer"),
                new Category("Beverage"),
                new Category("Snack"),
                new Category("Vegan"),
                new Category("Vegetarian"),
                new Category("Gluten-Free"),
                new Category("Dairy-Free"),
                new Category("Low-Carb"),
                new Category("Keto"),
                new Category("Paleo")
            );
            categoryRepository.saveAll(categories);
            System.out.println("Categories seeded successfully");
        }
    }
    
    private void seedConsumerWarnings() {
        if (consumerWarningRepository.count() == 0) {
            List<ConsumerWarning> warnings = Arrays.asList(
                new ConsumerWarning("Contains Nuts"),
                new ConsumerWarning("Contains Dairy"),
                new ConsumerWarning("Contains Eggs"),
                new ConsumerWarning("Contains Gluten"),
                new ConsumerWarning("Contains Soy"),
                new ConsumerWarning("Contains Shellfish"),
                new ConsumerWarning("Contains Fish"),
                new ConsumerWarning("Contains Alcohol")
            );
            consumerWarningRepository.saveAll(warnings);
            System.out.println("Consumer warnings seeded successfully");
        }
    }
    
    private void seedSampleRecipe() {
        if (recipeRepository.count() == 0) {
            // Get a user
            User user = userRepository.findByUsername("admin")
                    .orElseThrow(() -> new RuntimeException("Admin user not found"));
            
            // Get some categories
            Category breakfast = categoryRepository.findByName("Breakfast")
                    .orElseThrow(() -> new RuntimeException("Breakfast category not found"));
            Category vegetarian = categoryRepository.findByName("Vegetarian")
                    .orElseThrow(() -> new RuntimeException("Vegetarian category not found"));
            
            // Get some warnings
            ConsumerWarning containsEggs = consumerWarningRepository.findByName("Contains Eggs")
                    .orElseThrow(() -> new RuntimeException("Contains Eggs warning not found"));
            ConsumerWarning containsDairy = consumerWarningRepository.findByName("Contains Dairy")
                    .orElseThrow(() -> new RuntimeException("Contains Dairy warning not found"));
            
            // Create a sample recipe
            Recipe recipe = new Recipe();
            recipe.setTitle("Classic Scrambled Eggs");
            recipe.setDescription("A simple and delicious breakfast recipe for perfectly scrambled eggs.");
            recipe.setUser(user);
            recipe.setCategories(new HashSet<>(Arrays.asList(breakfast, vegetarian)));
            recipe.setConsumerWarnings(new HashSet<>(Arrays.asList(containsEggs, containsDairy)));
            recipe.setPrepTime(5);
            recipe.setCookTime(5);
            recipe.setServings(2);
            recipe.setDifficulty("Easy");
            recipe.setPublished(true);
            recipe.setViewCount(0L);
            recipe.setCreatedAt(LocalDateTime.now());
            
            recipeRepository.save(recipe);
            System.out.println("Sample recipe seeded successfully");
        }
    }
} 