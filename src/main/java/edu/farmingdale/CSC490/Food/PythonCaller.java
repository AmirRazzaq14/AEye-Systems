package edu.farmingdale.CSC490.Food;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PythonCaller.class);
    Gson gson = new Gson();
    private static final String PYTHON_API_URL = System.getenv("PYTHON_API_URL") != null ? 
        System.getenv("PYTHON_API_URL") :"http://localhost:" + getPythonServicePort();

    // A secondary method to get a Python service port
    private static int getPythonServicePort() {
        try {
            String configuredPort = System.getenv("PYTHON_API_PORT");
            if (configuredPort != null && !configuredPort.isEmpty()) {
                return Integer.parseInt(configuredPort);
            }
            // Default port
            return 8000;
        } catch (NumberFormatException e) {
            logger.error("Invalid port configuration, using default port: 8000", e);
            return 8000;
        }
    }

    public Nutrition_log analyze(byte[] imageBytes, String originalFilename, String prompt) {
        logger.info("Starting analysis for image: {}, prompt: {}", originalFilename, prompt);
        
        if (imageBytes == null || imageBytes.length == 0) {
            logger.error("Image bytes are null or empty");
            return null;
        }
        
        if (prompt == null || prompt.trim().isEmpty()) {
            logger.error("Prompt is null or empty");
           return null;
        }
        
        try {
            // Create a temporary file for the image
            Path tempFile = Files.createTempFile("image_", "_" + originalFilename);
            logger.debug("Created temporary file: {}", tempFile.toString());
            Files.write(tempFile, imageBytes);

            String image = tempFile.toString();

            try {
                // Call the Python API service
                String jsonResponse = callPythonApi(image,prompt);
                logger.debug("Received response from Python API: {}", jsonResponse);

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
                    logger.warn("Could not parse result. Response: {}", actualJson);
                    return null;
                }

                logger.info("Successfully analyzed image: {} with food name: {}", originalFilename, result.getFoodName());

                // Convert FoodResult to Nutrition_log
                Nutrition_log nutritionLog = Nutrition_log.builder()
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

                logger.info("Created nutrition log for food: {}", result.getFoodName());
                return nutritionLog;

            } finally {
                // Clean up temporary file
                boolean deleted = Files.deleteIfExists(tempFile);
                logger.debug("Temporary file {} deletion: {}", tempFile, deleted);
            }

        } catch (Exception e) {
            logger.error("Exception in PythonCaller.analyze: ", e);
        }
        return null;
    }

    private String callPythonApi(String imagePath,  String prompt) {
        logger.debug("Calling Python API with image: {} and prompt: {}", imagePath, prompt);
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
            String promptPath = getParsedPromptPath(prompt);
            logger.debug("Using prompt file path: {}", promptPath);
            body.add("prompt_file", promptPath);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                PYTHON_API_URL + "/analyze-upload",
                requestEntity, 
                String.class
            );
            
            logger.debug("Python API response status: {}", response.getStatusCode());
            return response.getBody();
            
        } catch (ResourceAccessException e) {
            logger.error("Error connecting to Python API: ", e);
            return "{\"error\": \"Failed to connect to Python API: " + e.getMessage() + 
                   ". Please make sure the Python FastAPI server is running at " + PYTHON_API_URL + "\"}";
        } catch (Exception e) {
            logger.error("Error calling Python API: ", e);
            return "{\"error\": \"Failed to connect to Python API: " + e.getMessage() + "\"}";
        }
    }
    
    // method to get a prompt file path
    private String getParsedPromptPath(String prompt) {
        logger.debug("Getting prompt file path for: {}", prompt);
        // If the prompt is already the full path, use it directly
        if (new File(prompt).exists()) {
            logger.debug("Prompt file exists at provided path: {}", prompt);
            return prompt;
        }
        
        // Otherwise, construct a relative path
        String projectRoot = System.getProperty("user.dir");
        if (projectRoot == null || projectRoot.isEmpty()) {
            projectRoot = System.getProperty("user.home") + "/AEye-Systems";
        }
        String scriptDir = new File(projectRoot, "src/main/resources/AI").getAbsolutePath();
        String fullPath = new File(scriptDir, prompt).getAbsolutePath();
        logger.debug("Constructed prompt file path: {}", fullPath);
        return fullPath;
    }
}

