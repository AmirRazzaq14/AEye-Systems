// client/GeminiClient.java
package edu.farmingdale.CSC490.Food.client;

import edu.farmingdale.CSC490.Food.config.ApiProperties;
import edu.farmingdale.CSC490.Food.exception.ApiException;
import edu.farmingdale.CSC490.Food.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class GeminiClient implements ApiClient {
    
    private final HttpClient httpClient;
    private final ApiProperties apiProperties;
    private final JsonUtils jsonUtils;
    
    public GeminiClient(ApiProperties apiProperties, JsonUtils jsonUtils) {
        this.apiProperties = apiProperties;
        this.jsonUtils = jsonUtils;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    @Override
    public Optional<String> analyze(String encodedImage, String prompt) throws ApiException {
        String url = apiProperties.getGemini().getUrl();
        String model = apiProperties.getGemini().getModel();
        String apiKey = apiProperties.getGemini().getKey();
        
        log.info("Calling Gemini API with model {}", model);
        
        try {
            String endpoint = String.format("%s/v1beta/models/%s:generateContent?key=%s",
                url, model, apiKey);
                
            String requestBody = buildRequestBody(encodedImage, prompt);
            HttpRequest request = createHttpRequest(endpoint, requestBody);
            
            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
                
            return handleResponse(response);
            
        } catch (Exception e) {
            throw new ApiException("Failed to call Gemini API", e);
        }
    }

    @Override
    public String buildRequestBody(String image, String prompt) {
        return String.format("""
            {
                "contents": [{
                    "parts": [
                        {"text": "%s"},
                        {
                            "inline_data": {
                                "mime_type": "image/jpeg",
                                "data": "%s"
                            }
                        }
                    ]
                }],
                "generationConfig": {
                    "temperature": 0.0,
                    "topK": 1,
                    "topP": 0.1,
                    "maxOutputTokens": 200
                }
            }""", jsonUtils.escapeJson(prompt), image);
    }

    @Override
    public HttpRequest createHttpRequest(String endpoint, String requestBody) {
        return HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(60))
            .build();
    }

    @Override
    public Optional<String> handleResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            log.error("Gemini API returned error status: {}", response.statusCode());
            return Optional.empty();
        }
        return Optional.of(response.body());
    }

}