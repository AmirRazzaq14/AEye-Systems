package edu.farmingdale.CSC490.Controller;

import edu.farmingdale.CSC490.Entity.Exercise;
import edu.farmingdale.CSC490.Service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    @Autowired
    private ExerciseService exerciseService;

    // GET all exercises
    @GetMapping
    public ResponseEntity<List<Exercise>> getAllExercises() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(exerciseService.getAllExercises());
    }

    // GET exercise by ID
    @GetMapping("/{exerciseId}")
    public ResponseEntity<Exercise> getExerciseById(
            @PathVariable String exerciseId) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(exerciseService.getExerciseById(exerciseId));
    }

    // GET exercises by muscle group
    @GetMapping("/muscle/{muscleGroup}")
    public ResponseEntity<List<Exercise>> getByMuscleGroup(
            @PathVariable String muscleGroup) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(exerciseService.getExercisesByMuscleGroup(muscleGroup));
    }

    // POST create new exercise
    @PostMapping
    public ResponseEntity<String> createExercise(
            @RequestBody Exercise exercise) throws ExecutionException, InterruptedException {
        exerciseService.createExercise(exercise);
        return ResponseEntity.ok("Exercise created successfully");
    }

    // PUT update exercise
    @PutMapping("/{exerciseId}")
    public ResponseEntity<String> updateExercise(
            @PathVariable String exerciseId,
            @RequestBody Exercise exercise) throws ExecutionException, InterruptedException {
        exerciseService.updateExercise(exerciseId, exercise);
        return ResponseEntity.ok("Exercise updated successfully");
    }

    // DELETE exercise
    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<String> deleteExercise(
            @PathVariable String exerciseId) throws ExecutionException, InterruptedException {
        exerciseService.deleteExercise(exerciseId);
        return ResponseEntity.ok("Exercise deleted successfully");
    }
}