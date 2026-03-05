package edu.farmingdale.CSC490.Service;

import edu.farmingdale.CSC490.Entity.Exercise;
import edu.farmingdale.CSC490.Repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ExerciseService {

    @Autowired
    private ExerciseRepository exerciseRepository;

    public void createExercise(Exercise exercise) throws ExecutionException, InterruptedException {
        exerciseRepository.save(exercise);
    }

    public List<Exercise> getAllExercises() throws ExecutionException, InterruptedException {
        return exerciseRepository.findAll();
    }

    public Exercise getExerciseById(String exerciseId) throws ExecutionException, InterruptedException {
        return exerciseRepository.findById(exerciseId);
    }

    public List<Exercise> getExercisesByMuscleGroup(String muscleGroup) throws ExecutionException, InterruptedException {
        return exerciseRepository.findByMuscleGroup(muscleGroup);
    }

    public void updateExercise(String exerciseId, Exercise exercise) throws ExecutionException, InterruptedException {
        exerciseRepository.update(exerciseId, exercise);
    }

    public void deleteExercise(String exerciseId) throws ExecutionException, InterruptedException {
        exerciseRepository.delete(exerciseId);
    }
}