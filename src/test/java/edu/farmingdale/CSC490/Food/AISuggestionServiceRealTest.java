package edu.farmingdale.CSC490.Food;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class AISuggestionServiceRealTest {
    @Autowired
    private AISuggestionService realService;

    private Nutrition_log ValidLogs;

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


    }
    @Test
    void testGenerateDailySuggestion_Integration() {

        // Act
        String result = realService.generateDailySuggestion(ValidLogs);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        System.out.println(result);
    }
}
