package edu.farmingdale.CSC490.Food.config;

import edu.farmingdale.CSC490.Food.exception.ApiException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Loads and validates the API Properties from Configuration.
 */

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

        String server = apiProperties.getServer();
        if (server == null) {
            throw new ApiException(10301, "API server not configured", "");
        }

        if ("gemini".equals(server)) {
            validateGeminiConfig();
        }else if ("ollama".equals(server)){
            validateOllamaConfig();
        }else {
            throw new ApiException(10302, "Invalid API server: " + server, "");
        }
    }

    private void validateGeminiConfig() {
        if (apiProperties.getGemini().getKey() == null) {
            throw new ApiException(10303,"Gemini API key not configured",  "");
        }
        if (apiProperties.getGemini().getUrl() == null) {
            throw new ApiException(10304,"Gemini URL not configured",  "");
        }
    }

    private void validateOllamaConfig() {
        if (apiProperties.getOllama().getUrl() == null) {
            throw new ApiException(10305,"Ollama URL not configured","");
        }
    }

    private void logConfiguration() {
        log.info("Detailed configuration loaded successfully");
        log.debug("API Configuration - Mode: {}", apiProperties.getServer());
    }

}
