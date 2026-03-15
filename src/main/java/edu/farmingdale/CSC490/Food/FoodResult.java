package edu.farmingdale.CSC490.Food;


import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class FoodResult {
    private String foodName;
    private String mealType;
    private int calories;
    private int protein_grams;
    private int carbs_grams;
    private int fat_grams;

}
