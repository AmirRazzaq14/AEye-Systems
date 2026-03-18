package edu.farmingdale.CSC490.Food.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface ResponseParser {

    //  Parse the response and extract the desired information
    Optional<String> extract(JsonNode rootNode);

}
