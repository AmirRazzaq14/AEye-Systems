package edu.farmingdale.CSC490.Food.parser;

import com.fasterxml.jackson.databind.JsonNode;
import edu.farmingdale.CSC490.Food.exception.parserException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import static edu.farmingdale.CSC490.Food.exception.parserException.*;

/**
 * Parser for Gemini responses.
 */

@Slf4j
@Component
public class GeminiResponseParser implements ResponseParser {

    @Override
    public String extract(JsonNode rootNode) throws parserException{

        log.info("Parsing Gemini Response");

        if (!hasValidCandidates(rootNode)) {
            throw new parserException(INVALID_GEMINI_RESPONSE, "Invalid Gemini response", rootNode.toString());
        }

        JsonNode firstCandidate = rootNode.get("candidates").get(0);
        JsonNode contentNode = firstCandidate.get("content");
        JsonNode partsArray = contentNode.get("parts");
        JsonNode firstPart = partsArray.get(0);
        String text = firstPart.get("text").asText();

        log.info("Parsing Gemini Response Successfully");
        return text;

    }
    
    private boolean hasValidCandidates(JsonNode rootNode) {
        return rootNode.has("candidates") && 
               rootNode.get("candidates").isArray() && 
               !rootNode.get("candidates").isEmpty();
    }


}