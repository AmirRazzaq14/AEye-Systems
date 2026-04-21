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

        double targetCals;
        double targetProtein;
        double targetCarb;
        double targetFat;

        // Calculation of Basal Metabolic Rate (BMR) Using the Mifflin-St Jeor Formula
        double bmr;
        if (user.getGender().equalsIgnoreCase("female")) {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        } else {
            // By default, it is calculated as male, or includes other gender situations
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        }

        // Assuming an activity factor of 1.2 (sedentary/light activity), it can be adjusted according to actual needs
        double activityFactor = 1.2;

       // targetCals = user.getTargetCals() != null ? user.getTargetCals() : bmr * activityFactor;
        targetCals = bmr * activityFactor;

        // Distribute macronutrients based on total caloric targets
        // Protein: about 30% calories (1g protein = 4 kcal)
        targetProtein = (targetCals * 0.30) / 4.0;
        // Carbohydrates: about 40% calories (1g carbs = 4 kcal)
        targetCarb = (targetCals * 0.40) / 4.0;
        // Fat: Approximately 30% calories (1g fat = 9 kcal)
        targetFat = (targetCals * 0.30) / 9.0;

        this.targetNutrition = new Nutrition();
        this.targetNutrition.setCals(String.valueOf(Math.round(targetCals)));
        this.targetNutrition.setProtein(String.valueOf(Math.round(targetProtein)));
        this.targetNutrition.setCarb(String.valueOf(Math.round(targetCarb)));
        this.targetNutrition.setFat(String.valueOf(Math.round(targetFat)));

    }


}