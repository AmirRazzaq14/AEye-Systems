package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Food.FoodAnalyzeService;
import edu.farmingdale.CSC490.Food.config.ConfigLoader;
import edu.farmingdale.CSC490.Food.util.FileUtils;
import edu.farmingdale.CSC490.Repository.NutritionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Autowired
    private FileUtils fileUtils;

    @PostMapping("/analyze")
    public ResponseEntity<Nutrition_log.Meal> analyzeFood(
            @RequestParam("image") MultipartFile image) {

        log.info("Received image file: {}", image.getOriginalFilename());

        configLoader.validateConfig();

        // Verify the uploaded file
        if (image.isEmpty()) {
            log.error("No image file provided");
            return ResponseEntity.badRequest().build();
        }

        // Limit file size (e.g. 10MB)
        if (image.getSize() > 10 * 1024 * 1024) {
            log.error("Image file too large: {} bytes", image.getSize());
            return ResponseEntity.status(413).build(); // Payload Too Large
        }

        try {
            Path promptFile = Path.of("src/main/java/edu/farmingdale/CSC490/Food/prompt", promptFilePath);
            if (!Files.exists(promptFile)) {
                log.error("Prompt file does not exist: {}", promptFile);
                return ResponseEntity.badRequest().build();
            }

            Optional<String> tempImagePath = fileUtils.tempFile(image);
            if (tempImagePath.isEmpty()) {
                log.error("Failed to create temp image file");
                return ResponseEntity.status(500).build(); // Internal Server Error
            }

            String imagePath = tempImagePath.get();
            try {
                // Call AI image analysis services
                Optional<Nutrition_log.Meal> result = foodAnalyzeService.analyze(imagePath, promptFile.toString());

                if (result.isEmpty()) {
                    log.error("Failed to analyze food");
                    return ResponseEntity.status(500).build();
                }

                log.info("Successfully analyzed food: {}", result.get().getName());
                log.info("Food result: {}", result.get());

                return ResponseEntity.ok(result.get());
            } finally {
                // Make sure temporary files are deleted
                try {
                    Files.deleteIfExists(Paths.get(imagePath));
                } catch (Exception e) {
                    log.warn("Failed to delete temporary file: {}", imagePath, e);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Food controller is running");
    }


}