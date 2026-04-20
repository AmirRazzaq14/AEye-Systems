package edu.farmingdale.CSC490.Coach;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class GeminiCoachService {

    private static final String MODEL_ID = "gemini-3.1-flash-lite-preview";
    private static final String COACH_SYSTEM_PROMPT =
            "You are a friendly, knowledgeable AI fitness and nutrition coach. "
                    + "Give clear, practical advice on workout form, exercise technique, nutrition, and healthy habits. "
                    + "When the user shares an image, look at it carefully and respond to their question; comment on form, equipment, exercises, meals, food, supplements, or other fitness and nutrition details when relevant. "
                    + "Keep responses concise and encouraging. "
                    + "If the user asks something outside fitness or nutrition, politely steer the conversation back or give a brief answer and offer to help with fitness or nutrition.";

    private static final String IMAGE_ONLY_DEFAULT_PROMPT =
            "Please analyze this image and share any fitness or nutrition-related observations. If you are not sure, describe what you see and ask how you can help.";

    @Value("${gemini.api.key:}")
    private String apiKeyProperty;

    @Autowired(required = false)
    private Environment environment;

    private Client client;

    @PostConstruct
    public void init() {
        tryBuildClient();
    }

    /**
     * Resolves the same key as {@code api.gemini.*} / food pipeline: env vars, then Spring properties.
     */
    private String resolveApiKey() {
        String key = firstNonBlank(
                apiKeyProperty,
                System.getenv("GEMINI_API_KEY"),
                System.getenv("GOOGLE_API_KEY"),
                System.getenv("GOOGLE_AI_API_KEY"),
                System.getenv("API_GEMINI_KEY"));
        if (!isBlank(key)) {
            return key.trim();
        }
        if (environment != null) {
            key = firstNonBlank(
                    environment.getProperty("gemini.api.key"),
                    environment.getProperty("api.gemini.key"));
            if (!isBlank(key)) {
                return key.trim();
            }
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (!isBlank(v)) {
                return v;
            }
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private synchronized void tryBuildClient() {
        if (client != null) {
            return;
        }
        String key = resolveApiKey();
        if (!isBlank(key)) {
            this.client = Client.builder().apiKey(key).build();
            System.out.println("Gemini client initialized successfully.");
        } else {
            System.err.println(
                    "Gemini API key not set. Set Railway variable GEMINI_API_KEY (or GOOGLE_API_KEY), "
                            + "or gemini.api.key / api.gemini.key in configuration.");
        }
    }

    /**
     * Sends the user message, optional history, and optional image to Gemini.
     */
    public String chat(String userMessage, List<CoachChatRequest.ChatTurn> history, MultipartFile image) throws IOException {
        tryBuildClient();
        if (client == null) {
            throw new IllegalStateException(
                    "Gemini API key not set. In Railway → Variables add GEMINI_API_KEY with your Google AI / Gemini key "
                            + "(same key as in Google AI Studio). Redeploy after saving.");
        }

        String text = userMessage != null ? userMessage.trim() : "";
        boolean hasImage = image != null && !image.isEmpty();
        if (text.isEmpty() && !hasImage) {
            return "Please type a message or attach an image.";
        }

        List<Content> contents = buildHistoryContents(history);

        if (hasImage) {
            byte[] imageBytes = image.getBytes();
            String mimeType = resolveImageMimeType(image);
            List<Part> parts = new ArrayList<>();
            parts.add(Part.fromText(text.isEmpty() ? IMAGE_ONLY_DEFAULT_PROMPT : text));
            parts.add(Part.fromBytes(imageBytes, mimeType));
            contents.add(Content.builder()
                    .role("user")
                    .parts(parts)
                    .build());
            System.out.println("Sent image to Gemini: " + image.getOriginalFilename() + ", mime=" + mimeType + ", size=" + image.getSize());
        } else {
            contents.add(Content.builder()
                    .role("user")
                    .parts(Collections.singletonList(Part.fromText(text)))
                    .build());
        }

        Content systemInstruction = Content.builder()
                .parts(Collections.singletonList(Part.fromText(COACH_SYSTEM_PROMPT)))
                .build();

        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(systemInstruction)
                .build();

        GenerateContentResponse response = client.models.generateContent(MODEL_ID, contents, config);

        return Optional.ofNullable(response.text())
                .orElse("Sorry, I couldn't generate a response.");
    }

    private List<Content> buildHistoryContents(List<CoachChatRequest.ChatTurn> history) {
        List<Content> list = new ArrayList<>();
        if (history == null) {
            return list;
        }
        for (CoachChatRequest.ChatTurn turn : history) {
            String role = turn.getRole() != null ? turn.getRole() : "user";
            String content = turn.getContent() != null ? turn.getContent() : "";
            list.add(Content.builder()
                    .role(role)
                    .parts(Collections.singletonList(Part.fromText(content)))
                    .build());
        }
        return list;
    }

    private static String resolveImageMimeType(MultipartFile image) {
        String ct = image.getContentType();
        if (ct != null && !ct.isBlank() && !"application/octet-stream".equalsIgnoreCase(ct)) {
            return ct;
        }
        String name = image.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".png")) {
                return "image/png";
            }
            if (lower.endsWith(".webp")) {
                return "image/webp";
            }
            if (lower.endsWith(".gif")) {
                return "image/gif";
            }
            if (lower.endsWith(".bmp")) {
                return "image/bmp";
            }
            if (lower.endsWith(".heic") || lower.endsWith(".heif")) {
                return "image/heic";
            }
        }
        return "image/jpeg";
    }
}