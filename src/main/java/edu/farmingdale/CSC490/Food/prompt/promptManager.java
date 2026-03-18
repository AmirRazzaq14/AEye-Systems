package edu.farmingdale.CSC490.Food.prompt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Manages the prompts.it is only output port for prompt to using api,
 * Returns formatted prompts for easy model calls
 */


@Slf4j
@Component
public class promptManager {


    /**
     * Port 1, get the prompt from an existing file
     * @param promptFile the prompt file name
     * @return the formatted prompt text
     */
    public String getPromptFromFile(String promptFile) {
        log.info("Getting prompt from file");
        //1.  Read the prompt file
        String dir  = "src/main/java/edu/farmingdale/CSC490/Food/prompt";
        Path promptFilePath = Path.of(dir, promptFile);
        String promptText = String.valueOf(readTextFile(promptFilePath.toString()));

        //2.  Format the prompt and return
        return formatPrompt(promptText);
    }

    /**
     * port 2, get the input from the front-end
     *
     * @param promptText the input prompt text from the front-end
     * @return the formatted prompt text
     */
    public String getPromptFromInput(String promptText) {
        log.info("Getting prompt from input");
        return formatPrompt(promptText);
    }

    /**
     * Formats the prompt text for use in the API call
     * @param promptText The prompt text
     * @return The formatted prompt text
     */
    private String formatPrompt(String promptText) {

        if (promptText == null || promptText.trim().isEmpty()) {
            log.error("Received empty or null prompt text");
            return "";
        }

        log.info("Formatting prompt text successfully");
        return promptText.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    /**
     * Reads the text file
     * @param filePath The file path
     * @return The text file content
     */
    private Optional<String> readTextFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("Text File does not exist: {}", filePath);
                return Optional.empty();
            }
            return Optional.of(Files.readString(path).trim());
        } catch (IOException e) {
            log.error("Failed to read text file {}: {}", filePath, e.getMessage());
            return Optional.empty();
        }
    }
}
