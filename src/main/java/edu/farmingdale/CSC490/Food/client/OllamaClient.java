package edu.farmingdale.CSC490.Food.client;

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
    public String analyze(String encodedImage, String promptText) throws ApiException {
        String url = apiProperties.getOllama().getUrl();
        
        log.info("Calling Ollama API for image analyser");
        
        try {
            //1.  Build the request body
            String requestBody = buildRequestBody(encodedImage, promptText);

            //2.  Create the HTTP request
            HttpRequest request = createHttpRequest(url, requestBody);

            //3.  Send the request and get the response
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            //4.  Handle the response and Return the result
            return handleResponse(response);
            
        } catch (Exception e) {
            log.error("Error calling Ollama API", e);
            throw new ApiException(10320,"Failed to call Ollama API", e.getMessage());
        }
    }

    private String buildRequestBody(String image, String prompt) {
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

    private HttpRequest createHttpRequest(String baseUrl, String requestBody) {
        log.info("Creating HTTP request with URL: {}", baseUrl);
        return HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(60))
            .build();
    }

    private String handleResponse(HttpResponse<String> response) {
        String responseBody = response.body();
        int responseStatusCode = response.statusCode();
        if (responseStatusCode != 200) {
            String  errorMessage = String.format("Ollama API returned error status: %d, response body: %s", responseStatusCode, responseBody);
            log.error(errorMessage);
            throw new ApiException(10321,"Ollama API returned error", errorMessage);
        }

        if (responseBody.isEmpty()) {
            log.error("Ollama API returned empty response");
            throw new ApiException(10322,"Ollama API returned empty response", "");
        }
        return responseBody;
    }

}