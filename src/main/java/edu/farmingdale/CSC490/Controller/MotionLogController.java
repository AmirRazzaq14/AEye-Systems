package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Config.FirebaseTokenFilter;
import edu.farmingdale.CSC490.Entity.Motion_log;
import edu.farmingdale.CSC490.Service.MotionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/motion-logs")
public class MotionLogController {

    @Autowired
    private MotionLogService service;

    @Autowired
    private FirebaseTokenFilter tokenFilter;

    @PostMapping
    public ResponseEntity<?> save(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Motion_log log) {
        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            if (log.getDate() == null || log.getDate().isBlank()) {
                return ResponseEntity.badRequest().body("Missing date");
            }
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
            List<Motion_log> logs = service.getAllLogs(uid);
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
            Motion_log log = service.getLog(uid, date);
            return log != null ? ResponseEntity.ok(log)
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}

