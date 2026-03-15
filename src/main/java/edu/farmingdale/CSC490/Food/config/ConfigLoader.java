package edu.farmingdale.CSC490.Food.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ConfigLoader {


    @Autowired
    private ApiProperties apiProperties;

    @PostConstruct
    public void validateConfig() {
        validateApiConfiguration();
        logConfiguration();
    }

    private void validateApiConfiguration() {
        if (apiProperties.getServer() == null) {
            throw new IllegalStateException("api.model must be configured");
        }

        if ("gemini".equals(apiProperties.getServer())) {
            validateGeminiConfig();
        } else {
            validateOllamaConfig();
        }
    }

    private void validateGeminiConfig() {
        if (apiProperties.getGemini().getKey() == null) {
            throw new IllegalStateException("Gemini API key not configured");
        }
        if (apiProperties.getGemini().getUrl() == null) {
            throw new IllegalStateException("Gemini URL not configured");
        }
    }

    private void validateOllamaConfig() {
        if (apiProperties.getOllama().getUrl() == null) {
            throw new IllegalStateException("Ollama URL not configured");
        }
    }

    private void logConfiguration() {
        log.info("API Configuration - Mode: {}", apiProperties.getServer());
        log.debug("Detailed configuration loaded successfully");
    }

}
