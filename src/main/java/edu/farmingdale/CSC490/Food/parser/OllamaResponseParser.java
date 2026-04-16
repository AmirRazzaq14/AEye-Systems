package edu.farmingdale.CSC490.Food.parser;

import com.fasterxml.jackson.databind.JsonNode;
import edu.farmingdale.CSC490.Food.exception.parserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * parser for Ollama response
 */
@Slf4j
@Component
public class OllamaResponseParser implements ResponseParser {
    
    @Override
    public String extract(JsonNode rootNode) {
        log.info("Parsing Ollama response");
        String response = rootNode.get("response").asText();
        log.info("Parsing Ollama Response Success");
        return response;
    }
    

}