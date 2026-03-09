package edu.farmingdale.CSC490.Food;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
public class foodAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(foodAnalyzer.class);

    Gson gson =  new GsonBuilder()
            .registerTypeAdapter(Double.class, (JsonDeserializer<Double>) (json, typeOfT, context) -> json.getAsDouble())
            .registerTypeAdapter(double.class, (JsonDeserializer<Double>) (json, typeOfT, context) -> json.getAsDouble())
            .registerTypeAdapter(int.class, (JsonDeserializer<Integer>) (json, typeOfT, context) -> Math.round(json.getAsFloat()))
            .registerTypeAdapter(Integer.class, (JsonDeserializer<Integer>) (json, typeOfT, context) -> Math.round(json.getAsFloat()))
            .create();


    @Autowired
    private AIImageAnalyzer aiImageAnalyzer;

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
                // Create a temporary prompt file
                Path tempPromptFile = Files.createTempFile("prompt_", ".txt");
                Files.write(tempPromptFile, prompt.getBytes());
                
                // Call the Java AI service instead of Python API
                String jsonResponse = aiImageAnalyzer.analyze(tempFile.toString(), tempPromptFile.toString());
                logger.debug("Received response from Java AI: {}", jsonResponse);

                FoodResult result;
                
                // Check if response is a valid JSON object
                if (jsonResponse != null && !jsonResponse.trim().isEmpty()) {
                    try {
                        // Try to parse as JSON
                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                        
                        // Check if it's an error response
                        if (jsonObject.has("error")) {
                            String errorMessage = jsonObject.get("error").getAsString();
                            logger.error("Error from Java AI: {}", errorMessage);
                            return null;
                        }
                        
                        // Attempt to convert JSON to FoodResult type
                        result = gson.fromJson(jsonObject, FoodResult.class);
                        
                        if(result == null){
                            logger.warn("Could not parse result. Response: {}", jsonResponse);
                            return null;
                        }
                        
                        logger.info("Successfully analyzed image: {} with food name: {}", originalFilename, result.getFoodName());
                        return result;
                        
                    } catch (JsonSyntaxException e) {
                        // If it's not valid JSON, it might be an error string or plain text
                        logger.warn("Response is not valid JSON: {}", jsonResponse);
                        
                        // Check if it looks like an error message
                        if (jsonResponse.toLowerCase().contains("error") || 
                            jsonResponse.toLowerCase().contains("exception") ||
                            jsonResponse.startsWith("ERROR:") ||
                            jsonResponse.startsWith("Exception")) {
                            logger.error("Error from Java AI: {}", jsonResponse);
                            return null;
                        } else {
                            // If it's not an error, maybe it's malformed response
                            logger.error("Invalid response format from AI: {}", jsonResponse);
                            return null;
                        }
                    }
                } else {
                    logger.error("Empty response from Java AI");
                    return null;
                }

            } finally {
                // Clean up temporary files
                Files.deleteIfExists(tempFile);
                logger.debug("Temporary files deletion completed");
            }

        } catch (Exception e) {
            logger.error("Exception in foodAnalyzer.analyze: ", e);
        }
        return null;
    }
}

