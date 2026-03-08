package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Controller.FoodController;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Repository.NutritionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodControllerTest {



    @Mock
    private NutritionRepository nutritionRepository;

    @InjectMocks
    private FoodController foodController;

    private FoodResult sampleFoodResult;

    @BeforeEach
    void setUp() {
        sampleFoodResult = FoodResult.builder()
                .foodName("Apple")
                .mealType("Breakfast")
                .calories(52)
                .protein_grams(1)
                .carbs_grams(14)
                .fat_grams(1)
                .build();
    }

    @Test
    void testSaveToDatabase() throws Exception {
        // Execute the test method
        foodController.saveToDatabase(sampleFoodResult);
        //Verify that the save method is called once
        verify(nutritionRepository, times(1)).save(any(Nutrition_log.class));

    }


}
