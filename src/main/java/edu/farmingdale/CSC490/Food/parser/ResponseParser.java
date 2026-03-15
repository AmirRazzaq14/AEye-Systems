package edu.farmingdale.CSC490.Food.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface ResponseParser {

    Optional<String> extract(JsonNode rootNode);

}
