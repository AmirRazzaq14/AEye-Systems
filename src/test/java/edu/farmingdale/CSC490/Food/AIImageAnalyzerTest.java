package edu.farmingdale.CSC490.Food;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;



public class AIImageAnalyzerTest {
    private AIImageAnalyzer aiImageAnalyzer;
    private String testPromptPath;
    private String testImagePath;

    @BeforeEach
    public void setUp() throws Exception {
        aiImageAnalyzer = new AIImageAnalyzer();

        testPromptPath = "E:\\Code\\AEye-Systems\\src\\main\\java\\edu\\farmingdale\\CSC490\\Food\\food_analyze_prompt";
        testImagePath = "E:\\Code\\AEye-Systems\\src\\main\\resources\\images\\Cheeseburger.jpg";

        aiImageAnalyzer.init();

    }

    @Test
    public void testAnalyzeImage() {

        String result = aiImageAnalyzer.analyze(testImagePath,  testPromptPath);

    }

}
