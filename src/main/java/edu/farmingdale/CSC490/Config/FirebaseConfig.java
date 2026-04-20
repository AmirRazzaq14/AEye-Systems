package edu.farmingdale.CSC490.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    /**
     * Credential resolution order (production-friendly):
     * <ol>
     *   <li>{@code FIREBASE_CREDENTIALS_JSON} — full service-account JSON string (Railway Variables)</li>
     *   <li>{@code FIREBASE_CREDENTIALS_BASE64} — same JSON, Base64-encoded (easier if newlines break)</li>
     *   <li>{@code GOOGLE_APPLICATION_CREDENTIALS} — path to a JSON key file</li>
     *   <li>{@code classpath:firebase-key.json} — local development only</li>
     * </ol>
     */
    @Bean
    public Firestore firestore() throws IOException {
        GoogleCredentials credentials = loadCredentials();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        return FirestoreClient.getFirestore();
    }

    private static GoogleCredentials loadCredentials() throws IOException {
        String inlineJson = System.getenv("FIREBASE_CREDENTIALS_JSON");
        if (inlineJson != null && !inlineJson.isBlank()) {
            byte[] utf8 = inlineJson.getBytes(StandardCharsets.UTF_8);
            try (InputStream in = new ByteArrayInputStream(utf8)) {
                return GoogleCredentials.fromStream(in);
            }
        }

        String inlineB64 = System.getenv("FIREBASE_CREDENTIALS_BASE64");
        if (inlineB64 != null && !inlineB64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(inlineB64.trim().replace("\n", "").replace("\r", ""));
            try (InputStream in = new ByteArrayInputStream(decoded)) {
                return GoogleCredentials.fromStream(in);
            }
        }

        String pathEnv = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (pathEnv != null && !pathEnv.isBlank()) {
            Path path = Path.of(pathEnv);
            if (Files.isRegularFile(path)) {
                try (InputStream in = new FileInputStream(path.toFile())) {
                    return GoogleCredentials.fromStream(in);
                }
            }
        }

        ClassPathResource resource = new ClassPathResource("firebase-key.json");
        if (!resource.exists()) {
            throw new IOException(
                    "Firebase credentials missing. In Railway → Variables add FIREBASE_CREDENTIALS_JSON "
                            + "(paste full service-account JSON), or FIREBASE_CREDENTIALS_BASE64 (same file "
                            + "Base64-encoded), or GOOGLE_APPLICATION_CREDENTIALS (path). Locally use "
                            + "src/main/resources/firebase-key.json (gitignored)."
            );
        }
        try (InputStream in = resource.getInputStream()) {
            return GoogleCredentials.fromStream(in);
        }
    }
}