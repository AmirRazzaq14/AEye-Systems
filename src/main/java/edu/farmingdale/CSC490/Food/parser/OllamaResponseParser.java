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
        log.info("===Parsing Ollama response===");

        log.info("1.  Checking if root node contains 'response' field");
        if (!rootNode.has("response")) {
            log.error("Root node does not contain 'response' field");
            throw new parserException(10420, "Root node does not contain 'response' field", rootNode.toString());
        }

        log.info("2.  Extracting 'response' field from root node");
        String response = rootNode.get("response").asText();
        if (response.trim().isEmpty()) {
            log.error("Extracted response is empty");
            throw new parserException(10421, "Extracted response is empty", response);
        }

        log.info("3.  returning the response");
        return response;
    }
    

}