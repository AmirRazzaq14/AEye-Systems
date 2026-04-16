package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Food.AOP.Retryable;
import edu.farmingdale.CSC490.Food.client.GeminiNutritionSuggestionClient;
import edu.farmingdale.CSC490.Food.config.ConfigLoader;
import edu.farmingdale.CSC490.Food.exception.AISuggestionException;
import edu.farmingdale.CSC490.Food.exception.promptException;
import edu.farmingdale.CSC490.Food.parser.ResultParser;
import edu.farmingdale.CSC490.Food.prompt.PromptManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
@Slf4j
public class AISuggestionService {

    @Autowired
    private PromptManager promptManager;

    @Autowired
    private GeminiNutritionSuggestionClient client;

    @Autowired
    private ConfigLoader configLoader;

    @Autowired
    private final ResultParser resultParser;

    public AISuggestionService(
            PromptManager promptManager,
            GeminiNutritionSuggestionClient client,
            ConfigLoader configLoader, ResultParser resultParser) {

        this.promptManager = promptManager;
        this.client = client;
        this.configLoader = configLoader;
        this.resultParser = resultParser;
    }

    public String generateDailySuggestion(Nutrition_log dailyLogs) {

        log.info("Generate Daily Suggestion");

        if (dailyLogs == null) {
            log.warn("No logs provided.");
            throw new AISuggestionException(100501, "No logs provided", null);
        }

        if(dailyLogs.getMeals().isEmpty()){
            log.warn("No meals provided.");
            throw new AISuggestionException(100502, "No meals provided", null);
        }

        //2. read prompt
        String prompt = promptManager.getPromptFromFile("nutrition_suggestion_prompt");

        //3.  Loading config
        configLoader.validateConfig();

        //4.  Calling API
        String userdata = dailyLogs.toString();
        String apiResponse = client.analyze(userdata, prompt);

        //5.  Parsing API response
        String result = resultParser.suggestionParse(apiResponse);

        log.info("Returning Suggestion Successfully");
        return result;

    }

}
