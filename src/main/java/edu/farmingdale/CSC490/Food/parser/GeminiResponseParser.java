package edu.farmingdale.CSC490.Food.parser;

import com.fasterxml.jackson.databind.JsonNode;
import edu.farmingdale.CSC490.Food.exception.parserException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * Parser for Gemini responses.
 */

@Slf4j
@Component
public class GeminiResponseParser implements ResponseParser {

    @Override
    public String extract(JsonNode rootNode) {

        log.info("===Parsing Gemini Response===");
        try {

            log.info("1.  Checking if the response has valid candidates");
            if (!hasValidCandidates(rootNode)) {
                throw new parserException(10410, "Invalid Gemini response", rootNode.toString());
            }

            log.info("2.  Extracting the text from the first candidate");
            JsonNode firstCandidate = rootNode.get("candidates").get(0);
            JsonNode contentNode = firstCandidate.get("content");
            JsonNode partsArray = contentNode.get("parts");

            log.info("3.  Checking if the parts array is empty");
            if (partsArray.isEmpty()) {
                throw new parserException(10411, "Invalid Gemini response at parts Array", partsArray.toString());
            }

            log.info("3.  Extracting the text from the first part");
            JsonNode firstPart = partsArray.get(0);
            if (!firstPart.has("text")) {
                throw new parserException(10412, "Invalid Gemini response at first part", firstPart.toString());
            }

            log.info("4.  Validating the text");
            String text = firstPart.get("text").asText();
            if (text == null || text.trim().isEmpty()) {
                throw new parserException(100413, "Invalid Gemini response at text part", text);
            }
            log.info("5.  Returning the text");
            return text;
            
        } catch (Exception e) {
            log.error("Failed to extract from Gemini response: {}", e.getMessage());
            throw new parserException(100414, "Failed to extract from Gemini response", e.getMessage());
        }
    }
    
    private boolean hasValidCandidates(JsonNode rootNode) {
        return rootNode.has("candidates") && 
               rootNode.get("candidates").isArray() && 
               !rootNode.get("candidates").isEmpty();
    }


}