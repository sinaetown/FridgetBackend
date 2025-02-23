package com.fridge.fridgeproject.recipe;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/generate-recipes")
    public ResponseEntity<String> generateRecipes() {
        // Delegate the recipe generation to the service
        String result = recipeService.generateRecipes();
        
        // Return the response
        if (result.startsWith("Error")) {
            return ResponseEntity.status(500).body(result);
        }
        
        return ResponseEntity.ok(result);
    }
}
