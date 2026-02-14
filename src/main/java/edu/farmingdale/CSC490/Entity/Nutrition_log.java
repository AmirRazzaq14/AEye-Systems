package edu.farmingdale.CSC490.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nutrition_log {
    private int log_id;
    private int user_id;
    private LocalDate log_date;
    private String meal_type;
    private String food_name;
    private int calories;
    private int protein_grams;
    private int carbs_grams;
    private int fat_grams;
    private Instant logged_at;
}
