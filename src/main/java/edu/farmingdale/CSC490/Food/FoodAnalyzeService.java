package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Food.client.ApiClient;
import edu.farmingdale.CSC490.Food.client.ApiClientFactory;
import edu.farmingdale.CSC490.Food.config.ConfigLoader;
import edu.farmingdale.CSC490.Food.exception.foodAnalyzeException;
import edu.farmingdale.CSC490.Food.image.ImageManager;
import edu.farmingdale.CSC490.Food.parser.FoodResultParser;
import edu.farmingdale.CSC490.Food.prompt.PromptManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


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


    public Nutrition_log.Meal analyze(MultipartFile image) {
        log.info("===Analyze Food===");
        try {
            log.info("1.  Encoding image");
            String encodedImage = imageManager.encodeImageFromMultipartFile(image);

            log.info("2.  Reading prompt");
            String promptContext = promptManager.getPromptFromFile("food_analyze_prompt");

            log.info("3.  Loading config");
            configLoader.validateConfig();

            log.info("4.  Creating API client");
            ApiClient client = apiClientFactory.getClient();

            log.info("5.  Calling API");
            String apiResponse = client.analyze(encodedImage, promptContext);

            log.info("6.  Parsing API response");
            Nutrition_log.Meal result = resultParser.parse(apiResponse);
            result.setMealId("meals_"+ System.currentTimeMillis());

            log.info("7.  Returning result");
            return result;

        }catch (Exception e) {
            log.error("Unexpected error during analysis", e);
            throw new foodAnalyzeException(10000,"Unexpected error during analysis",e.getMessage());
        }
    }

}
