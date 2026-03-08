package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Food.FoodResult;
import edu.farmingdale.CSC490.Food.foodAnalyzer;
import edu.farmingdale.CSC490.Repository.NutritionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/food")
@CrossOrigin(origins = "*")
public class FoodController {

    private static final Logger logger = LoggerFactory.getLogger(FoodController.class);

    @Value("${prompt.food_analyzer}")
    private String prompt;
    
    @Autowired
    private foodAnalyzer foodAnalyzer;

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
            // Call the Python service for image analysis
            FoodResult result = foodAnalyzer.analyze(
                    image.getBytes(),
                    image.getOriginalFilename(),
                    prompt
            );
            logger.debug("Result: {}",result);

            assert result != null;
            //saveToDatabase(result);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error analyzing food image: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }


    public void saveToDatabase(FoodResult result){

        try {
            logger.info("Saving food result to database: {}", result);
            Nutrition_log nutrition_log = Nutrition_log.builder()
                    .log_id(1)
                    .user_id(1)
                    .log_date(LocalDate.now())
                    .meal_type(result.getMealType())
                    .food_name(result.getFoodName())
                    .calories(result.getCalories())
                    .protein_grams(result.getProtein_grams())
                    .carbs_grams(result.getCarbs_grams())
                    .fat_grams(result.getFat_grams())
                    .logged_at(Instant.now())
                    .build();

            //TODO: save to database with nutrition log error with diff data type
            nutritionRepository.save(nutrition_log);
        } catch (Exception e) {
            logger.error("Error saving food result to database: {}", e.getMessage());
        }
    }

    @GetMapping("/logs/{userId}")
    public ResponseEntity<List<Nutrition_log>> getUserLogs(@PathVariable String userId) {
        try {
            List<Nutrition_log> logs = nutritionRepository.findByUserId(userId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("Error retrieving user logs: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/logs/{logId}")
    public ResponseEntity<Void> deleteLog(@PathVariable String logId) {
        try {
            nutritionRepository.delete(logId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting log: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }


    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Food controller is running");
    }


}