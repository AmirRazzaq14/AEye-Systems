package edu.farmingdale.CSC490.Food;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class foodAnalyzerTest {

    @Mock
    private RestTemplate restTemplate;

    private foodAnalyzer foodAnalyzer;

    @BeforeEach
    void setUp() throws IllegalAccessException, NoSuchFieldException {
        foodAnalyzer = new foodAnalyzer();
        Field pythonApiUrlField = foodAnalyzer.getClass().getDeclaredField("PYTHON_API_URL");
        pythonApiUrlField.setAccessible(true);
        pythonApiUrlField.set(foodAnalyzer, "http://localhost:8000");

        Field restTemplateField = foodAnalyzer.getClass().getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(foodAnalyzer, restTemplate);
    }

    @Test
    void testAnalyzeWithFloatingPointValues()  {
        // Prepare test data - JSON response with floating-point values
        String jsonResponse = """
                {
                  "foodName": "Apple",
                  "mealType": "Snack",
                  "calories": 52.3,
                  "protein_grams": 0.3,
                  "carbs_grams": 14.2,
                  "fat_grams": 0.2
                }""";

        // Create a mocked HttpEntity response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Create an array of simulated image bytes
        byte[] imageBytes = "fake image data".getBytes();

        // Perform analysis
        FoodResult result = foodAnalyzer.analyze(imageBytes, "test.jpg", "test prompt");

        // Validate the result - the float should be rounded
        assertNotNull(result);
        assertEquals("Apple", result.getFoodName());
        assertEquals("Snack", result.getMealType());
        assertEquals(52, result.getCalories());      // 52.3 Rounding to 52
        assertEquals(0, result.getProtein_grams());   // 0.3 Rounding to 0
        assertEquals(14, result.getCarbs_grams());    // 14.2 Rounding to 14
        assertEquals(0, result.getFat_grams());       // 0.2 Rounding to 0
    }

    @Test
    void testAnalyzeWithExactHalfValues(){
        // Test the rounding of critical values such as 0.5
        String jsonResponse = """
                {
                  "foodName": "Banana",
                  "mealType": "Breakfast",
                  "calories": 89.5,
                  "protein_grams": 1.3,
                  "carbs_grams": 22.8,
                  "fat_grams": 0.5
                }""";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        byte[] imageBytes = "fake image data".getBytes();

        FoodResult result = foodAnalyzer.analyze(imageBytes, "test.jpg", "test prompt");

        assertNotNull(result);
        assertEquals("Banana", result.getFoodName());
        assertEquals(90, result.getCalories());      // 89.5 Rounding to 90
        assertEquals(1, result.getProtein_grams());   // 1.3 Rounding to 1
        assertEquals(23, result.getCarbs_grams());    // 22.8 Rounding to 23
        assertEquals(1, result.getFat_grams());       // 0.5 Rounding to 1
    }

    @Test
    void testAnalyzeWithNullImageBytes() {
        FoodResult result = foodAnalyzer.analyze(null, "test.jpg", "test prompt");
        assertNull(result);
    }

    @Test
    void testAnalyzeWithEmptyImageBytes() {
        FoodResult result = foodAnalyzer.analyze(new byte[0], "test.jpg", "test prompt");
        assertNull(result);
    }

    @Test
    void testAnalyzeWithNullPrompt() {
        FoodResult result = foodAnalyzer.analyze("fake image data".getBytes(), "test.jpg", null);
        assertNull(result);
    }

    @Test
    void testAnalyzeWithEmptyPrompt() {
        FoodResult result = foodAnalyzer.analyze("fake image data".getBytes(), "test.jpg", "");
        assertNull(result);
    }

    @Test
    void testAnalyzeWithConnectionError() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        byte[] imageBytes = "fake image data".getBytes();
        FoodResult result = foodAnalyzer.analyze(imageBytes, "test.jpg", "test prompt");

        assertNull(result);
    }

    @Test
    void testGsonConfigurationHandlesFloatingPoints() {
        // Test whether the Gson configuration correctly handles the floating-point to integer conversion
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Double.class, (JsonDeserializer<Double>) (json, typeOfT, context) -> json.getAsDouble())
                .registerTypeAdapter(double.class, (JsonDeserializer<Double>) (json, typeOfT, context) -> json.getAsDouble())
                .registerTypeAdapter(int.class, (JsonDeserializer<Integer>) (json, typeOfT, context) -> Math.round(json.getAsFloat()))
                .registerTypeAdapter(Integer.class, (JsonDeserializer<Integer>) (json, typeOfT, context) -> Math.round(json.getAsFloat()))
                .create();

        String json = """
                {
                  "foodName": "Test Food",
                  "mealType": "Lunch",
                  "calories": 123.7,
                  "protein_grams": 4.5,
                  "carbs_grams": 15.2,
                  "fat_grams": 8.9
                }""";

        FoodResult result = gson.fromJson(json, FoodResult.class);
        assertEquals(124, result.getCalories());     // 123.7 Rounded to 124
        assertEquals(5, result.getProtein_grams());  // 4.5 Rounding to 5
        assertEquals(15, result.getCarbs_grams());   // 15.2 Rounding to 15
        assertEquals(9, result.getFat_grams());      // 8.9 Rounding to 9
    }
}
