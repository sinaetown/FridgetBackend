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
            ProcessBuilder pb = new ProcessBuilder("python3", "/Users/sinaehong/Downloads/hopperhacks_backend/fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/prompt_v2.py");
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

            String jsonFilePath = "/Users/sinaehong/Downloads/hopperhacks_backend/fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/recipes.json";
            File jsonFile = new File(jsonFilePath);

            ProcessBuilder pb2 = new ProcessBuilder("/usr/local/bin/python3",
                    "/Users/sinaehong/Downloads/hopperhacks_backend/fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/ratensort.py");
            pb2.redirectErrorStream(true);
            Process process2 = pb2.start();

            BufferedReader reader2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));
            StringBuilder output2 = new StringBuilder();
            while ((line = reader2.readLine()) != null) {
                output2.append(line).append("\n");
            }
            System.out.println("Python rantensort.py Output: \n" + output2.toString());

            int exitCode2 = process2.waitFor();
            if (exitCode2 != 0) {
                System.out.println("❌ Error: rantensort.py execution failed. Exit code: " + exitCode2);
                return Collections.emptyList();
            }

            String resultJsonPath = "/Users/sinaehong/Downloads/hopperhacks_backend/fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/results.json";
            File resultJsonFile = new File(resultJsonPath);
            if (!resultJsonFile.exists()) {
                List<Recipe> emptyRecipeList = new ArrayList<>();
                objectMapper.writeValue(resultJsonFile, emptyRecipeList);
            }

            String jsonContent = new String(Files.readAllBytes(Paths.get(resultJsonPath)));
            List<Recipe> sortedRecipes = objectMapper.readValue(jsonContent, new TypeReference<List<Recipe>>() {});

            for (Recipe sr : sortedRecipes) {
                List<String> missingIngredients = new ArrayList<>();
                for (Ingredient ingredient : sr.getIngredients()) {
                    if (!userIngredients.contains(ingredient.getName().toLowerCase())) {
                        missingIngredients.add(ingredient.getName());
                    }
                }
                sr.setMissingIngredients(missingIngredients);
            }

            return sortedRecipes;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
