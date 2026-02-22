package edu.farmingdale.CSC490.Food;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class foodAnalysis {

    public FoodResult analyzeImage(String image) {
        try {
            String json = getJsonFromPython(image);
            System.out.println("Raw response from Python: " + json);

            // Check if the returned string is empty or not in valid JSON format
            if (json.trim().isEmpty()) {
                System.err.println("Empty response from Python script");
                return null;
            }

            // Verify that it is a valid JSON object format
            String trimmedJson = json.trim();
            if (!trimmedJson.startsWith("{") && !trimmedJson.startsWith("[")) {
                System.err.println("Invalid JSON format received: " + json);
                return null;
            }
            // Use Gson to convert to FoodResult type
            Gson gson = new Gson();
            return gson.fromJson(json, FoodResult.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getJsonFromPython(String imagePath) throws IOException, InterruptedException {
        String projectRoot = System.getProperty("user.dir");
        String scriptDir = new File(projectRoot, "src/main/java/edu/farmingdale/CSC490/Food").getAbsolutePath();
        String fullImagePath = new File(projectRoot, imagePath).getAbsolutePath();

        System.out.println(fullImagePath);

        // Construct commands: python hello.py image
        String[] command = {"python", "ollamaAI.py", fullImagePath };

        //Create a process to execute the command and
        //set the working directory to the directory where the script is located
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(scriptDir));
        Process process = pb.start();

        // Read output from Python
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
        );

        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        process.waitFor();

        return output.toString();
    }
}