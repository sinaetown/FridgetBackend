package com.fridge.fridgeproject.recipe;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fridge.fridgeproject.common.CommonResponse;

@RestController
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/recipe/recommend")
    public List<Recipe> generateRecipes() {
        List<Recipe> recipes = recipeService.generateRecipes();
        return recipes;
    }
}
