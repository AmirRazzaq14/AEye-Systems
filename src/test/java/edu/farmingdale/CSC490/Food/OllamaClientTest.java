package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Food.client.OllamaClient;
import edu.farmingdale.CSC490.Food.config.ApiProperties;
import edu.farmingdale.CSC490.Food.image.ImageManager;
import edu.farmingdale.CSC490.Food.prompt.PromptManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class OllamaClientTest {

    private OllamaClient ollamaClient;

    private ImageManager imageManager;

    private PromptManager promptManager;

    @BeforeEach
    public void setUp() {
        imageManager = new ImageManager();
        promptManager = new PromptManager();

        ApiProperties realApiProperties = new ApiProperties();
        ApiProperties.Ollama realOllamaProperties = new ApiProperties.Ollama();
        realOllamaProperties.setUrl("http://localhost:11434");
        realOllamaProperties.setModel("llava");
        realApiProperties.setOllama(realOllamaProperties);

        ollamaClient = new OllamaClient(realApiProperties);

    }


    @Test
    void testAnalyze_RealFile() throws Exception {
        // Test with real files
        String imagePath = "src/main/resources/images/Cheeseburger.jpg";
        String promptPath = "food_analyze_prompt";

        // Encode images
        Optional<String> encodedImageOpt = imageManager.encodeImageFromFile(imagePath);
        assertTrue(encodedImageOpt.isPresent(), "Image should be successfully encoded");
        String encodedImage = encodedImageOpt.get();

        // Read the file
        Optional<String> promptOpt = promptManager.getPromptFromFile(promptPath);
        assertTrue(promptOpt.isPresent(), "Prompt should be successfully read");
        String promptText = promptOpt.get();


        // Perform tests
        Optional<String> result = ollamaClient.analyze(encodedImage, promptText);

        if (result.isPresent()) {
            System.out.println("Ollama API Response: " + result.get());
        } else {
            System.out.println("Ollama API returned empty response");
        }
    }
}
