package edu.farmingdale.CSC490.Food;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.logging.log4j.util.StringBuilders.escapeJson;

@Component
public class AIImageAnalyzer {

    private static final Logger logger = Logger.getLogger(AIImageAnalyzer.class.getName());
    private final Properties config = new Properties();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String model;
    private String localUrl;
    private String localModel;
    private String apiKey;
    private String remoteUrl;
    private String remoteModel;

    @PostConstruct
    public void init() {
        loadConfig();
    }

    private void loadConfig() {
        try {
            Path configPath = Paths.get("src/main/resources/application.properties");
            if (Files.exists(configPath)) {
                try (InputStream input = Files.newInputStream(configPath)) {
                    config.load(input);

                    // Load the configuration into the system properties
                    for (String key : config.stringPropertyNames()) {
                        String value = config.getProperty(key);

                        // Handle placeholders such as ${VARIABLE:default}
                        Pattern pattern = Pattern.compile("\\$\\{([^}:]+):?([^}]*)\\}");
                        Matcher matcher = pattern.matcher(value);

                        StringBuffer sb = new StringBuffer();
                        while (matcher.find()) {
                            String varName = matcher.group(1);
                            String defaultValue = matcher.group(2) != null ? matcher.group(2) : "";
                            String envValue = System.getenv(varName);
                            if (envValue == null) {
                                envValue = System.getProperty(varName, defaultValue);
                            }
                            matcher.appendReplacement(sb, envValue);
                        }
                        matcher.appendTail(sb);

                        System.setProperty(key, sb.toString());

                    }

                    model = System.getProperty("api.model");
                    localUrl = System.getProperty("api.local.url");
                    localModel = System.getProperty("api.local.model");
                    apiKey = System.getProperty("api.remote.key");
                    remoteUrl = System.getProperty("api.remote.url");
                    remoteModel = System.getProperty("api.remote.model");
                }
            } else {
                logger.warning("Configuration file not found: " + configPath);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading configuration", e);
        }
    }

    public String analyze(String imagePath,  String promptPath) {
        logger.info("Starting analysis for image: " + imagePath + " with prompt: " + promptPath);

        // Verify that the file exists
        File imageFile = new File(imagePath);
        File promptFile = new File(promptPath);

        if (!imageFile.exists()) {
            String errorMsg = "ERROR: Image file does not exist: " + imagePath;
            logger.severe(errorMsg);
            return errorMsg;
        }

        if (!promptFile.exists()) {
            String errorMsg = "ERROR: Prompt file does not exist: " + promptPath;
            logger.severe(errorMsg);
            return errorMsg;
        }

        // Read and encode images
        String encodedImage;
        try {
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            encodedImage = Base64.getEncoder().encodeToString(imageBytes);

            if (encodedImage.isEmpty()) {
                String errorMsg = "ERROR: No image provided or empty image file";
                logger.severe(errorMsg);
                return errorMsg;
            }
        } catch (IOException e) {
            String errorMsg = "ERROR: Failed to read image file: " + e.getMessage();
            logger.severe(errorMsg);
            return errorMsg;
        }

        // Read the prompt
        String basePrompt;
        try {
            basePrompt = Files.readString(Paths.get(promptPath)).trim();

            if (basePrompt.isEmpty()) {
                String errorMsg = "ERROR: Prompt file is empty";
                logger.severe(errorMsg);
                return errorMsg;
            }
        } catch (IOException e) {
            String errorMsg = "ERROR: Failed to read prompt file: " + e.getMessage();
            logger.severe(errorMsg);
            return errorMsg;
        }


        // Determine whether to use a remote API based on environment variables


        logger.info("Using model: " + model);

        if ("remote".equals(model)) {
            logger.info("Using remote API");
            return analyzeWithRemoteApi(encodedImage, basePrompt);
        } else {
            logger.info("Using local Ollama API");
            return analyzeWithLocalApi(encodedImage, basePrompt);
        }
    }

    private String analyzeWithLocalApi(String image, String prompt) {


        logger.info("Using local Ollama API with URL: " + localUrl + " and localModel: " + localModel);

        if (localUrl == null || localUrl.equals("None") || localUrl.trim().isEmpty()) {
            String errorMsg = "LOCAL_API_URL is not set properly.";
            logger.severe(errorMsg);
            return errorMsg;
        }
        try {
            // Build the request body
            String requestBody = String.format(
                    """
                    {
                      "model": "%s",
                      "prompt": "%s",
                      "images": ["%s"],
                      "stream": false,
                      "options": {
                        "temperature": 0.0,
                        "num_predict": 200
                      }
                    }""",
                    localModel, escapeJson(prompt), image
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(localUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(java.time.Duration.ofSeconds(60))
                    .build();


            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                String errorMsg = "Ollama API request failed: " + response.statusCode();
                logger.severe(errorMsg);
                return "{\"error\": true, \"message\": \"" + errorMsg + "\", \"statusCode\":" + response.statusCode() + "}";
            }

            return response.body();

        } catch (IOException | InterruptedException e) {
            String errorMsg = "Error during analysis: " + e.getMessage();
            logger.severe(errorMsg);
            return errorMsg;
        }
    }

    private String analyzeWithRemoteApi(String image, String prompt) {


        logger.info("Using remote API with base URL: " + remoteUrl + " and model: " + remoteModel);

        try {
            // Google Gemini API
            String apiEndpoint = String.format("%s/v1beta/models/%s:generateContent?key=%s",
                    remoteUrl, remoteModel, apiKey);

            String requestBody = String.format(
                    """
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
                    }""",
                    escapeJson(prompt), image
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiEndpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(java.time.Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                String errorMsg = "Remote API request failed: " + response.statusCode();
                logger.severe(errorMsg);
                logger.severe("Response body: " + response.body());
                return "{\"error\": true, \"message\": \"" + errorMsg + "\", \"statusCode\":" + response.statusCode() + "}";
            }

            return  response.body();

        } catch (IOException | InterruptedException e) {
            String errorMsg = "Error during analysis: " + e.getMessage();
            logger.severe(errorMsg);
            return errorMsg;
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}