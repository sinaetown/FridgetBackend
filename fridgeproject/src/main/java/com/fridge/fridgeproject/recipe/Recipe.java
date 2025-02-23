package com.fridge.fridgeproject.recipe;


import java.io.*;

public class Recipe {
    public static void main(String[] args) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "hopperhacks_backend/hopperhacks_backend/fridgeproject/src/main/java/com/fridge/fridgeproject/recipe/Prompt.py");
            pb.redirectErrorStream(true); // Merge error output with standard output
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Print output from Python script
            }

            int exitCode = process.waitFor();
            System.out.println("Python script exited with code: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
