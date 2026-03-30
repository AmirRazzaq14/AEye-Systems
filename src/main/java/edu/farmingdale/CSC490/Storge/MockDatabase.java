package edu.farmingdale.CSC490.Storge;

import edu.farmingdale.CSC490.Entity.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class MockDatabase {
    public final List<Workout_log> workoutLogs = new ArrayList<>();
    public final List<Nutrition_log> nutritionLogs = new ArrayList<>();
    public final List<Goal> goals = new ArrayList<>();
    public final List<Body_measurement> bodyMeasurements = new ArrayList<>();
    public final List<User> users = new ArrayList<>();

    @PostConstruct
    public void initDummyData() {
        // Create user 1
        User u1 = new User();
        u1.setUser_id(String.valueOf(1));
        u1.setEmail("john.doe@example.com");
        users.add(u1);

        LocalDate today = LocalDate.now();

        // Add 2 Workouts this week for user 1
        Workout_log w1 = new Workout_log();
        w1.setUser_id(1);
        w1.setWorkout_date(today);
        w1.setExercises_done(5);
        w1.setReps_per_exercise(12);
        w1.setLogged_at(Instant.now());
        workoutLogs.add(w1);

        Workout_log w2 = new Workout_log();
        w2.setUser_id(1);
        w2.setWorkout_date(today.minusDays(1));
        w2.setExercises_done(4);
        w2.setReps_per_exercise(10);
        w2.setLogged_at(Instant.now());
        workoutLogs.add(w2);

        // Add Nutrition using new meal-based structure
        List<Nutrition_log.Meal> meals = new ArrayList<>();
        meals.add(new Nutrition_log.Meal("Breakfast", "600", "30"));
        meals.add(new Nutrition_log.Meal("Lunch", "900", "40"));
        meals.add(new Nutrition_log.Meal("Dinner", "900", "45"));

        Nutrition_log n1 = new Nutrition_log();
        n1.setUserId("1");
        n1.setDate(today.toString());
        n1.setMeals(meals);
        n1.setNotes("Felt good today");
        n1.setUpdatedAt(Instant.now().toString());
        nutritionLogs.add(n1);

        // Add Goal
        Goal g1 = new Goal();
        g1.setUser_id(1);
        g1.setTarget_weight(180.0);
        g1.setStatus("in_progress");
        g1.setCreated_at(Instant.now());
        goals.add(g1);

        // Add Body Measurement
        Body_measurement m1 = new Body_measurement();
        m1.setUser_id(1);
        m1.setBody_part("Bicep");
        m1.setLength_in(14.5);
        m1.setRecorded_at(Instant.now());
        bodyMeasurements.add(m1);
    }
}