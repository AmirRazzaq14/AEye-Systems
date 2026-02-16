package edu.farmingdale.CSC490.Dashboard;

import edu.farmingdale.CSC490.Dashboard.Entity.Card;
import edu.farmingdale.CSC490.Dashboard.Entity.Chart;
import edu.farmingdale.CSC490.Dashboard.Entity.DashboardView;
import edu.farmingdale.CSC490.Dashboard.Entity.RecentActivity;
import edu.farmingdale.CSC490.Entity.*;
import edu.farmingdale.CSC490.Storge.*;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardService {

    private final DateRangeDataRequest<Workout_log> workoutStorage;
    private final DateRangeDataRequest<Nutrition_log> nutritionStorage;
    private final DateRangeDataRequest<Goal> goalStorage;
    private final DateRangeDataRequest<Body_measurement> bodyMeasurementStorage;

    private final LocalDate today = LocalDate.now();
    private final LocalDate startWeek = today.with(DayOfWeek.MONDAY);

    public DashboardService() {
        this.workoutStorage = new WorkoutStorage();
        this.nutritionStorage = new NutritionStorage();
        this.goalStorage = new GoalStorage();
        this.bodyMeasurementStorage = new bodyStorage();
    }

    public DashboardService(
            DateRangeDataRequest<Workout_log> workoutStorage,
            DateRangeDataRequest<Nutrition_log> nutritionStorage,
            DateRangeDataRequest<Goal> goalStorage,
            DateRangeDataRequest<Body_measurement> bodyMeasurementStorage) {
        this.workoutStorage = workoutStorage;
        this.nutritionStorage = nutritionStorage;
        this.goalStorage = goalStorage;
        this.bodyMeasurementStorage = bodyMeasurementStorage;
    }
    public DashboardView buildDashboard(int userId) {

        Card workoutCard = buildWorkoutCard(userId);
        Card nutritionCard = buildNutritionCard(userId);
        Card goalCard = buildGoalCard(userId);
        Chart activityChart = buildActivityChart(userId);
        Chart nutritionChart = buildNutritionChart(userId);
        List<RecentActivity> recentActivity = buildRecentActivity(userId);


        return DashboardView.builder()
                .workoutCard(workoutCard)
                .nutritionCard(nutritionCard)
                .goalCard(goalCard)
                .activityChart(activityChart)
                .nutritionChart(nutritionChart)
                .recentActivity(recentActivity)
                .build();
    }

    //  summary weekly Workout  and shows trend with previous week
    Card buildWorkoutCard(int userId) {


        //from monday to today is weekly workout data range
        List<Workout_log> currentWeekData = workoutStorage.getDataByUserIdAndDateRange(userId, startWeek, today);
        //previous week need minus one week
        List<Workout_log> previousWeekData = workoutStorage
                .getDataByUserIdAndDateRange(userId, startWeek.minusWeeks(1),
                                                        today.minusDays(1));

        int currentWeekWorkouts = currentWeekData.size();
        int previousWeekWorkouts = previousWeekData.size();
        int trend = currentWeekWorkouts - previousWeekWorkouts;

        return new Card("Workout weekly", currentWeekWorkouts, trend);
    }

    private Card buildNutritionCard(int userId) {

        List<Nutrition_log> currentWeekData = nutritionStorage.getDataByUserIdAndDateRange(userId, startWeek, today);
        List<Nutrition_log> previousWeekData = nutritionStorage
                .getDataByUserIdAndDateRange(userId, startWeek.minusWeeks(1),
                                                        today.minusDays(1));


        int currentWeekCalories = currentWeekData.stream()
                .mapToInt(Nutrition_log::getCalories)
                .sum();
        int previousWeekCalories = previousWeekData.stream()
                .mapToInt(Nutrition_log::getCalories)
                .sum();

        int trend = currentWeekCalories - previousWeekCalories;


        return new Card("Calories burned", currentWeekCalories, trend);
    }

    private Card buildGoalCard(int userId) {
        LocalDate today = LocalDate.now();
        LocalDate startWeek = today.with(DayOfWeek.MONDAY);

        List<Goal> currentWeekData = goalStorage.getDataByUserIdAndDateRange(userId, startWeek, today);
        List<Goal> previousWeekData = goalStorage
                .getDataByUserIdAndDateRange(userId, startWeek.minusWeeks(1),
                                                        today.minusDays(1));

        int currentWeekGoals = currentWeekData.size();
        int previousWeekGoals = previousWeekData.size();
        int trend = currentWeekGoals - previousWeekGoals;

        return new Card("Goal completion", currentWeekGoals, trend);
    }

    // iterate through each workout log, to build a liner chart at UI
    private Chart buildActivityChart(int userId) {
        LocalDate today = LocalDate.now();
        LocalDate startWeek = today.with(DayOfWeek.MONDAY);

        Map<String, Integer> activityData = new HashMap<>();

        List<Workout_log> currentWeekData = workoutStorage.getDataByUserIdAndDateRange(userId, startWeek, today);

        for (Workout_log workout : currentWeekData) {
            String dayOfWeek = workout.getWorkout_date().getDayOfWeek().toString();
            int exercisesDone = workout.getExercises_done();
            activityData.put(dayOfWeek, exercisesDone);
        }

        return new Chart("Weekly Activity", activityData);
    }

    private Chart buildNutritionChart(int userId) {

        Map<String, Integer> nutritionData = new HashMap<>();

        List<Nutrition_log> currentWeekData = nutritionStorage.getDataByUserIdAndDateRange(userId, startWeek, today);

        for (Nutrition_log nutrition : currentWeekData) {
            String dayOfWeek = nutrition.getLog_date().getDayOfWeek().toString();
            int calories = nutrition.getCalories();
            nutritionData.put(dayOfWeek, calories);
        }

        return new Chart("Nutrition Tracking", nutritionData);
    }

    //  build recent activity, limit on weekly basis
    private List<RecentActivity> buildRecentActivity(int userId) {
        LocalDate startData = today.minusWeeks(1);

        List<RecentActivity> recentActivity = new ArrayList<>();

        // get recent activity from workout, nutrition, body measurement and goal
        List<Workout_log> currentWeekData = workoutStorage
                .getDataByUserIdAndDateRange(userId, startData, today);
        List<Nutrition_log> currentWeekNutritionData = nutritionStorage
                .getDataByUserIdAndDateRange(userId, startData, today);
        List<Body_measurement> currentWeekBodyData = bodyMeasurementStorage
                .getDataByUserIdAndDateRange(userId, startData, today);
        List<Goal> currentWeekGoalData = goalStorage
                .getDataByUserIdAndDateRange(userId, startData, today);

        //  add to recent activity
        for (Workout_log workout : currentWeekData) {
            String type = "Workout";
            String info = workout.getReps_per_exercise()  + " reps";
            Instant time = workout.getLogged_at();
            recentActivity.add(new RecentActivity(type, info, time));
        }

        for (Nutrition_log nutrition : currentWeekNutritionData) {
            String type = "Nutrition";
            String info = nutrition.getCalories()  + " calories";
            Instant time = nutrition.getLogged_at();
            recentActivity.add(new RecentActivity(type, info, time));
        }

        for (Body_measurement body : currentWeekBodyData){
            String type = "Body Measurement";
            String info = body.getBody_part();
            Instant time = body.getRecorded_at();
            recentActivity.add(new RecentActivity(type, info, time));
        }

        for (Goal goal : currentWeekGoalData){
            String type = "Goal";
            String info = "status: " + goal.getStatus();
            Instant time = goal.getCreated_at();
            recentActivity.add(new RecentActivity(type, info, time));
        }

        //  sort by time in descending order
        recentActivity.sort((a, b) -> b.getTime().compareTo(a.getTime()));

        return recentActivity;
    }



}
