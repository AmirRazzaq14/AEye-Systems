package edu.farmingdale.CSC490.Food;


import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class FoodResult {
    private String foodName;
    private String mealType;
    private int calories;
    private int protein_grams;
    private int carbs_grams;
    private int fat_grams;

}
