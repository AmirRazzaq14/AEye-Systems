package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Food.util.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilsTest {

    private JsonUtils jsonUtils;

    @BeforeEach
    public void setUp() {
        jsonUtils = new JsonUtils();
    }

    @Test
    public void testEscapeJson_WithSpecialCharacters() {
        String input = "You are a JSON-only response bot. You MUST respond with EXACTLY this JSON structure:\n{\n    foodName: string,\n    mealType: string,\n    calories: 0,\n    protein_grams: 0,\n    carbs_grams: 0,\n    fat_grams: 0\n }\nSTRICT RULES:\n1. Use ONLY the 6 fields shown above - NO additional fields\n2. NO nested objects or arrays\n3. NO explanations before or after\n4. NO markdown formatting\n5. The response must start with { and end with }\n6. Use double quotes for strings\n7. Use integers for numbers\t";
        String result = jsonUtils.escapeJson(input);

        assertFalse(result.contains("\n"));
        assertTrue(result.contains("\\n"));
        assertTrue(result.contains("\\t"));
    }

    @Test
    public void testEscapeJson_WithQuotes() {
        String input = "He said \"Hello World\"";
        String result = jsonUtils.escapeJson(input);
        assertEquals("He said \\\"Hello World\\\"", result);
    }

    @Test
    public void testEscapeJson_WithBackslashes() {
        String input = "Path: C:\\Program Files\\App";
        String result = jsonUtils.escapeJson(input);
        assertEquals("Path: C:\\\\Program Files\\\\App", result);
    }

    @Test
    public void testEscapeJson_WithCarriageReturnAndTab() {
        String input = "Line 1\rLine 2\tTabbed";
        String result = jsonUtils.escapeJson(input);
        assertEquals("Line 1\\rLine 2\\tTabbed", result);
    }

    @Test
    public void testEscapeJson_EmptyString() {
        String input = "";
        String result = jsonUtils.escapeJson(input);
        assertEquals("", result);
    }

    @Test
    public void testEscapeJson_NullInput() {
        String input = null;
        String result = jsonUtils.escapeJson(input);
        assertEquals("", result);
    }

    @Test
    public void testEscapeJson_AlreadyEscaped() {
        String input = "Text with \\\"escaped quotes\\\"";
        String result = jsonUtils.escapeJson(input);
        assertEquals("Text with \\\\\\\"escaped quotes\\\\\\\"", result);
    }



    @Test
    public void testEscapeJson_PromptText() {
        String input = "Analyze the food in this image and return only JSON with foodName, calories, protein_grams, carbs_grams, and fat_grams";
        String result = jsonUtils.escapeJson(input);
        assertEquals(input, result);
    }
}
