package edu.farmingdale.CSC490.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.farmingdale.CSC490.Food.FoodResult;
import edu.farmingdale.CSC490.Food.AIImageAnalyzer;
import edu.farmingdale.CSC490.Repository.NutritionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/food")
@CrossOrigin(origins = "*")
public class FoodController {

    private static final Logger logger = LoggerFactory.getLogger(FoodController.class);

    @Value("${prompt.food_analyzer}")
    private String promptFilePath;
    
    @Autowired
    private AIImageAnalyzer aiImageAnalyzer;

    @Autowired
    private NutritionRepository nutritionRepository;

    @PostMapping("/analyze")
    public ResponseEntity<FoodResult> analyzeFood(
            @RequestParam("image") MultipartFile image) {

        
        try {
            if (image.isEmpty()) {
                logger.error("Image file is empty.");
                return ResponseEntity.badRequest().build();
            }

            logger.info("Received image file: {}", image.getOriginalFilename());

            Path promptFile = Path.of("src/main/java/edu/farmingdale/CSC490/Food",promptFilePath);


            // Create a temporary file to save the uploaded image
            Path tempImageFile = Files.createTempFile("food_image_", "_" + image.getOriginalFilename());
            Files.write(tempImageFile, image.getBytes());

            
            try {
                // Call AI image analysis services
                String jsonResponse = aiImageAnalyzer.analyze(tempImageFile.toString(), promptFile.toString());
                logger.debug("AI Response: {}", jsonResponse);
                
                // Convert the JSON response to a FoodResult object
                FoodResult result = parseFoodResultFromJson(jsonResponse);
                
                if (result == null) {
                    logger.error("Failed to parse food result from AI response");
                    return ResponseEntity.status(502).body(null);
                }
                
                logger.info("Successfully analyzed food: {}", result.getFoodName());
                logger.info("Food result: {}", result);
                
                return ResponseEntity.ok(result);

            } finally {
                Files.deleteIfExists(tempImageFile);
            }

        } catch (Exception e) {
            logger.error("Error analyzing food image: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
    
    private FoodResult parseFoodResultFromJson(String jsonResponse) {
        try {
            // Use objectMapper in AIImageAnalyzer to parse JSON
            ObjectMapper mapper = new ObjectMapper();

            // Parse the JSON response
            JsonNode rootNode = mapper.readTree(jsonResponse);

            // Check if it's an error response
            if (rootNode.has("error") && rootNode.get("error").asBoolean()) {
                String errorMessage = rootNode.has("message") ? rootNode.get("message").asText() : "Unknown error";
                logger.error("API returned error: {}", errorMessage);
                return null;
            }

            // Check if it's Ollama's response format (including response field)
            if (rootNode.has("response")) {
                // Get the nested JSON string
                String nestedJson = rootNode.get("response").asText();

                // Parse the nested JSON to FoodResult
                return mapper.readValue(nestedJson, FoodResult.class);
            }

            // Check if it's the Gemini API's response format (including the candidates field)
            if (rootNode.has("candidates") && rootNode.get("candidates").isArray()) {
                JsonNode candidatesArray = rootNode.get("candidates");
                if (!candidatesArray.isEmpty()) {
                    JsonNode firstCandidate = candidatesArray.get(0);
                    if (firstCandidate.has("content") && firstCandidate.get("content").has("parts")) {
                        JsonNode partsArray = firstCandidate.get("content").get("parts");
                        if (!partsArray.isEmpty()) {
                            JsonNode firstPart = partsArray.get(0);
                            if (firstPart.has("text")) {
                                // Get the actual FoodResult JSON string
                                String foodResultJson = firstPart.get("text").asText();

                                // Resolves to a FoodResult object
                                return mapper.readValue(foodResultJson, FoodResult.class);
                            }
                        }
                    }
                }
            }

            // If none of the above formats are used, try parsing directly to FoodResult
            return mapper.treeToValue(rootNode, FoodResult.class);
        } catch (Exception e) {
            logger.error("Error parsing JSON response: {}", e.getMessage());
            return null;
        }
    }

    public void saveToDatabase(FoodResult result){

        try {
            logger.info("Saving food result to database: {}", result);

            //TODO: save to database with nutrition log error with diff data type
            //nutritionRepository.save(nutrition_log);
        } catch (Exception e) {
            logger.error("Error saving food result to database: {}", e.getMessage());
        }
    }


    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Food controller is running");
    }


}