package edu.farmingdale.CSC490.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nutrition_log {
    private String id;
    private String userId;
    private String date;
    private List<Meal> meals;
    private String notes;
    private String updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Meal {
        private String name;
        private String cals;
        private String protein;
    }
}