// client/ApiClientFactory.java
package edu.farmingdale.CSC490.Food.client;

import edu.farmingdale.CSC490.Food.config.ApiProperties;
import edu.farmingdale.CSC490.Food.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApiClientFactory {
    
    private final OllamaClient ollamaClient;
    private final GeminiClient geminiClient;
    private final ApiProperties apiProperties;
    
    public ApiClientFactory(OllamaClient localClient,
                           GeminiClient remoteClient,
                           ApiProperties apiProperties) {
        this.ollamaClient = localClient;
        this.geminiClient = remoteClient;
        this.apiProperties = apiProperties;
    }
    
    public ApiClient getClient() throws ApiException {
        String server = apiProperties.getServer();

        log.info("Using API Server: {}", server);
        if ("gemini".equals(server)) {
            return geminiClient;
        } else if ("ollama".equals(server)) {
            return ollamaClient;
        } else {
            log.error("Unknown model type: {}", server);
            throw new ApiException("Unknown model type: " + server);
        }
    }
}