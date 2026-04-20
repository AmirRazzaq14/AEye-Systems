package edu.farmingdale.CSC490.Food.client;

import edu.farmingdale.CSC490.Food.AOP.Retryable;
import edu.farmingdale.CSC490.Food.config.ApiProperties;
import edu.farmingdale.CSC490.Food.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Implementation of the ApiClient interface for the Gemini API.
 */

@Slf4j
@Component
public class GeminiImageAnalyzeClient implements ApiClient {
    
    private final HttpClient httpClient;
    private final ApiProperties apiProperties;
    
    public GeminiImageAnalyzeClient(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Override
    public String analyze(String encodedImage, String promptText) throws ApiException {
        String url = apiProperties.getGemini().getUrl();
        String model = apiProperties.getGemini().getModel();
        String apiKey = apiProperties.getGemini().getKey();
        
        log.info("Calling Gemini API for food image analyze");
        
        try {

            //1.  Build the request body
            String endpoint = String.format("%s/v1beta/models/%s:generateContent?key=%s",
                url, model, apiKey);
            String requestBody = buildRequestBody(encodedImage, promptText);

            //2.  Create the HTTP request
            HttpRequest request = createHttpRequest(endpoint, requestBody);

            //3.  Send the request and get the response
            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            //4.  Handle the response and Return the result
            return handleResponse(response);
            
        } catch (Exception e) {
            log.error("Failed to call Gemini API", e);
            throw new ApiException(10310,"Failed to call Gemini API", e.getMessage());
        }
    }

    private String buildRequestBody(String image, String prompt) {
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
            }""", prompt, image);
    }

    private HttpRequest createHttpRequest(String endpoint, String requestBody) {
        return HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(60))
            .build();
    }

    private String handleResponse(HttpResponse<String> response)  throws ApiException{
        String responseBody = response.body();
        int responseStatusCode = response.statusCode();
        if (responseStatusCode != 200) {
            String errorMessage = String.format("Gemini API returned error status: %d, response body: %s", responseStatusCode, responseBody);
            log.error(errorMessage);
            throw new ApiException(10311,"Gemini API returned error", errorMessage);
        }

        if (responseBody.isEmpty()) {
            log.error("Gemini API returned empty response");
            throw new ApiException(10312,"Gemini API returned empty response", "");
        }
        return responseBody;
    }


}