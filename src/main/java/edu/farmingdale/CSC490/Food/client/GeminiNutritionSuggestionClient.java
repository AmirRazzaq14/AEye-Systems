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
 * Gemini API implementation for nutrition suggestion.
 */

@Slf4j
@Component
public class GeminiNutritionSuggestionClient implements ApiClient {

    private final HttpClient httpClient;
    private final ApiProperties apiProperties;

    public GeminiNutritionSuggestionClient(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    @Retryable()
    @Override
    public String analyze(String userDate, String promptText){
        String url = apiProperties.getGemini().getUrl();
        String model = apiProperties.getGemini().getModel();
        String apiKey = apiProperties.getGemini().getKey();

        log.info("Calling Gemini API for Nutrition suggestion");

        try {

            //1.  Build the request body
            String endpoint = String.format("%s/v1beta/models/%s:generateContent?key=%s",
                    url, model, apiKey);
            String requestBody = buildRequestBody(userDate, promptText);

            //2.  Create the HTTP request
            HttpRequest request = createHttpRequest(endpoint, requestBody);

            //3.  Send the request and get the response
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            //4.  Handle the response and Return the result
            return handleResponse(response);

        } catch (ApiException e){
            log.error("Failed to call Gemini API", e);
            throw new ApiException(10310, e.getMessage(),  e.getDetail());
        } catch (Exception e) {
            log.error("Unknown Exception in Gemini API calling",e);
            throw new RuntimeException("Unknown Exception in Gemini API calling",e);
        }
    }

    private String buildRequestBody(String userDate, String prompt) {
        return String.format("""
            {
                "contents": [{
                    "parts": [
                        {"text": "%s \nuserDietaryData: %s"},
                    ]
                }],
                "generationConfig": {
                    "temperature": 0.0,
                    "topK": 1,
                    "topP": 0.1,
                    "maxOutputTokens": 200
                }
            }""", prompt, userDate);
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

        if (responseBody.isEmpty()) {
            log.error("Gemini API returned empty response");
            throw new ApiException(10311,"Gemini API returned empty response", "");
        }

        if (response.statusCode() == 503) {
            String errorMessage = String.format("Gemini API temporarily unavailable, status: %d, response body: %s",
                    responseStatusCode, responseBody);
            log.error(errorMessage);
            throw new ApiException(10312, "Gemini API is temporarily unavailable", errorMessage);
        }

        if (responseStatusCode != 200) {
            String errorMessage = String.format("Gemini API returned error, status: %d, response body: %s", responseStatusCode, responseBody);
            log.error(errorMessage);
            throw new ApiException(10313,"Gemini API returned error", errorMessage);
        }


        return responseBody;
    }


}