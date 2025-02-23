package com.fridge.fridgeproject.recipe;

import com.fridge.fridgeproject.ingredient.UserIngredient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fridge.fridgeproject.ingredient.IngredientService;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class RecipeService {

    private final IngredientService ingredientService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RecipeService(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    public List<Recipe> generateRecipes() {
        try {
            List<UserIngredient> userIngredientsList = ingredientService.findMyIngredients();

            List<String> userIngredients = new ArrayList<>();
            for (UserIngredient ingredient : userIngredientsList) {
                userIngredients.add(ingredient.getName()); 
            }
            String jsonInput = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(userIngredients);

            ProcessBuilder pb = new ProcessBuilder("python3", "/Users/sinaehong/Downloads/hopperhacks_backend/fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/Prompt.py");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            OutputStream os = process.getOutputStream();
            os.write(jsonInput.getBytes());
            os.flush();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return Collections.emptyList();
            }

            String jsonFilePath = "/Users/sinaehong/Downloads/hopperhacks_backend/fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/recipe.json";
            File jsonFile = new File(jsonFilePath);
            if (jsonFile.exists()) {
                System.out.println("Reading JSON file from Python");
                String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
                return objectMapper.readValue(jsonContent, new TypeReference<List<Recipe>>() {});
        } 
        return null;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
