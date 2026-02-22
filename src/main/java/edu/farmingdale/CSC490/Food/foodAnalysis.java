package edu.farmingdale.CSC490.Food;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class foodAnalysis {
    static String imagePath = "C:\\Users\\user\\Downloads\\apple2.jpg";
    public FoodResult analyzeImage() {
        try {
            String json = getJsonFromPython(imagePath);
            System.out.println("JSON: " + json);

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
        String scriptDir = projectRoot + "/src/main/java/edu/farmingdale/CSC490/Food";

        // Construct commands: python hello.py image
        String[] command = {"python", "ollamaAI.py", imagePath};

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