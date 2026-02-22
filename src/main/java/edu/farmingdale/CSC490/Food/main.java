package edu.farmingdale.CSC490.Food;

public class main {
    public static void main(String[] args) {

        String image1 = "src/main/resources/images/apple.jpg";
        String image2 = "src/main/resources/images/bananas.jpg";
        String image3 = "src/main/resources/images/Cheeseburger.jpg";
        String image4 = "src/main/resources/images/rice.jpg";
        String image5 = "src/main/resources/images/apple2.jpg";


        foodAnalysis  analyzer = new foodAnalysis();
        FoodResult result = analyzer.analyzeImage(image3);

        if(result != null) {
            System.out.println("Food Name: " + result.getFoodName());
            System.out.println("Food Type: " + result.getFoodType());
            System.out.println("Serving Size: " + result.getServingSize());
            System.out.println("Calories: " + result.getCalories());
            System.out.println("Protein (grams): " + result.getProtein_grams());
            System.out.println("Carbs (grams): " + result.getCarbs_grams());
            System.out.println("Fat (grams): " + result.getFat_grams());
        }else {
            System.out.println("No result found.");
        }

    }
}
