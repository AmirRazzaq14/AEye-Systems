package edu.farmingdale.CSC490.Food;

public class main {
    public static void main(String[] args) {
        foodAnalysis  analyzer = new foodAnalysis();
        FoodResult result = analyzer.analyzeImage();

        System.out.println("Food Name: " + result.getFoodName());
        System.out.println("Food Type: " + result.getFoodType());
        System.out.println("Serving Size: " + result.getServingSize());
        System.out.println("Calories: " + result.getCalories());
        System.out.println("Protein (grams): " + result.getProtein_grams());
        System.out.println("Carbs (grams): " + result.getCarbs_grams());
        System.out.println("Fat (grams): " + result.getFat_grams());

    }
}
