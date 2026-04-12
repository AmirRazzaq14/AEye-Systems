package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Entity.User;
import edu.farmingdale.CSC490.Food.config.ConfigLoader;
import edu.farmingdale.CSC490.Food.client.GeminiNutritionSuggestionClient;
import edu.farmingdale.CSC490.Food.prompt.PromptManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    private List<Nutrition_log> mockLogs;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Create mock data
        mockUser = new User();
        mockUser.setUser_id("testUser");
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("password");
        mockUser.setWeight(70.0);
        mockUser.setHeight(175.0);
        mockUser.setAge(30);
        mockUser.setGender("male");

        // Create meals data
        Nutrition_log log1 = getNutritionLog();

        Nutrition_log log2 = new Nutrition_log();
        log2.setId("log2");
        log2.setUserId("testUser");
        log2.setDate("2025-04-06");
        log2.setMeals(Collections.emptyList());  // Empty meal log
        log2.setNotes("No meals recorded");
        log2.setUpdatedAt("2025-04-06T12:00:00Z");

        mockLogs = Arrays.asList(log1, log2);
    }

    @NotNull
    private static Nutrition_log getNutritionLog() {
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

        // Create Nutrition_log and set up meals
        Nutrition_log log1 = new Nutrition_log();
        log1.setId("log1");
        log1.setUserId("testUser");
        log1.setDate("2025-04-05");
        log1.setMeals(Arrays.asList(meal1, meal2));
        log1.setNotes("A balanced day");
        log1.setUpdatedAt("2025-04-05T12:00:00Z");
        return log1;
    }

    @Test
    void testGenerateDailySuggestion_WithValidInput_ShouldCallAllMethods() throws Exception {
        // Arrange
        String expectedPrompt = "Sample prompt content";
        String expectedResponse = "AI suggestion response";

        when(promptManager.getPromptFromFile(anyString())).thenReturn(expectedPrompt);
        when(client.analyze(anyString(), anyString())).thenReturn(expectedResponse);

        // Act
        String result = aiSuggestionService.generateDailySuggestion(mockLogs, mockUser);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);

        verify(promptManager, times(1)).getPromptFromFile("nutrition_suggestion_prompt");
        verify(configLoader, times(1)).validateConfig();
        verify(client, times(1)).analyze(anyString(), anyString());
    }

    @Test
    void testGenerateDailySuggestion_WithEmptyLogs_ShouldStillCallClient() throws Exception {
        // Arrange
        String expectedPrompt = "Sample prompt content";
        String expectedResponse = "AI suggestion response";

        when(promptManager.getPromptFromFile(anyString())).thenReturn(expectedPrompt);
        when(client.analyze(anyString(), anyString())).thenReturn(expectedResponse);

        // Act
        String result = aiSuggestionService.generateDailySuggestion(Collections.emptyList(), mockUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("No weekly logs provided."));
        assertEquals(expectedResponse, result);

        verify(promptManager, times(1)).getPromptFromFile("food_analyze_prompt");
        verify(configLoader, times(1)).validateConfig();
        verify(client, times(1)).analyze(anyString(), anyString());
    }

    @Test
    void testGenerateDailySuggestion_WithNullUser_ShouldFail() throws Exception {
        // Arrange
        String expectedPrompt = "Sample prompt content";

        when(promptManager.getPromptFromFile(anyString())).thenReturn(expectedPrompt);
        when(client.analyze(anyString(), anyString())).thenReturn("AI suggestion response");

        // Act
        String result = aiSuggestionService.generateDailySuggestion(mockLogs, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("No user provided"));

        verify(promptManager, times(1)).getPromptFromFile("food_analyze_prompt");
        verify(configLoader, times(1)).validateConfig();
        verify(client, never()).analyze(anyString(), anyString());
    }

    @Test
    void testGenerateDailySuggestion_WhenPromptManagerFails_ShouldReturnError() throws Exception {
        // Arrange
        when(promptManager.getPromptFromFile(anyString())).thenThrow(new RuntimeException("Prompt load failed"));

        // Act
        String result = aiSuggestionService.generateDailySuggestion(mockLogs, mockUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Prompt load failed"));
    }
}
