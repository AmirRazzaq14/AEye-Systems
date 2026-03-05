package edu.farmingdale.CSC490.Food;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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


@Service
public class foodAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(foodAnalyzer.class);

    Gson gson = new Gson();

    @Value("${PYTHON_API_URL}")
    private String PYTHON_API_URL;

    public FoodResult analyze(byte[] imageBytes, String originalFilename, String prompt) {
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

                FoodResult result;
                // Extract the actual result if it's wrapped in a result field
                String actualJson = jsonResponse;
                if (jsonResponse.contains("\"result\"")) {
                    JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                    if (jsonObject.has("result")) {
                        actualJson = jsonObject.getAsJsonObject("result").toString();
                        logger.debug("Extracted result: {}", actualJson);

                    }
                }else if (jsonResponse.contains("\"error\"")) {
                    JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                    if (jsonObject.has("error")) {
                        actualJson = jsonObject.get("error").toString();
                        logger.warn("Error from Python API: {}", actualJson);

                        return null;
                    }
                }

                // Convert JSON to FoodResult type
                result = gson.fromJson(actualJson, FoodResult.class);

                if(result == null){
                    logger.warn("Could not parse result. Response: {}", actualJson);
                    return null;
                }

                logger.info("Successfully analyzed image: {} with food name: {}", originalFilename, result.getFoodName());

                return result;


            } finally {
                // Clean up temporary file
                boolean deleted = Files.deleteIfExists(tempFile);
                logger.debug("Temporary file {} deletion: {}", tempFile, deleted);
            }

        } catch (Exception e) {
            logger.error("Exception in foodAnalyzer.analyze: ", e);
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

