package edu.farmingdale.CSC490.Food.prompt;

import edu.farmingdale.CSC490.Food.exception.promptException;
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
public class PromptManager {


    /**
     * get the prompt from an existing file
     * @param promptFile the prompt file name
     * @return the formatted prompt text
     */
    public String getPromptFromFile(String promptFile) {
        log.info("Getting prompt from file");
        //1.  Read the prompt file
        String promptText = readTextFile(promptFile);

        //2.  Return the formatted prompt
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
            throw new promptException(10200,  "Received empty or null prompt text", "");
        }

        return promptText.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    /**
     * Reads the text file
     * @param promptFile The file path
     * @return The text file content
     */
    private String readTextFile(String promptFile) {
        String dir = System.getProperty("user.dir") + "/src/main/java/edu/farmingdale/CSC490/Food/prompt";
        Path promptFilePath = Path.of(dir, promptFile);
        String filePath = promptFilePath.toString();

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("Text File does not exist: {}", filePath);
                throw new promptException(10201,  "Text File does not exist", "");
            }
            return Files.readString(path).trim();
        } catch (IOException e) {
            log.error("Failed to read text file {}: {}", filePath, e.getMessage());
            throw new promptException(10202,  "Failed to read text file", "");
        }
    }
}
