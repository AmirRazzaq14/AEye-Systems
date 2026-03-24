package edu.farmingdale.CSC490.Food.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Food.config.ApiProperties;
import edu.farmingdale.CSC490.Food.exception.promptException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Slf4j
@Component
public class FoodResultParser {

    private final ObjectMapper objectMapper;
    private final OllamaResponseParser ollamaParser;
    private final GeminiResponseParser geminiParser;
    private final ApiProperties apiProperties;
    
    public FoodResultParser(OllamaResponseParser ollamaParser, 
                           GeminiResponseParser geminiParser,
                            ApiProperties apiProperties) {
        this.objectMapper = new ObjectMapper();
        this.ollamaParser = ollamaParser;
        this.geminiParser = geminiParser;
        this.apiProperties  = apiProperties;
    }
    
    public Nutrition_log.Meal parse(String jsonResponse) {

        log.info("===Parsing JSON response===");
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            log.warn("Empty JSON response received");
            throw new promptException(10400,"Empty JSON response", "");
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            String server = apiProperties.getServer();

            if(server.equals("ollama")){
                log.info("Parse to Ollama Form");
                return Optional.ofNullable(ollamaParser.extract(rootNode))
                        .map(this::parseFoodResult)
                        .orElseThrow(() -> new promptException(10401, "Failed to parse JSON response from Ollama Form", ""));
            }else if (server.equals("gemini")){
                log.info("Parse to Gemini Form");
                return Optional.ofNullable(geminiParser.extract(rootNode))
                        .map(this::parseFoodResult)
                        .orElseThrow(() -> new promptException(10404, "Failed to parse JSON response from Gemini Form", ""));
            }

            throw new promptException(10405, "Invalid server type: " + server,"");
            
        } catch (Exception e) {
            log.error("Failed to parse JSON response: {}", e.getMessage());
            throw new promptException(10402, "Failed to parse JSON response", e.getMessage());
        }
    }
    
    private Nutrition_log.Meal parseFoodResult(String json) {
        log.info("===Parsing Food Result===");
        try {
            return objectMapper.readValue(json, Nutrition_log.Meal.class);
        }catch (Exception e){
            log.error("Failed to parse food result: {}", e.getMessage());
            throw new promptException(10403, "Failed to parse food result", e.getMessage());
        }

    }
}