package edu.farmingdale.CSC490.Food.parser;

import com.fasterxml.jackson.databind.JsonNode;
import edu.farmingdale.CSC490.Food.exception.parserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static edu.farmingdale.CSC490.Food.exception.parserException.INVALID_OLLAMA_RESPONSE;

/**
 * parser for Ollama response
 */
@Slf4j
@Component
public class OllamaResponseParser implements ResponseParser {
    
    @Override
    public String extract(JsonNode rootNode) {
        try {
            log.info("Parsing Ollama response");
            String response = rootNode.get("response").asText();
            log.info("Parsing Ollama Response Success");
            return response;
        } catch (Exception e) {
            log.error("Non-Ollama response type: {}", e.getMessage());
            throw new parserException(INVALID_OLLAMA_RESPONSE, "Non-Ollama response type", rootNode.toString());
        }
    }
    

}