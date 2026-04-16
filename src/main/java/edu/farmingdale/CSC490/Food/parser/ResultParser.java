package edu.farmingdale.CSC490.Food.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Food.config.ApiProperties;
import edu.farmingdale.CSC490.Food.exception.parserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Optional;

import static edu.farmingdale.CSC490.Food.exception.parserException.*;

@Slf4j
@Component
public class ResultParser {

    private final ObjectMapper objectMapper;
    private final OllamaResponseParser ollamaParser;
    private final GeminiResponseParser geminiParser;
    private final ApiProperties apiProperties;
    
    public ResultParser(OllamaResponseParser ollamaParser,
                        GeminiResponseParser geminiParser,
                        ApiProperties apiProperties) {
        this.objectMapper = new ObjectMapper();
        this.ollamaParser = ollamaParser;
        this.geminiParser = geminiParser;
        this.apiProperties  = apiProperties;
    }
    
    public Nutrition_log.Meal mealParse(String jsonResponse) {

        log.info("Parsing JSON response for Meal");
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            log.warn("Empty JSON response received in meal response");
            throw new parserException(EMPTY_JSON_RESPONSE,"Empty Meal JSON response", "");
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            String server = apiProperties.getServer();

            if(server.equals("ollama")){
                log.info("Parse Meal response to Ollama Form");
                return Optional.ofNullable(ollamaParser.extract(rootNode))
                        .map(this::parseFoodResult)
                        .orElseThrow(() -> new parserException(INVALID_OLLAMA_RESPONSE, "Failed to mealParse Meal JSON response from Ollama Form", ""));
            }else if (server.equals("gemini")){
                log.info("Parse Meal response to Gemini Form");
                return Optional.ofNullable(geminiParser.extract(rootNode))
                        .map(this::parseFoodResult)
                        .orElseThrow(() -> new parserException(INVALID_GEMINI_RESPONSE, "Failed to mealParse Meal JSON response from Gemini Form", ""));
            }

            throw new parserException(INVALID_SERVER_TYPE, "Invalid server type: " + server,"");
            
        } catch (parserException e) {
            log.error("Parser Meal response Exception: {} - {}", e.getMessage(), e.getDetail());
            throw e;
        } catch (JsonProcessingException e) {
            log.error("Unknown Json Processing in Meal response parser: {}", e.getMessage());
            throw new parserException(UNKNOWN_JSON_PROCESSING_EXCEPTION, "Unknown Json Processing Exception in Meal response parser", e.getMessage());
        } catch (Exception e) {
            log.error("Unknown Exception in Meal response parser: {}", e.getMessage());
            throw new parserException(UNKNOWN_PARSER_EXCEPTION, "Unknown Exception in Meal response parser", e.getMessage());
        }
    }
    
    private Nutrition_log.Meal parseFoodResult(String json){
        log.info("Parsing Food Result");
        try {
            return objectMapper.readValue(json, Nutrition_log.Meal.class);
        }catch (JsonProcessingException e){
            log.error("Failed to mealParse food result: {}", e.getMessage());
            throw new parserException(UNKNOWN_JSON_PROCESSING_EXCEPTION, "Failed to mealParse food result", e.getMessage());
        }

    }


    public String suggestionParse(String jsonResponse) {
        log.info("Parsing JSON response for Suggestion");
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            log.warn("Empty JSON response received in Suggestion response");
            throw new parserException(EMPTY_JSON_RESPONSE,"Empty Suggestion JSON response", "");
        }

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            return Optional.ofNullable(ollamaParser.extract(rootNode))
                    .orElseThrow(() -> new parserException(INVALID_GEMINI_RESPONSE, "Failed to mealParse Suggestion JSON response", ""));
        } catch (parserException e) {
            log.error("Parser Suggestion response Exception: {} - {}", e.getMessage(), e.getDetail());
            throw e;
        } catch (JsonProcessingException e) {
            log.error("Unknown Json Processing in result parser: {}", e.getMessage());
            throw new parserException(UNKNOWN_JSON_PROCESSING_EXCEPTION, "Unknown Json Processing Exception in Suggestion response parser", e.getMessage());
        } catch (Exception e) {
            log.error("Unknown Exception in result parser: {}", e.getMessage());
            throw new parserException(UNKNOWN_PARSER_EXCEPTION, "Unknown Exception in Suggestion response parser", e.getMessage());
        }

    }

}