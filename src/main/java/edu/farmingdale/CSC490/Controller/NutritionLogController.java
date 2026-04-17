package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Config.FirebaseTokenFilter;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Entity.User;
import edu.farmingdale.CSC490.Food.AISuggestionService;
import edu.farmingdale.CSC490.Food.FoodAnalyzeService;
import edu.farmingdale.CSC490.Service.NutritionLogService;
import edu.farmingdale.CSC490.Service.UserService;
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
    private UserService userService;

    @Autowired
    private AISuggestionService aiSuggestionService;

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

            service.saveLog(uid, nutrition_log);
            log.info("Save nutrition log successfully");
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
                getNutritionLogWithTarget(authHeader,nutrition_log);
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

    private void getNutritionLogWithTarget(String authHeader, Nutrition_log log) throws Exception {
        String uid = tokenFilter.verifyAndGetUid(authHeader);
        User user = userService.getUserById(uid);
        log.updateTargetNutrition(user);
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

        if (meal.getMealId() == null) {
            meal.setMealId("meals_" + System.currentTimeMillis());
        }

        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            service.saveMeal(uid, date, meal);
            log.info("Successfully save Meal on {} with meal id {}", date, meal.getMealId());
            return ResponseEntity.ok().body("{\"message\":\"Saved\"}");
        } catch (Exception e) {
            log.error("Error to save Meal on log {} : {}", date, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
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
            log.info("Successfully get exercise calories");
            return ResponseEntity.ok("123");
        } catch (Exception e) {
            log.error("Error to get exercise calories by this date {} : {}", date, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }



}