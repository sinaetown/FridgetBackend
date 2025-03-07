package com.fridge.fridgeproject.recipe;

import java.io.*;
import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {
    private String name;
    private String description;
    private Nutrition nutrition;
    private List<Ingredient> ingredients;
    private List<String> steps;
    private String reference;
    private List<String> missingIngredients;

}
