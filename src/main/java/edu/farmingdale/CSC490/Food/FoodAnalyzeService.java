package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Food.client.ApiClient;
import edu.farmingdale.CSC490.Food.client.ApiClientFactory;
import edu.farmingdale.CSC490.Food.config.ConfigLoader;
import edu.farmingdale.CSC490.Food.exception.foodAnalyzeException;
import edu.farmingdale.CSC490.Food.image.ImageManager;
import edu.farmingdale.CSC490.Food.parser.ResultParser;
import edu.farmingdale.CSC490.Food.prompt.PromptManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


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
    private final ResultParser resultParser;
    @Autowired
    private ConfigLoader configLoader;


    public FoodAnalyzeService( ImageManager imageManager, PromptManager promptManager,
                              ApiClientFactory apiClientFactory,
                              ResultParser resultParser) {
        this.imageManager = imageManager;
        this.promptManager = promptManager;
        this.apiClientFactory = apiClientFactory;
        this.resultParser = resultParser;
    }

    public Nutrition_log.Meal analyze(MultipartFile image) {
        log.info("Analyze Food");
        try {
            // 1.  Encoding image
            String encodedImage = imageManager.encodeImageFromMultipartFile(image);

            //2.  Reading prompt
            String promptContext = promptManager.getPromptFromFile("food_analyze_prompt");

            //3.  Loading config
            configLoader.validateConfig();

            //4.  Creating API client
            ApiClient client = apiClientFactory.getClient();

            //5.  Calling API
            String apiResponse = client.analyze(encodedImage, promptContext);

            //6.  Parsing API response
            Nutrition_log.Meal result = resultParser.mealParse(apiResponse);
            result.setMealId("meals_"+ System.currentTimeMillis());

            log.info("Returning food result successfully");
            return result;

        }catch (Exception e) {
            log.error("Unexpected error during analysis", e);
            throw new foodAnalyzeException(10000,"Unexpected error during analysis",e.getMessage());
        }
    }

}
