package com.fridge.fridgeproject.recipe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Nutrition {
    private String calories;
    private String protein;
    private String carbs;
    private String fat;
    private String fiber;
    private String sugar;
    private String sodium;
}
