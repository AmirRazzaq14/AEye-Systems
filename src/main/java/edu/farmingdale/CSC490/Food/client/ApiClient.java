package edu.farmingdale.CSC490.Food.client;


import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public interface ApiClient {
    /**
     * Analyzes the given image using the specified prompt.
     * @param encodedImage The 64bit encoded image to analyze.
     * @param promptText The prompt context to use for analysis.
     * @return The analysis result, or an empty optional if there was an error.
     * @throws ApiException If there was an error calling the API.
     */
    Optional<String> analyze(String encodedImage, String promptText) throws ApiException;

    String buildRequestBody(String image, String prompt);
    HttpRequest createHttpRequest(String endpoint, String requestBody);
    Optional<String> handleResponse(HttpResponse<String> response);
}