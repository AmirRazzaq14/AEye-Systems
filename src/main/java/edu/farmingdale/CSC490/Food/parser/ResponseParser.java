package edu.farmingdale.CSC490.Food.parser;

import com.fasterxml.jackson.databind.JsonNode;


public interface ResponseParser {

    //  Parse the response and extract the desired information
    String extract(JsonNode rootNode);

}
