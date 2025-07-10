package com.javarecipe.backend.recipe.repository;

import com.javarecipe.backend.recipe.entity.Instruction;
import com.javarecipe.backend.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstructionRepository extends JpaRepository<Instruction, Long> {
    
    List<Instruction> findByRecipeOrderByStepNumber(Recipe recipe);
    
    void deleteByRecipe(Recipe recipe);
} 