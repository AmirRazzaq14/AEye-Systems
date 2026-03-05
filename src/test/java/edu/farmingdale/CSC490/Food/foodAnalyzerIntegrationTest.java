package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class foodAnalyzerIntegrationTest {

    @Test
    public void testPythonCallerWithRealImage() throws Exception {
        foodAnalyzer foodAnalyzer = new foodAnalyzer();

        // Try loading the test picture from the resources directory
        // python E:\Code\AEye-Systems\src\main\resources\AI\AI_ImageAnalyzer.py E:\Code\AEye-Systems\src\main\resources\images\apple.jpg E:\Code\AEye-Systems\src\main\resources\AI\food_analyze_prompt.txt
        ClassPathResource imgResource = new ClassPathResource("images/apple.jpg");

        // Read the content of the image
        InputStream imageStream = imgResource.getInputStream();
        byte[] imageBytes = StreamUtils.copyToByteArray(imageStream);
        String fileName = imgResource.getFilename();


        // Call foodAnalyzer to analyze the image
        Nutrition_log result = foodAnalyzer.analyze(imageBytes, fileName);

        // Print the result to the console
        if (result != null) {
            System.out.println("\nAnalyze the results:");
            System.out.println("Log ID: " + result.getLog_id());
            System.out.println("User ID: " + result.getUser_id());
            System.out.println("Record Date: " + result.getLog_date());
            System.out.println("Meals Type: " + result.getMeal_type());
            System.out.println("Food name: " + result.getFood_name());
            System.out.println("Calories: " + result.getCalories());
            System.out.println("Protein (g): " + result.getProtein_grams());
            System.out.println("Carbohydrates (g): " + result.getCarbs_grams());
            System.out.println("Fat (g): " + result.getFat_grams());
            System.out.println("Record time: " + result.getLogged_at());
        } else {
            System.out.println("The analysis fails and the return result is empty");
        }

        assertNotNull(result, "foodAnalyzer should return a valid Nutrition_log object");
    }
}
