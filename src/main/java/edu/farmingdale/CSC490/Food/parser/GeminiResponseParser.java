package edu.farmingdale.CSC490.Food.parser;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import java.util.Optional;

@Slf4j
@Component
public class GeminiResponseParser implements ResponseParser {

    @Override
    public Optional<String> extract(JsonNode rootNode) {
        try {
            if (!hasValidCandidates(rootNode)) {
                return Optional.empty();
            }
            
            JsonNode firstCandidate = rootNode.get("candidates").get(0);
            JsonNode contentNode = firstCandidate.get("content");
            JsonNode partsArray = contentNode.get("parts");
            
            if (partsArray.isEmpty()) {
                return Optional.empty();
            }
            
            JsonNode firstPart = partsArray.get(0);
            if (firstPart == null || !firstPart.has("text")) {
                return Optional.empty();
            }
            
            String text = firstPart.get("text").asText();
            return text.trim().isEmpty() ? Optional.empty() : Optional.of(text);
            
        } catch (Exception e) {
            log.debug("Failed to extract from Gemini response: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    private boolean hasValidCandidates(JsonNode rootNode) {
        return rootNode.has("candidates") && 
               rootNode.get("candidates").isArray() && 
               !rootNode.get("candidates").isEmpty();
    }


}