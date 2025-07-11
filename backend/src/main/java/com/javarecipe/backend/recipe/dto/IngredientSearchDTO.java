package com.javarecipe.backend.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientSearchDTO {
    private String name;
    private Long recipeCount; // How many recipes use this ingredient
}
