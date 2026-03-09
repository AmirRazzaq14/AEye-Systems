package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Config.FirebaseTokenFilter;
import edu.farmingdale.CSC490.Service.ExerciseLogService;
import edu.farmingdale.CSC490.Entity.Exercise_log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercise-logs")
public class ExerciseLogController {

    @Autowired
    private ExerciseLogService service;

    @Autowired
    private FirebaseTokenFilter tokenFilter;

    @PostMapping
    public ResponseEntity<?> save(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Exercise_log log) {
        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader); // ← get uid from token
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
            List<Exercise_log> logs = service.getAllLogs(uid);
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
            Exercise_log log = service.getLog(uid, date);
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