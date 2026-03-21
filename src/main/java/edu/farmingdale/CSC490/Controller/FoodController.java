package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Food.FoodAnalyzeService;
import edu.farmingdale.CSC490.Repository.NutritionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/food")
@CrossOrigin(origins = "*")
public class FoodController {

    @Autowired
    private FoodAnalyzeService foodAnalyzeService;



    @PostMapping("/analyze")
    public ResponseEntity<Nutrition_log.Meal> analyzeFood(
            @RequestParam("image") MultipartFile image) {

        log.info("Analyze the image : {}", image.getOriginalFilename());

        Optional<Nutrition_log.Meal> result = foodAnalyzeService.analyze(image);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());

    }


    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Food controller is running");
    }


}