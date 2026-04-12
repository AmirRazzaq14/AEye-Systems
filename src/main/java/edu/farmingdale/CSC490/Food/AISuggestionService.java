package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Entity.User;
import edu.farmingdale.CSC490.Food.client.GeminiNutritionSuggestionClient;
import edu.farmingdale.CSC490.Food.config.ConfigLoader;
import edu.farmingdale.CSC490.Food.prompt.PromptManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AISuggestionService {

    @Autowired
    private PromptManager promptManager;

    @Autowired
    private GeminiNutritionSuggestionClient client;

    @Autowired
    private ConfigLoader configLoader;


    public String generateDailySuggestion(List<Nutrition_log> weeklyLogs, User user) {

        log.info("Generate Daily Suggestion");

        if (weeklyLogs == null || weeklyLogs.isEmpty()) {
            return "No weekly logs provided.";
        }
        if (user == null) {
            return "No user provided.";
        }

        //1. combine weekly logs and user date to a string in json format
        String userdata = combineUserDate(weeklyLogs, user);

        //2. read prompt
        String prompt = promptManager.getPromptFromFile("food_analyze_prompt");

        //3.  Loading config
        configLoader.validateConfig();

        return client.analyze(userdata, prompt);

    }

    public String combineUserDate(List<Nutrition_log> weeklyLogs, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"weeklyLogs\":[");

        for (Nutrition_log log : weeklyLogs) {
            sb.append("{");
            sb.append(log);
            sb.append("}");
        }
        sb.append("]");
        sb.append(",\"user\":");
        sb.append("{");
        sb.append(user.getWeight());
        sb.append(user.getHeight());
        sb.append(user.getAge());
        sb.append(user.getGender());
        sb.append("}");
        return sb.toString();
    }
}
