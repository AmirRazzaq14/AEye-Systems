package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Food.PythonCaller;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/food")
@CrossOrigin(origins = "*")
public class FoodController {

    @Value("${prompt.food_analyzer:food_analyze_prompt}")
    private String prompt;
    
    @Autowired
    private PythonCaller PythonCaller;

    @PostMapping("/analyze")
    public ResponseEntity<Nutrition_log> analyzeFood(
            @RequestParam("image") MultipartFile image) {

        
        try {
            if (image.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Call the Python service for image analysis
            Nutrition_log result = PythonCaller.analyze(
                    image.getBytes(),
                    image.getOriginalFilename(),
                    prompt
            );

            //  If no result found, return a default result
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.internalServerError().build();
            }
            
        } catch (Exception e) {
            System.err.println("Error analyzing food image: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Food controller is running");
    }


}