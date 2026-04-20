package edu.farmingdale.CSC490.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ConfigController {

    // Optional web SDK config (Firebase Console → Project settings → Your apps). Empty defaults so
    // the app still starts on Railway when only server credentials (FIREBASE_CREDENTIALS_JSON) are set.
    @Value("${FIREBASE_API_KEY:}")
    private String apiKey;

    @Value("${FIREBASE_AUTH_DOMAIN:}")
    private String authDomain;

    @Value("${FIREBASE_PROJECT_ID:}")
    private String projectId;

    @Value("${FIREBASE_STORAGE_BUCKET:}")
    private String storageBucket;

    @Value("${FIREBASE_MESSAGING_SENDER_ID:}")
    private String messagingSenderId;

    @Value("${FIREBASE_APP_ID:}")
    private String appId;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("apiKey",             apiKey);
        config.put("authDomain",         authDomain);
        config.put("projectId",          projectId);
        config.put("storageBucket",      storageBucket);
        config.put("messagingSenderId",  messagingSenderId);
        config.put("appId",              appId);
        return ResponseEntity.ok(config);
    }
}