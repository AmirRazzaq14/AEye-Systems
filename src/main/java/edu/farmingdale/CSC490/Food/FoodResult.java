package edu.farmingdale.CSC490.Food;

import lombok.Data;

@Data
public class FoodResult {
    private String foodName;
    private String foodType;
    private double servingSize;
    private double calories;
    private double protein_grams;
    private double carbs_grams;
    private double fat_grams;
}
