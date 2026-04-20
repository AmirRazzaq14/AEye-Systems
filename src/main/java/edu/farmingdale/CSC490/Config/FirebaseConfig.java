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

@Configuration
public class FirebaseConfig {

    /**
     * Credential resolution order (production-friendly):
     * <ol>
     *   <li>{@code FIREBASE_CREDENTIALS_JSON} — full service-account JSON string (set in Railway)</li>
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
                    "Firebase credentials missing. On Railway set FIREBASE_CREDENTIALS_JSON to your "
                            + "service account JSON, or GOOGLE_APPLICATION_CREDENTIALS to a key file path. "
                            + "Locally, add src/main/resources/firebase-key.json (gitignored)."
            );
        }
        try (InputStream in = resource.getInputStream()) {
            return GoogleCredentials.fromStream(in);
        }
    }
}