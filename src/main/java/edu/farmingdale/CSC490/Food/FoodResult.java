package edu.farmingdale.CSC490.Food;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class FoodResult {
    private String foodName;
    private String mealType;
    private Double calories;
    private Double protein_grams;
    private Double carbs_grams;
    private Double fat_grams;
}
