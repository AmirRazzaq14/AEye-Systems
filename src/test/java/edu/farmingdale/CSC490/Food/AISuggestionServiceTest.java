package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Food.config.ApiProperties;
import edu.farmingdale.CSC490.Food.config.ConfigLoader;
import edu.farmingdale.CSC490.Food.client.GeminiNutritionSuggestionClient;
import edu.farmingdale.CSC490.Food.exception.AISuggestionException;
import edu.farmingdale.CSC490.Food.prompt.PromptManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AISuggestionServiceTest {

    @InjectMocks
    private AISuggestionService aiSuggestionService;

    @Mock
    private PromptManager promptManager;

    @Mock
    private GeminiNutritionSuggestionClient client;

    @Mock
    private ConfigLoader configLoader;


    private Nutrition_log ValidLogs;
    private Nutrition_log emptyMeatLogs;

    @BeforeEach
    void setUp() {

        Nutrition_log.Meal meal1 = new Nutrition_log.Meal();
        meal1.setMealId("meal1");
        meal1.setName("Breakfast");
        meal1.setCals("300");
        meal1.setCarb("30");
        meal1.setProtein("10");
        meal1.setFat("10");

        Nutrition_log.Meal meal2 = new Nutrition_log.Meal();
        meal2.setMealId("meal2");
        meal2.setName("Lunch");
        meal2.setCals("600");
        meal2.setCarb("50");
        meal2.setProtein("20");
        meal2.setFat("15");

        // Create valid log with set up meals
        ValidLogs = new Nutrition_log();
        ValidLogs.setId("log1");
        ValidLogs.setUserId("testUser");
        ValidLogs.setDate("2025-04-05");
        ValidLogs.setMeals(Arrays.asList(meal1, meal2));
        ValidLogs.setNotes("A balanced day");
        ValidLogs.setUpdatedAt("2025-04-05T12:00:00Z");

        // Create log with empty meals
        emptyMeatLogs = new Nutrition_log();
        emptyMeatLogs.setId("log2");
        emptyMeatLogs.setUserId("testUser");
        emptyMeatLogs.setDate("2025-04-06");
        emptyMeatLogs.setMeals(Collections.emptyList());  // Empty meal log
        emptyMeatLogs.setNotes("No meals recorded");
        emptyMeatLogs.setUpdatedAt("2025-04-06T12:00:00Z");

    }


    @Test
    void testGenerateDailySuggestion_WithValidInput() {
        // Arrange
        String expectedPrompt = "Sample prompt content";
        String expectedResponse = "AI suggestion response";

        when(promptManager.getPromptFromFile(anyString())).thenReturn(expectedPrompt);
        when(client.analyze(anyString(), anyString())).thenReturn(expectedResponse);

        // Act
        String result = aiSuggestionService.generateDailySuggestion(ValidLogs);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);

        verify(promptManager, times(1)).getPromptFromFile("nutrition_suggestion_prompt");
        verify(configLoader, times(1)).validateConfig();
        verify(client, times(1)).analyze(anyString(), anyString());
    }

    @Test
    void testGenerateDailySuggestion_WithEmptyLogs() {
        // Arrange
        String expectedResponse = "No logs provided";


        // Act
        Exception exception = assertThrows(AISuggestionException.class, () -> {
            aiSuggestionService.generateDailySuggestion(null);;
        });

        // Assert
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedResponse));

        verify(promptManager,  never()).getPromptFromFile("nutrition_suggestion_prompt");
        verify(configLoader,  never()).validateConfig();
        verify(client,  never()).analyze(anyString(), anyString());
    }

    @Test
    void testGenerateDailySuggestion_WithEmptyMeatLogs() {
        // Arrange
        String expectedResponse = "No meals provided";

        // Act
        Exception exception = assertThrows(AISuggestionException.class, () -> {
            aiSuggestionService.generateDailySuggestion(emptyMeatLogs);;
        });

        // Assert
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedResponse));

        verify(promptManager,  never()).getPromptFromFile("nutrition_suggestion_prompt");
        verify(configLoader,  never()).validateConfig();
        verify(client,  never()).analyze(anyString(), anyString());
    }



}
