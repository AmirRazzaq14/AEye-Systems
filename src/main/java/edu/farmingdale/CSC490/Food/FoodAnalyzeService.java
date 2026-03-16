package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Food.client.ApiClient;
import edu.farmingdale.CSC490.Food.client.ApiClientFactory;
import edu.farmingdale.CSC490.Food.config.ConfigLoader;
import edu.farmingdale.CSC490.Food.exception.ApiException;
import edu.farmingdale.CSC490.Food.exception.ImageAnalysisException;
import edu.farmingdale.CSC490.Food.parser.FoodResultParser;
import edu.farmingdale.CSC490.Food.util.FileUtils;
import edu.farmingdale.CSC490.Food.util.ImageEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Component
public class FoodAnalyzeService {

    private final ImageEncoder imageEncoder;
    private final FileUtils fileUtils;
    private final ApiClientFactory apiClientFactory;
    private final FoodResultParser resultParser;


    @Autowired
    private ConfigLoader configLoader;

    // Accumulated nutritional information
    private Nutrition_log.Meal totalNutrition = new Nutrition_log.Meal();

    public FoodAnalyzeService(ImageEncoder imageEncoder,
                              FileUtils fileUtils,
                              ApiClientFactory apiClientFactory,
                              FoodResultParser resultParser) {
        this.imageEncoder = imageEncoder;
        this.fileUtils = fileUtils;
        this.apiClientFactory = apiClientFactory;
        this.resultParser = resultParser;
    }


    public Optional<Nutrition_log.Meal> analyze(String imagePath, String promptPath) {
        log.info("Starting analysis for image: {}", imagePath);

        try {
            // 1. Encoded images
            String encodedImage = imageEncoder.encodeImage(imagePath)
                    .orElseThrow(() -> new ImageAnalysisException("Failed to encode image: " + imagePath));

            // 2. Read the prompt
            String prompt = fileUtils.readTextFile(promptPath)
                    .orElseThrow(() -> new ImageAnalysisException("Failed to read prompt: " + promptPath));

            // 3. Get the API client
            ApiClient client = apiClientFactory.getClient();

            // 4. Call the API
            Optional<String> apiResponse = client.analyze(encodedImage, prompt);

            if (apiResponse.isEmpty()) {
                log.error("API returned empty response");
                return Optional.empty();
            }

            // 5. Interpret the results
            return resultParser.parse(apiResponse.get());

        } catch (ImageAnalysisException e) {
            log.error("Analysis failed: {}", e.getMessage());
            return Optional.empty();
        } catch (ApiException e) {
            log.error("API call failed: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error during analysis", e);
            return Optional.empty();
        }
    }

}
