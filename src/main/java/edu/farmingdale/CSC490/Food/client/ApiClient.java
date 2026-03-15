package edu.farmingdale.CSC490.Food.client;


import edu.farmingdale.CSC490.Food.exception.ApiException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public interface ApiClient {
    Optional<String> analyze(String encodedImage, String prompt) throws ApiException;

    String buildRequestBody(String image, String prompt);
    HttpRequest createHttpRequest(String endpoint, String requestBody);
    Optional<String> handleResponse(HttpResponse<String> response);
}