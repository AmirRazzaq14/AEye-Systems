package edu.farmingdale.CSC490.Food.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.farmingdale.CSC490.Food.FoodResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Slf4j
@Component
public class FoodResultParser {

    private final ObjectMapper objectMapper;
    private final OllamaResponseParser ollamaParser;
    private final GeminiResponseParser geminiParser;
    
    public FoodResultParser(OllamaResponseParser ollamaParser, 
                           GeminiResponseParser geminiParser) {
        this.objectMapper = new ObjectMapper();
        this.ollamaParser = ollamaParser;
        this.geminiParser = geminiParser;
    }
    
    public Optional<FoodResult> parse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            log.warn("Empty JSON response received");
            return Optional.empty();
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            log.info("Parsing response to extracted format {}", jsonResponse);
            
            // Try the Ollama format
            Optional<String> extractedJson = ollamaParser.extract(rootNode);
            if (extractedJson.isPresent()) {
                return parseFoodResult(extractedJson.get());
            }
            
            // Try the Gemini format
            extractedJson = geminiParser.extract(rootNode);
            if (extractedJson.isPresent()) {
                return parseFoodResult(extractedJson.get());
            }
            
            log.warn("Could not extract FoodResult from response");
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Failed to parse JSON response: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    private Optional<FoodResult> parseFoodResult(String json) {
        try {
            FoodResult result = objectMapper.readValue(json, FoodResult.class);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            log.error("Failed to parse FoodResult from JSON: {}", e.getMessage());
            return Optional.empty();
        }
    }
}