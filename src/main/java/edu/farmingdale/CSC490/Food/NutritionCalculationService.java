package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Entity.User;
import org.springframework.stereotype.Service;

@Service
public class NutritionCalculationService {

    public void calculateTotalNutrition(Nutrition_log log) {
        if (log.getMeals() == null || log.getMeals().isEmpty()) {
            log.setTotalNutrition(new Nutrition_log.Nutrition("0", "0", "0", "0"));
            return;
        }

        double totalCals = 0, totalProtein = 0, totalCarb = 0, totalFat = 0;

        for (Nutrition_log.Meal meal : log.getMeals()) {
            totalCals += getValue(Double.valueOf(meal.getCals()));
            totalProtein += getValue(Double.valueOf(meal.getProtein()));
            totalCarb += getValue(Double.valueOf(meal.getCarb()));
            totalFat += getValue(Double.valueOf(meal.getFat()));
        }

        log.setTotalNutrition(new Nutrition_log.Nutrition(
                String.valueOf(round(totalCals)), String.valueOf(round(totalProtein)),
                String.valueOf(round(totalCarb)), String.valueOf(round(totalFat))
        ));
    }

    public void calculateTargetNutrition(Nutrition_log log, User user) {
        if (user == null) {
            log.setTargetNutrition(new Nutrition_log.Nutrition("2500", "150", "100", "100"));
            return;
        }
        if(user.getCalorieGoal() != null){
            calculateTargetNutritionByGoal(log, user.getCalorieGoal());
        }else {

            double bmr = calculateBMR(user);
            double tdee = bmr * 1.2;

            log.setTargetNutrition(calculateMacros(tdee));
        }
    }

    // Calculated based on custom calorie goals
    public void calculateTargetNutritionByGoal(Nutrition_log log, double calorieGoal) {
        log.setTargetNutrition(calculateMacros(calorieGoal));
    }

    private Nutrition_log.Nutrition calculateMacros(double totalCals) {
        return new Nutrition_log.Nutrition(
                String.valueOf(round(totalCals)),
                String.valueOf(round((totalCals * 0.30) / 4.0)),  // Protein 30%
                String.valueOf(round((totalCals * 0.40) / 4.0)),  // Carb 40%
                String.valueOf(round((totalCals * 0.30) / 9.0))  // Fat 30%
        );
    }

    private double calculateBMR(User user) {
        if ("female".equalsIgnoreCase(user.getGender())) {
            return 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        } else {
            return 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        }
    }

    private double getValue(Double value) {
        return value != null ? value : 0.0;
    }

    private int round(double value) {
        return (int) Math.round(value);
    }
}
