package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Food.client.OllamaClient;
import edu.farmingdale.CSC490.Food.config.ApiProperties;
import edu.farmingdale.CSC490.Food.util.FileUtils;
import edu.farmingdale.CSC490.Food.util.ImageEncoder;
import edu.farmingdale.CSC490.Food.util.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class OllamaClientTest {

    private OllamaClient ollamaClient;

    private ImageEncoder imageEncoder;

    private FileUtils fileUtils;

    @BeforeEach
    public void setUp() {
        fileUtils = new FileUtils();
        imageEncoder = new ImageEncoder(fileUtils);

        ApiProperties realApiProperties = new ApiProperties();
        ApiProperties.Ollama realOllamaProperties = new ApiProperties.Ollama();
        realOllamaProperties.setUrl("http://localhost:11434");
        realOllamaProperties.setModel("llava");
        realApiProperties.setOllama(realOllamaProperties);
        JsonUtils realJsonUtils = new JsonUtils();

        ollamaClient = new OllamaClient(realApiProperties, realJsonUtils);

    }


    @Test
    void testAnalyze_RealFile() throws Exception {
        // Test with real files
        String imagePath = "src/main/resources/images/Cheeseburger.jpg";
        String promptPath = "src/main/java/edu/farmingdale/CSC490/Food/prompt/food_analyze_prompt";

        // Encode images
        Optional<String> encodedImageOpt = imageEncoder.encodeImage(imagePath);
        assertTrue(encodedImageOpt.isPresent(), "Image should be successfully encoded");
        String encodedImage = encodedImageOpt.get();

        // Read the file
        Optional<String> promptOpt = fileUtils.readTextFile(promptPath);
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
