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
    private Nutrition totalNutrition; // add new field
    private Nutrition targetNutrition; // add new field
    private String notes;
    private String updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Meal {
        private String mealId; // add new field
        private String name;
        private String cals;
        private String protein;
        private String carb; // add new field
        private String fat; // add new field

        public String getCals() { return cals != null ? cals : "0"; }
        public String getProtein() { return protein != null ? protein : "0"; }
        public String getCarb() { return carb != null ? carb : "0"; }
        public String getFat() { return fat != null ? fat : "0"; }
        public String getName() { return name != null ? name : "Unknown"; }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Nutrition { // add new entity
        private String cals;
        private String protein;
        private String carb;
        private String fat;

        public String getCals() { return cals != null ? cals : "0"; }
        public String getProtein() { return protein != null ? protein : "0"; }
        public String getCarb() { return carb != null ? carb : "0"; }
        public String getFat() { return fat != null ? fat : "0"; }
    }



    public void updateTotalNutrition() {
        if (meals == null || meals.isEmpty()) {
            this.totalNutrition = new Nutrition("0", "0", "0", "0");
            return;
        }

        double totalCals = 0;
        double totalProtein = 0;
        double totalCarb = 0;
        double totalFat = 0;

        for (Meal meal : meals) {
            totalCals += parseNutrient(meal.getCals());
            totalProtein += parseNutrient(meal.getProtein());
            totalCarb += parseNutrient(meal.getCarb());
            totalFat += parseNutrient(meal.getFat());
        }

        this.totalNutrition = new Nutrition();
        this.totalNutrition.setCals(String.valueOf(Math.round(totalCals)));
        this.totalNutrition.setProtein(String.valueOf(Math.round(totalProtein)));
        this.totalNutrition.setCarb(String.valueOf(Math.round(totalCarb)));
        this.totalNutrition.setFat(String.valueOf(Math.round(totalFat)));
    }

    private double parseNutrient(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void updateTargetNutrition(User user) {
        if (user == null) {
            this.targetNutrition = new Nutrition("2500", "150", "100", "100");
            return;
        }

        double targetCals = 0;
        double targetProtein = 0;
        double targetCarb = 0;
        double targetFat = 0;

        // Calculate target nutrition based on user's core profile metrics
        targetCals = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        targetProtein = user.getWeight() * 2;
        targetCarb = user.getWeight() * 3;
        targetFat = user.getWeight() * 1;
        if (user.getGender().equalsIgnoreCase("female")) {
            targetProtein *= 0.9;
            targetCarb *= 0.7;
            targetFat *= 0.5;
        }else if (user.getGender().equalsIgnoreCase("male")) {
            targetProtein *= 1.1;
            targetCarb *= 1.2;
            targetFat *= 1.3;
        }else {
            // Default values for other genders
            targetProtein *= 1.1;
            targetCarb *= 1.2;
            targetFat *= 1.3;
        }

        this.targetNutrition = new Nutrition();
        this.targetNutrition.setCals(String.valueOf(Math.round(targetCals)));
        this.targetNutrition.setProtein(String.valueOf(Math.round(targetProtein)));
        this.targetNutrition.setCarb(String.valueOf(Math.round(targetCarb)));
        this.targetNutrition.setFat(String.valueOf(Math.round(targetFat)));

    }

}