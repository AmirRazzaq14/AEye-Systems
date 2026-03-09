package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Config.FirebaseTokenFilter;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Service.NutritionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nutrition-logs")
public class NutritionLogController {

    @Autowired
    private NutritionLogService service;

    @Autowired
    private FirebaseTokenFilter tokenFilter;

    @PostMapping
    public ResponseEntity<?> save(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Nutrition_log log) {
        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            service.saveLog(uid, log);
            return ResponseEntity.ok("Saved");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            List<Nutrition_log> logs = service.getAllLogs(uid);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{date}")
    public ResponseEntity<?> getByDate(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String date) {
        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            Nutrition_log log = service.getLog(uid, date);
            return log != null ? ResponseEntity.ok(log)
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
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
            return ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}