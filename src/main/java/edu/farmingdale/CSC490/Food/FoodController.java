package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FoodController {
    
    @Autowired
    private PythonCaller pythonCaller;

    @PostMapping("/analyze")
    public ResponseEntity<Nutrition_log> analyzeFood(
            @RequestParam("file") MultipartFile file) {
        
        System.out.println("Received pictures: " + file.getOriginalFilename()
            + ", size: " + file.getSize() + " bytes");
        
        try {
            // Read the image binary data
            byte[] imageBytes = file.getBytes();
            
            // Call Python analysis
            Nutrition_log result = pythonCaller.analyze(
                imageBytes,
                file.getOriginalFilename()
            );

            //  If no result found, return a default result
            if(result == null){
                result = Nutrition_log.builder()
                    .user_id(1)
                    .log_id(1)
                    .log_date(LocalDate.now())
                    .meal_type("unknown")
                    .food_name("unknown")
                    .calories(1000)
                    .protein_grams(10)
                    .carbs_grams(10)
                    .fat_grams(10)
                    .logged_at(Instant.now())
                    .build();
            }


            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }


}