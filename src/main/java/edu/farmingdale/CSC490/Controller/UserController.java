package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Entity.User;
import edu.farmingdale.CSC490.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private edu.farmingdale.CSC490.Config.FirebaseTokenFilter tokenFilter;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> request) {
        try {
            User user = userService.login(
                    request.get("email"),
                    request.get("password")
            );
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user_id", user.getUser_id());
            response.put("email", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody User user) {
        try {
            userService.register(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Account created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<User> getUser(
            @PathVariable String userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> updates) {
        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            userService.updateProfile(uid, updates);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String uid = tokenFilter.verifyAndGetUid(authHeader);
            User user = userService.getUserById(uid);
            
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("profile", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }
}
