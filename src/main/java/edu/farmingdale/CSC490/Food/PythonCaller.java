package edu.farmingdale.CSC490.Food;

import com.google.gson.Gson;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;

@Service
public class PythonCaller {

    Gson gson = new Gson();

    public Nutrition_log analyze(byte[] imageBytes, String originalFilename) {
        try {

            //Save the picture to a temporary file
            Path tempDir = Files.createTempDirectory("food_analyzer");
            Path tempFile = tempDir.resolve(originalFilename);
            Files.write(tempFile, imageBytes);

            // get output from python
            String imagePath = tempFile.toAbsolutePath().toString();
            String json = getJsonFromPython(imagePath);
            System.out.println("Raw response from Python: " + json);

            // Clean up temporary files
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);

            // convert Json to FoodResult type
            FoodResult result = gson.fromJson(json, FoodResult.class);

            if(result == null){
                System.out.println("No result found.");
                return null;
            }

            //  convert FoodResult to Nutrition_log
            return Nutrition_log.builder()
                    .user_id(1)
                    .log_id(1)
                    .log_date(LocalDate.now())
                    .meal_type(result.getMealType())
                    .food_name(result.getFoodName())
                    .calories(result.getCalories().intValue())
                    .protein_grams(result.getProtein_grams().intValue())
                    .carbs_grams(result.getCarbs_grams().intValue())
                    .fat_grams(result.getFat_grams().intValue())
                    .logged_at(Instant.now())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getJsonFromPython(String imagePath) throws IOException, InterruptedException {
        String projectRoot = System.getProperty("user.dir");
        String scriptDir = new File(projectRoot, "src/main/resources/AI").getAbsolutePath();
        String fullImagePath = new File(imagePath).getAbsolutePath();
        String scriptPath = new File(scriptDir, "ollamaAI.py").getAbsolutePath();
        String promptPath = new File(scriptDir, "food_analyze_prompt").getAbsolutePath();

        // Construct commands to execute the Python script
        String[] command = {"python", scriptPath, fullImagePath,promptPath};

        //System.out.println(Arrays.toString(command));

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