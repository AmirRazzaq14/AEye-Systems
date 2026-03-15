package edu.farmingdale.CSC490.Food.parser;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class OllamaResponseParser implements ResponseParser {
    
    @Override
    public Optional<String> extract(JsonNode rootNode) {
        if (rootNode.has("response")) {
            String nestedJson = rootNode.get("response").asText();
            if (!nestedJson.trim().isEmpty()) {
                return Optional.of(nestedJson);
            }
        }
        return Optional.empty();
    }
    

}