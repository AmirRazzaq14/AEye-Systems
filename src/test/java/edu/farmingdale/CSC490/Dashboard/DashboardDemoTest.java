package edu.farmingdale.CSC490.Dashboard;

import edu.farmingdale.CSC490.Dashboard.Entity.Card;
import edu.farmingdale.CSC490.Dashboard.Entity.Chart;
import edu.farmingdale.CSC490.Dashboard.Entity.DashboardView;
import edu.farmingdale.CSC490.Dashboard.Entity.RecentActivity;
import edu.farmingdale.CSC490.Entity.Body_measurement;
import edu.farmingdale.CSC490.Entity.Goal;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import edu.farmingdale.CSC490.Entity.Workout_log;
import edu.farmingdale.CSC490.Storge.DateRangeDataRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;


//  Dashboard demo test, it is used to test whether the data is ok using mock data

public class DashboardDemoTest {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== AEye-Systems Dashboard Demo ===\n");
        
        // Create a simulation data service
        DashboardService demoService = createDemoDashboardService();
        
        // Build dashboard views
        DashboardView dashboardView = demoService.buildDashboard(1);
        
        // Displays dashboard data
        displayDashboard(dashboardView);
    }
    
    private static DashboardService createDemoDashboardService() throws Exception {
        // Create a mock data store
        DateRangeDataRequest<Workout_log> mockWorkoutStorage = createMockWorkoutStorage();
        DateRangeDataRequest<Nutrition_log> mockNutritionStorage = createMockNutritionStorage();
        DateRangeDataRequest<Goal> mockGoalStorage = createMockGoalStorage();
        DateRangeDataRequest<Body_measurement> mockBodyMeasurementStorage = createMockBodyMeasurementStorage();
        
        return new DashboardService(
                mockWorkoutStorage,
                mockNutritionStorage,
                mockGoalStorage,
                mockBodyMeasurementStorage
        );
    }
    
    private static DateRangeDataRequest<Workout_log> createMockWorkoutStorage() throws Exception {
        DateRangeDataRequest<Workout_log> mockStorage = mock(DateRangeDataRequest.class);
        
        // Create simulated site log data
        List<Workout_log> currentWeekWorkouts = new ArrayList<>();
        Workout_log workout1 = new Workout_log();
        workout1.setExercises_done(5);
        workout1.setReps_per_exercise(10);
        workout1.setWorkout_date(LocalDate.now());
        workout1.setLogged_at(Instant.now());
        currentWeekWorkouts.add(workout1);
        
        Workout_log workout2 = new Workout_log();
        workout2.setExercises_done(3);
        workout2.setReps_per_exercise(12);
        workout2.setWorkout_date(LocalDate.now().minusDays(2));
        workout2.setLogged_at(Instant.now().minusSeconds(3600));
        currentWeekWorkouts.add(workout2);
        
        List<Workout_log> previousWeekWorkouts = new ArrayList<>();
        Workout_log prevWorkout = new Workout_log();
        prevWorkout.setExercises_done(2);
        prevWorkout.setReps_per_exercise(8);
        prevWorkout.setWorkout_date(LocalDate.now().minusWeeks(1));
        prevWorkout.setLogged_at(Instant.now().minusSeconds(86400));
        previousWeekWorkouts.add(prevWorkout);
        
        when(mockStorage.getDataByUserIdAndDateRange(anyInt(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(currentWeekWorkouts)
            .thenReturn(previousWeekWorkouts); // The second call returns the previous week's data
        
        return mockStorage;
    }
    
    private static DateRangeDataRequest<Nutrition_log> createMockNutritionStorage() throws Exception {
        DateRangeDataRequest<Nutrition_log> mockStorage = mock(DateRangeDataRequest.class);
        
        // Create simulated nutrient log data
        List<Nutrition_log> currentWeekNutrition = new ArrayList<>();
        Nutrition_log nutrition1 = new Nutrition_log();
        nutrition1.setCalories(2000);
        nutrition1.setLog_date(LocalDate.now());
        nutrition1.setLogged_at(Instant.now());
        currentWeekNutrition.add(nutrition1);
        
        Nutrition_log nutrition2 = new Nutrition_log();
        nutrition2.setCalories(1800);
        nutrition2.setLog_date(LocalDate.now().minusDays(1));
        nutrition2.setLogged_at(Instant.now().minusSeconds(3600));
        currentWeekNutrition.add(nutrition2);
        
        List<Nutrition_log> previousWeekNutrition = new ArrayList<>();
        Nutrition_log prevNutrition = new Nutrition_log();
        prevNutrition.setCalories(2200);
        prevNutrition.setLog_date(LocalDate.now().minusWeeks(1));
        prevNutrition.setLogged_at(Instant.now().minusSeconds(86400));
        previousWeekNutrition.add(prevNutrition);
        
        when(mockStorage.getDataByUserIdAndDateRange(anyInt(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(currentWeekNutrition)
            .thenReturn(previousWeekNutrition); // The second call returns the previous week's data
        
        return mockStorage;
    }
    
    private static DateRangeDataRequest<Goal> createMockGoalStorage() throws Exception {
        DateRangeDataRequest<Goal> mockStorage = mock(DateRangeDataRequest.class);
        
        // Create the target data for the simulation
        List<Goal> currentWeekGoals = new ArrayList<>();
        Goal goal1 = new Goal();
        goal1.setStatus("completed");
        goal1.setCreated_at(Instant.now());
        currentWeekGoals.add(goal1);
        
        Goal goal2 = new Goal();
        goal2.setStatus("in_progress");
        goal2.setCreated_at(Instant.now().minusSeconds(1800));
        currentWeekGoals.add(goal2);
        
        List<Goal> previousWeekGoals = new ArrayList<>();
        Goal prevGoal = new Goal();
        prevGoal.setStatus("completed");
        prevGoal.setCreated_at(Instant.now().minusSeconds(86400));
        previousWeekGoals.add(prevGoal);
        
        when(mockStorage.getDataByUserIdAndDateRange(anyInt(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(currentWeekGoals)
            .thenReturn(previousWeekGoals); // The second call returns the previous week's data
        
        return mockStorage;
    }
    
    private static DateRangeDataRequest<Body_measurement> createMockBodyMeasurementStorage() throws Exception {
        DateRangeDataRequest<Body_measurement> mockStorage = mock(DateRangeDataRequest.class);
        
        // Create simulated body measurements
        List<Body_measurement> currentWeekMeasurements = new ArrayList<>();
        Body_measurement measurement1 = new Body_measurement();
        measurement1.setBody_part("Arm");
        measurement1.setRecorded_at(Instant.now());
        currentWeekMeasurements.add(measurement1);
        
        Body_measurement measurement2 = new Body_measurement();
        measurement2.setBody_part("Waist");
        measurement2.setRecorded_at(Instant.now().minusSeconds(3600));
        currentWeekMeasurements.add(measurement2);
        
        when(mockStorage.getDataByUserIdAndDateRange(anyInt(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(currentWeekMeasurements);
        
        return mockStorage;
    }
    
    private static void displayDashboard(DashboardView dashboardView) {
        System.out.println("📊 Dashboard overview");
        System.out.println("========================");
        
        // Displays card data
        System.out.println("\n📈 Indicator card:");
        displayCard(dashboardView.getWorkoutCard(), "💪");
        displayCard(dashboardView.getNutritionCard(), "🍎");
        displayCard(dashboardView.getGoalCard(), "🎯");
        
        // Displays chart data
        System.out.println("\n📊 Chart data:");
        displayChart(dashboardView.getActivityChart(), "Activities");
        displayChart(dashboardView.getNutritionChart(), "Nutrition");
        
        // Shows recent activity
        System.out.println("\n📝 Recent Activity:");
        displayRecentActivity(dashboardView.getRecentActivity());
        
        System.out.println("\n✅ The dashboard data display is complete!");
    }
    
    private static void displayCard(Card card, String emoji) {
        String trendSymbol = card.getTrend() >= 0 ? "↗️" : "↘️";
        System.out.printf("%s %s: %d (%d %s)\n", 
            emoji, card.getTitle(), card.getValue(), Math.abs(card.getTrend()), trendSymbol);
    }
    
    private static void displayChart(Chart chart, String type) {
        System.out.printf("📈 %s Chart - %s:\n", type, chart.getTitle());
        for (Map.Entry<String, Integer> entry : chart.getData().entrySet()) {
            System.out.printf("  • %s: %d\n", entry.getKey(), entry.getValue());
        }
        System.out.println();
    }
    
    private static void displayRecentActivity(List<RecentActivity> activities) {
        if (activities.isEmpty()) {
            System.out.println("  There is no activity record at the moment");
            return;
        }
        
        for (int i = 0; i < Math.min(activities.size(), 5); i++) { // Only the first 5 items are displayed
            RecentActivity activity = activities.get(i);
            System.out.printf("  🕒 [%s] %s: %s\n", 
                activity.getTime().toString().substring(0, 19), 
                activity.getType(), 
                activity.getInfo());
        }
    }

}
