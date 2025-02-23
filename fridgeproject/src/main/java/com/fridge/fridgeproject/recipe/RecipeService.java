package com.fridge.fridgeproject.recipe;

import com.fridge.fridgeproject.ingredient.UserIngredient;
import com.fridge.fridgeproject.ingredient.IngredientService;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.*;

@Service
public class RecipeService {

    private final IngredientService ingredientService;

    public RecipeService(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    public String generateRecipes() {
        try {
            // Retrieve user ingredients from the service
            List<UserIngredient> userIngredientsList = ingredientService.findMyIngredients();

            // Convert ingredients list to a simple list of strings
            List<String> userIngredients = new ArrayList<>();
            for (UserIngredient ingredient : userIngredientsList) {
                userIngredients.add(ingredient.getName()); // Assuming 'getName()' method exists
            }

            // Convert list to JSON
            String jsonInput = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(userIngredients);

            // Start the Python process
            ProcessBuilder pb = new ProcessBuilder("python3", "/fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/Prompt.py");
            pb.redirectErrorStream(true);  // Merge error output with standard output
            Process process = pb.start();

            // Send JSON input to Python script
            OutputStream os = process.getOutputStream();
            os.write(jsonInput.getBytes());
            os.flush();
            os.close();

            // Read Python output (recipe data)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "Error: Python script failed.";
            }

            // Return the response from Python script
            return output.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Unable to generate recipes.";
        }
    }
}
