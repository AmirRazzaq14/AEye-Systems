package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Entity.Workout_log;
import edu.farmingdale.CSC490.Repository.WorkoutRepository;
import edu.farmingdale.CSC490.Service.WorkoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
@CrossOrigin(origins = "*")
public class WorkoutController {

    @Autowired
    private WorkoutService workoutService;

    @GetMapping
    public ResponseEntity<List<Workout_log>> getAllWorkouts(
            @RequestParam(required = false) String userId) throws Exception {
        if (userId != null) {
            return ResponseEntity.ok(workoutService.getWorkoutsByUser(userId));
        }
        return ResponseEntity.ok(workoutService.getAllWorkouts());
    }

    @PostMapping
    public ResponseEntity<String> createWorkout(
            @RequestBody Workout_log workout) throws Exception {
        workoutService.createWorkout(workout);
        return ResponseEntity.status(201).body("Workout logged successfully");
    }
}
