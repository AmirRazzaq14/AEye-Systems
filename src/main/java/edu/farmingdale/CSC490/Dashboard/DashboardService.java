package edu.farmingdale.CSC490.Dashboard;

import edu.farmingdale.CSC490.Dashboard.Entity.Card;
import edu.farmingdale.CSC490.Dashboard.Entity.Chart;
import edu.farmingdale.CSC490.Dashboard.Entity.DashboardView;
import edu.farmingdale.CSC490.Dashboard.Entity.RecentActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardService {


    public DashboardView buildDashboard(int userId) {

        DashboardView dv = new DashboardView();

        //  Workout Card
        Card workoutCard = new Card("Workout", 21, 12);
        dv.setWorkoutCard(workoutCard);


        //  Nutrition Card
        Card nutritionCard = new Card("Calories burned", 2990, 8);
        dv.setNutritionCard(nutritionCard);


        //   Goal Card
        Card goalCard = new Card("Goal completion", 94, 5);
        dv.setGoalCard(goalCard);


        //   Activity Chart
        Chart activityChart = new Chart("Weekly Activity", null);
        Map<String, Integer> activityData = new HashMap<>();
        activityData.put("Monday", 100);
        activityData.put("Tuesday", 200);
        activityData.put("Wednesday", 300);
        activityData.put("Thursday", 400);
        activityData.put("Friday", 500);
        activityData.put("Saturday", 600);
        activityData.put("Sunday", 700);
        activityChart.setData(activityData);
        dv.setActivityChart(activityChart);


        //   Nutrition Chart
        Chart nutritionChart = new Chart("Nutrition Tracking", null);
        Map<String, Integer> nutritionData = new HashMap<>();
        nutritionData.put("Monday", 1900);
        nutritionData.put("Tuesday", 2000);
        nutritionData.put("Wednesday", 2100);
        nutritionData.put("Thursday", 2200);
        nutritionData.put("Friday", 2300);
        nutritionData.put("Saturday", 2400);
        nutritionData.put("Sunday", 2500);
        nutritionChart.setData(nutritionData);
        dv.setNutritionChart(nutritionChart);


        //  Recent Activity
        List<RecentActivity> recentActivity = new ArrayList<>();
        recentActivity.add(new RecentActivity("Exercise", "Walking", "Completed 30 minutes of walking", "3 hours ago"));
        recentActivity.add(new RecentActivity("Nutrition", "Apple", "Eaten an apple", "4 hours ago"));
        recentActivity.add(new RecentActivity("Measurement", "Body Measurements", "Recorded body measurements", "5 hours ago"));
        dv.setRecentActivity(recentActivity);

        return dv;
    }



}
