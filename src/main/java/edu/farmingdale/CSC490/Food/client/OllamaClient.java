package edu.farmingdale.CSC490.Food.client;

import edu.farmingdale.CSC490.Food.config.ApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Implementation of the ApiClient interface for the Ollama API.
 */

@Slf4j
@Component
public class OllamaClient implements ApiClient {
    
    private final HttpClient httpClient;
    private final ApiProperties apiProperties;
    
    public OllamaClient(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }



    /**
     * Calls the Ollama API to analyze an image and return the result.
     *
     * @param encodedImage The encoded image to analyze.
     * @param promptText The prompt context to use for analysis.
     * @return The analysis result, or an empty optional if there was an error.
     * @throws ApiException If there was an error calling the API.
     */
    @Override
    public Optional<String> analyze(String encodedImage, String promptText) throws ApiException {
        String url = apiProperties.getOllama().getUrl();
        String model = apiProperties.getOllama().getModel();
        
        log.info("Calling Ollama API at {} with model {}", url, model);
        
        try {
            String requestBody = buildRequestBody(encodedImage, promptText);
            HttpRequest request = createHttpRequest(url, requestBody);
            
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            return handleResponse(response);
            
        } catch (Exception e) {
            log.error("Error calling Ollama API", e);
            throw new ApiException("Failed to call Ollama API", e);
        }
    }

    @Override
    public String buildRequestBody(String image, String prompt) {
        String model = apiProperties.getOllama().getModel();
        return String.format("""
            {
                "model": "%s",
                "prompt": "%s",
                "images": ["%s"],
                "stream": false,
                "options": {
                    "temperature": 0.0,
                    "num_predict": 200
                }
            }""", model, prompt, image);
    }

    @Override
    public HttpRequest createHttpRequest(String baseUrl, String requestBody) {
        log.info("Creating HTTP request with URL: {}", baseUrl);
        return HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(60))
            .build();
    }

    @Override
    public Optional<String> handleResponse(HttpResponse<String> response) {
        String responseBody = response.body();
        int responseStatusCode = response.statusCode();
        if (responseStatusCode != 200) {
            log.error("Ollama API returned error status: {}, response body: {}", responseStatusCode, responseBody);
            return Optional.empty();
        }

        if (responseBody.isEmpty()) {
            log.error("Ollama API returned empty response");
            return Optional.empty();
        }
        return Optional.of(responseBody);
    }

}