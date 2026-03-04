package edu.farmingdale.CSC490.Food;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import org.springframework.stereotype.Service;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;




@Service
public class PythonCaller {

    Gson gson = new Gson();
    private static final String PYTHON_API_URL = System.getenv("PYTHON_API_URL") != null ? 
        System.getenv("PYTHON_API_URL") : "http://localhost:8001";

    public Nutrition_log analyze(byte[] imageBytes, String originalFilename) {
        try {
            // Create a temporary file for the image
            Path tempFile = Files.createTempFile("image_", "_" + originalFilename);
            Files.write(tempFile, imageBytes);

            String prompt = "food_analyze_prompt";
            String image = tempFile.toString();

            try {
                // Call the Python API service
                String jsonResponse = callPythonApi(image,prompt);

                //System.out.println("Raw response from Python API: " + jsonResponse);

                // Check if response is an error message
                if (jsonResponse.contains("\"error\"")) {
                    System.out.println("Error from Python API: " + jsonResponse);
                    return null;
                }

                // Extract the actual result if it's wrapped in a result field
                String actualJson = jsonResponse;
                if (jsonResponse.contains("\"result\"")) {
                    JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                    if (jsonObject.has("result")) {
                        actualJson = jsonObject.getAsJsonObject("result").toString();
                    }
                }

                // Convert JSON to FoodResult type
                FoodResult result = gson.fromJson(actualJson, FoodResult.class);

                if(result == null){
                    System.out.println("Could not parse result. Response: " + actualJson);
                    return null;
                }

                // Convert FoodResult to Nutrition_log
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

            } finally {
                // Clean up temporary file
                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
            System.err.println("Exception in PythonCaller.analyze: " + e.getMessage());
        }
        return null;
    }

    private String callPythonApi(String imagePath,  String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create form data
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // Add the image file
            FileSystemResource imageResource = new FileSystemResource(new File(imagePath));
            body.add("image", imageResource);
            
            // Add the prompt file path
            String projectRoot = System.getProperty("user.dir");
            String scriptDir = new File(projectRoot, "src/main/resources/AI").getAbsolutePath();
            String promptPath = new File(scriptDir, prompt).getAbsolutePath();
            body.add("prompt", promptPath);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                PYTHON_API_URL + "/analyze-upload",
                requestEntity, 
                String.class
            );
            
            return response.getBody();
            
        } catch (ResourceAccessException e) {
            System.err.println("Error connecting to Python API: " + e.getMessage());
            return "{\"error\": \"Failed to connect to Python API: " + e.getMessage() + 
                   ". Please make sure the Python FastAPI server is running at " + PYTHON_API_URL + "\"}";
        } catch (Exception e) {
            System.err.println("Error calling Python API: " + e.getMessage());
            return "{\"error\": \"Failed to connect to Python API: " + e.getMessage() + "\"}";
        }
    }
}

