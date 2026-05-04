package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Config.FirebaseTokenFilter;
import edu.farmingdale.CSC490.Entity.Motion_log;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Food.AISuggestionService;
import edu.farmingdale.CSC490.Food.FoodAnalyzeService;
import edu.farmingdale.CSC490.Service.MotionLogService;
import edu.farmingdale.CSC490.Service.NutritionLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/nutrition-logs")
public class NutritionLogController {

    @Autowired
    private NutritionLogService service;

    @Autowired
    private FirebaseTokenFilter tokenFilter;

    @Autowired
    private FoodAnalyzeService foodAnalyzeService;


    @Autowired
    private AISuggestionService aiSuggestionService;

    @Autowired
    private MotionLogService motionLogService;

    @PostMapping("/analyze-image")
    public ResponseEntity<Nutrition_log.Meal> analyzeFood(
            @RequestParam("image") MultipartFile image) {

        log.info("Analyze the image : {}", image.getOriginalFilename());

        Nutrition_log.Meal result = foodAnalyzeService.analyze(image);
        return Optional.ofNullable(result).isPresent()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().build();

    }

    @PostMapping
    public ResponseEntity<?> save(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Nutrition_log nutrition_log) {

        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            
            // Ensure each meal has an ID and valid values
            if (nutrition_log.getMeals() != null) {
                nutrition_log.getMeals().forEach(meal -> {
                    if (meal.getMealId() == null || meal.getMealId().isEmpty()) {
                        meal.setMealId("meal_" + System.currentTimeMillis() + "_" + Math.random());
                    }
                    // Ensure nutritional values are strings and have defaults
                    if (meal.getCals() == null || meal.getCals().isEmpty()) meal.setCals("0");
                    if (meal.getProtein() == null || meal.getProtein().isEmpty()) meal.setProtein("0");
                    if (meal.getCarb() == null || meal.getCarb().isEmpty()) meal.setCarb("0");
                    if (meal.getFat() == null || meal.getFat().isEmpty()) meal.setFat("0");
                });
            }
            
            // Set the userId
            nutrition_log.setUserId(uid);
            
            service.saveLog(uid, nutrition_log);
            log.info("Save nutrition log successfully for date: {}", nutrition_log.getDate());
            return ResponseEntity.ok("{\"message\":\"Saved\"}");
        } catch (Exception e) {
            log.error("Error save nutrition log: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestHeader("Authorization") String authHeader) {

        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            List<Nutrition_log> logs = service.getAllLogs(uid);
            log.debug("Get {} nutrition logs",logs.size());
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error get all log: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{date}")
    public ResponseEntity<?> getByDate(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String date) {
        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);

            Nutrition_log nutrition_log = service.getLog(uid, date);

            if(nutrition_log != null) {
                log.info("Successfully get nutrition log by date:{}", date);
                return ResponseEntity.ok(nutrition_log);
            }else {
                log.warn("Nutrition log not found by date:{}", date);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error to get log by this date {} : {}", date, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    @GetMapping("/week/{date}")
    public ResponseEntity<?> getByWeek(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String date) {
        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            List<Nutrition_log> nutrition_log = service.getWeekLog(uid, date);
            if(nutrition_log != null) {
                log.info("Successfully get nutrition log this week");
                return ResponseEntity.ok(nutrition_log);
            }else {
                log.warn("Nutrition log not found by week:{}", date);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error to get log by this week {} : {}", date, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }

    @DeleteMapping("/{date}")
    public ResponseEntity<?> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String date) {

        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            service.deleteLog(uid, date);
            log.info("Successfully delete nutrition log by date:{}",date);
            return ResponseEntity.ok("{\"message\":\"Deleted\"}");
        } catch (Exception e) {
            log.error("Error to delete log by this date {} : {}", date, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/meals/{date}")
    public ResponseEntity<?> saveMeal(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String date,
            @RequestBody Nutrition_log.Meal meal) {

        if (meal == null) {
            log.warn("Meal data is required");
            return ResponseEntity.badRequest().body("{\"message\":\"Meal data is required\"}");
        }

        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            
            // Generate meal ID if not provided
            if (meal.getMealId() == null || meal.getMealId().isEmpty()) {
                meal.setMealId("meal_" + System.currentTimeMillis() + "_" + Math.random());
            }
            
            service.saveMeal(uid, date, meal);
            log.info("Successfully save Meal on {} with meal id {}", date, meal.getMealId());
            return ResponseEntity.ok().body("{\"message\":\"Saved\"}");
        } catch (Exception e) {
            log.error("Error to save Meal on log {} : {}", date, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"message\":\"Failed to save meal: " + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/meals/{date}/{mealId}")
    public ResponseEntity<?> deleteMeal(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String date,
            @PathVariable String mealId) {

        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            service.deleteMeal(uid, date, mealId);
            log.info("Successfully delete Meal on {} with meal id {}", date, mealId);
            return ResponseEntity.ok("{\"message\":\"Deleted\"}");
        } catch (Exception e) {
            log.error("Error to delete Meal on log {} : {}", date, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/meals/{date}/{mealId}")
    public ResponseEntity<?> updateMeal(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String date,
            @PathVariable String mealId,
            @RequestBody Nutrition_log.Meal meal) {
        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            service.updateMeal(uid, date, mealId, meal);
            log.info("Successfully update Meal on {} with meal id {}", date, mealId);
            return ResponseEntity.ok("{\"message\":\"Updated\"}");
        } catch (Exception e) {
            log.error("Error to update Meal on log {} : {}", date, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/notes/{date}")
    public ResponseEntity<?> saveNotes(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String date,
            @RequestBody Map<String, String> body) {

        String note = body.get("notes");
        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            service.saveNotes(uid, date, note);
            log.info("Successfully save notes on log {}", date);
            return ResponseEntity.ok("{\"message\":\"Saved\"}");
        } catch (Exception e) {
            log.error("Error to save notes on log {} : {}", date, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }

    @GetMapping("/suggestions/{date}")
    public ResponseEntity<?> getSuggestion(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String date) {

        log.info("Getting suggestions");
        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            Nutrition_log nutrition_log = service.getLog(uid, date);

            if(nutrition_log != null) {
                String suggestion = aiSuggestionService.generateDailySuggestion(nutrition_log);
                Map<String, String> response = Map.of("suggestions", suggestion);
                return ResponseEntity.ok(response);
            }else {
                log.warn("Today's nutrition log({}) not being build", date);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error to get suggestion by this date {} : {}", date, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }

    @GetMapping("/burnedCalories/{date}")
    public ResponseEntity<?> getExerciseCalories(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String date) {

        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            Motion_log motionLog = motionLogService.getLog(uid, date);
            double burned = motionLog != null ? motionLog.getCaloriesTotal() : 0.0;
            log.info("Burned calories for uid {} date {}: {}", uid, date, burned);
            return ResponseEntity.ok(Map.of("burnedCalories", burned));
        } catch (Exception e) {
            log.error("Error to get exercise calories by this date {} : {}", date, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }



}