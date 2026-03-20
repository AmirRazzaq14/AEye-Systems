package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Food.client.ApiClient;
import edu.farmingdale.CSC490.Food.client.ApiClientFactory;
import edu.farmingdale.CSC490.Food.config.ConfigLoader;
import edu.farmingdale.CSC490.Food.client.ApiException;
import edu.farmingdale.CSC490.Food.image.ImageManager;
import edu.farmingdale.CSC490.Food.parser.FoodResultParser;
import edu.farmingdale.CSC490.Food.prompt.PromptManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@Component
public class FoodAnalyzeService {

    @Autowired
    private final ImageManager imageManager;
    @Autowired
    private final PromptManager promptManager;
    @Autowired
    private final ApiClientFactory apiClientFactory;
    @Autowired
    private final FoodResultParser resultParser;
    @Autowired
    private ConfigLoader configLoader;


    public FoodAnalyzeService( ImageManager imageManager, PromptManager promptManager,
                              ApiClientFactory apiClientFactory,
                              FoodResultParser resultParser) {
        this.imageManager = imageManager;
        this.promptManager = promptManager;
        this.apiClientFactory = apiClientFactory;
        this.resultParser = resultParser;
    }


    public Optional<Nutrition_log.Meal> analyze(MultipartFile image) {

        try {
            // 1. Encoded images
            Optional<String> encodedImage = imageManager.encodeImageFromMultipartFile(image);
            if (encodedImage.isEmpty()) {
                log.error("Failed to encode image");
                return Optional.empty();
            }

            // 2. Read the prompt
            Optional<String> promptContext = promptManager.getPromptFromFile("food_analyze_prompt");
            if (promptContext.isEmpty()) {
                log.error("Failed to read prompt");
                return Optional.empty();
            }

            // 3. load the config
            configLoader.validateConfig();

            // 4. Get the API client
            ApiClient client = apiClientFactory.getClient();

            // 5. Call the API
            Optional<String> apiResponse = client.analyze(encodedImage.get(), promptContext.get());
            if (apiResponse.isEmpty()) {
                log.error("API returned empty response");
                return Optional.empty();
            }

            // 6. Interpret the results
            return resultParser.parse(apiResponse.get());

        } catch (ApiException e) {
            log.error("API call failed: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error during analysis", e);
            return Optional.empty();
        }
    }

}
