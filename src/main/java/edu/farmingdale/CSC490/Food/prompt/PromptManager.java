package edu.farmingdale.CSC490.Food.prompt;

import edu.farmingdale.CSC490.Food.exception.promptException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Loads prompts from the classpath (packaged in the JAR). Works in Docker/Railway — not from
 * {@code user.dir}/src/main/java/... which only exists in local IDE runs.
 */
@Slf4j
@Component
public class PromptManager {

    private static final String PROMPT_CLASSPATH_PREFIX = "edu/farmingdale/CSC490/Food/prompt/";

    /**
     * @param promptFile file name only (e.g. {@code food_analyze_prompt})
     */
    public String getPromptFromFile(String promptFile) {
        log.info("Getting prompt from classpath: {}", promptFile);
        String promptText = readTextFile(promptFile);
        return formatPrompt(promptText);
    }

    private String formatPrompt(String promptText) {

        if (promptText == null || promptText.trim().isEmpty()) {
            log.error("Received empty or null prompt text");
            throw new promptException(10200, "Received empty or null prompt text", "");
        }

        return promptText.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String readTextFile(String promptFile) {
        String classpathLocation = PROMPT_CLASSPATH_PREFIX + promptFile;
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            log.error("Prompt not found on classpath: {}", classpathLocation);
            throw new promptException(10201, "Text File does not exist", classpathLocation);
        }
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            log.error("Failed to read prompt resource {}: {}", classpathLocation, e.getMessage());
            throw new promptException(10202, "Failed to read text file", e.getMessage());
        }
    }
}
