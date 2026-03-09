package edu.farmingdale.CSC490.Coach;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class GeminiCoachService {

    private static final String MODEL_ID = "gemini-2.5-flash";
    private static final String COACH_SYSTEM_PROMPT =
            "You are a friendly, knowledgeable AI fitness and nutrition coach. "
                    + "Give clear, practical advice on workout form, exercise technique, nutrition, and healthy habits. "
                    + "Keep responses concise and encouraging. If the user asks something outside fitness or nutrition, "
                    + "politely steer the conversation back or give a brief answer and offer to help with fitness or nutrition.";

    @Value("${gemini.api.key:}")
    private String apiKeyProperty;

    private Client client;

    @PostConstruct
    public void init() {
        String key = resolveApiKey();
        if (key != null && !key.isBlank()) {
            this.client = Client.builder().apiKey(key).build();
        }
    }

    private String resolveApiKey() {
        String key = Optional.ofNullable(apiKeyProperty)
                .filter(s -> !s.isBlank())
                .orElseGet(() -> System.getenv("GEMINI_API_KEY"));
        if (key == null || key.isBlank()) {
            key = System.getenv("GOOGLE_API_KEY");
        }
        return key;
    }

    /**
     * Sends the user message and optional history to Gemini and returns the model reply.
     */
    public String chat(String userMessage, List<CoachChatRequest.ChatTurn> history) {
        if (client == null) {
            throw new IllegalStateException(
                    "Gemini API key not set. Set gemini.api.key in application.properties or GEMINI_API_KEY or GOOGLE_API_KEY environment variable.");
        }
        List<Content> contents = buildContents(userMessage, history);
        Content systemInstruction = Content.builder()
                .parts(Collections.singletonList(Part.fromText(COACH_SYSTEM_PROMPT)))
                .build();
        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(systemInstruction)
                .build();

        GenerateContentResponse response = client.models.generateContent(MODEL_ID, contents, config);
        String text = response.text();
        return text != null ? text : "";
    }

    private List<Content> buildContents(String userMessage, List<CoachChatRequest.ChatTurn> history) {
        List<Content> list = new ArrayList<>();
        if (history != null) {
            for (CoachChatRequest.ChatTurn turn : history) {
                String role = turn.getRole() != null ? turn.getRole() : "user";
                String content = turn.getContent() != null ? turn.getContent() : "";
                list.add(Content.builder()
                        .role(role)
                        .parts(Collections.singletonList(Part.fromText(content)))
                        .build());
            }
        }
        list.add(Content.builder()
                .role("user")
                .parts(Collections.singletonList(Part.fromText(userMessage)))
                .build());
        return list;
    }
}
