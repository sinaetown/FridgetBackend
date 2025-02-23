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
    private List<String> ingredients;
    private List<String> steps;
    private String reference;
    
    public static void main(String[] args) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/Prompt.py");
            pb.redirectErrorStream(true); 
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); 
            }

            int exitCode = process.waitFor();
            System.out.println("Python script exited with code: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
