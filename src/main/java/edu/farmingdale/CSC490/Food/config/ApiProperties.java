package edu.farmingdale.CSC490.Food.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "api")
@Data
public class ApiProperties {
    private String server;
    private Ollama ollama;
    private Gemini gemini;

    @Data
    public static class Ollama{
        private String url;
        private String model;
    }

    @Data
    public static class Gemini{
        private String key;
        private String url;
        private String model;
    }

}
