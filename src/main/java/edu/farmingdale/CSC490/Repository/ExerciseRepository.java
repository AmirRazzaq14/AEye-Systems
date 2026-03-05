package edu.farmingdale.CSC490.Repository;

import com.google.cloud.firestore.Firestore;
import edu.farmingdale.CSC490.Entity.Exercise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class ExerciseRepository {

    @Autowired
    private Firestore firestore;

    // Save a new exercise
    public void save(Exercise exercise) throws ExecutionException, InterruptedException {
        firestore.collection("exercises")
                .document()
                .set(exercise)
                .get();
    }

    // Get all exercises
    public List<Exercise> findAll() throws ExecutionException, InterruptedException {
        return firestore.collection("exercises")
                .get().get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(Exercise.class))
                .collect(Collectors.toList());
    }

    // Get exercise by ID
    public Exercise findById(String exerciseId) throws ExecutionException, InterruptedException {
        return firestore.collection("exercises")
                .document(exerciseId)
                .get().get()
                .toObject(Exercise.class);
    }

    // Get exercises by muscle group
    public List<Exercise> findByMuscleGroup(String muscleGroup) throws ExecutionException, InterruptedException {
        return firestore.collection("exercises")
                .whereEqualTo("muscle_group", muscleGroup)
                .get().get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(Exercise.class))
                .collect(Collectors.toList());
    }

    // Update an exercise
    public void update(String exerciseId, Exercise exercise) throws ExecutionException, InterruptedException {
        firestore.collection("exercises")
                .document(exerciseId)
                .set(exercise)
                .get();
    }

    // Delete an exercise
    public void delete(String exerciseId) throws ExecutionException, InterruptedException {
        firestore.collection("exercises")
                .document(exerciseId)
                .delete()
                .get();
    }
}