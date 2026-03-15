package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Food.FoodAnalyzeService;
import edu.farmingdale.CSC490.Food.FoodResult;
import edu.farmingdale.CSC490.Food.config.ConfigLoader;
import edu.farmingdale.CSC490.Repository.NutritionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/food")
@CrossOrigin(origins = "*")
public class FoodController {

    @Value("${prompt.food_analyzer}")
    private String promptFilePath;

    @Autowired
    private FoodAnalyzeService foodAnalyzeService;

    @Autowired
    private NutritionRepository nutritionRepository;

    @Autowired
    private ConfigLoader configLoader;


    @PostMapping("/analyze")
    public ResponseEntity<FoodResult> analyzeFood(
            @RequestParam("image") MultipartFile image)  {
        log.info("Received image file: {}", image.getOriginalFilename());

        configLoader.validateConfig();

        try {
            Path promptFile = Path.of("src/main/java/edu/farmingdale/CSC490/Food/prompt", promptFilePath);

            Path tempImageFile = Files.createTempFile("food_image_", "_" + image.getOriginalFilename());
            Files.write(tempImageFile, image.getBytes());

            // Call AI image analysis services
            Optional<FoodResult> result = foodAnalyzeService.analyze(tempImageFile.toString(), promptFile.toString());


            if (result.isEmpty()) {
                log.error("Failed to parse food result from AI response");
                return ResponseEntity.status(502).body(null);
            }

            log.info("Successfully analyzed food: {}", result.get().getFoodName());
            log.info("Food result: {}", result);

            return ResponseEntity.ok(result.get());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }



    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Food controller is running");
    }


}