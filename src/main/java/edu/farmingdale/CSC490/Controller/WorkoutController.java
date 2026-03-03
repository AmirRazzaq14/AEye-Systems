package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Entity.Workout_log;
import edu.farmingdale.CSC490.Storge.MockDatabase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final MockDatabase mockDatabase;

    public WorkoutController(MockDatabase mockDatabase) {
        this.mockDatabase = mockDatabase;
    }

    @GetMapping
    public ResponseEntity<List<Workout_log>> getAllWorkouts(@RequestParam(required = false) Integer userId) {
        if (userId != null) {
            List<Workout_log> userWorkouts = mockDatabase.workoutLogs.stream()
                    .filter(w -> w.getUser_id() == userId)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userWorkouts);
        }
        return ResponseEntity.ok(mockDatabase.workoutLogs);
    }

    @PostMapping
    public ResponseEntity<Workout_log> createWorkout(@RequestBody Workout_log workout) {
        int newId = mockDatabase.workoutLogs.size() + 1;
        workout.setSession_log_id(newId);
        if (workout.getLogged_at() == null) {
            workout.setLogged_at(Instant.now());
        }
        if (workout.getWorkout_date() == null) {
            workout.setWorkout_date(LocalDate.now());
        }
        mockDatabase.workoutLogs.add(workout);
        return ResponseEntity.status(201).body(workout);
    }
}
