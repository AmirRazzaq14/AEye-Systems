package edu.farmingdale.CSC490.Repository;

import com.google.cloud.firestore.Firestore;
import edu.farmingdale.CSC490.Entity.Workout_log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class WorkoutRepository {

    @Autowired
    private Firestore firestore;

    public void save(Workout_log workout) throws Exception {
        firestore.collection("workout_logs")
                .document()
                .set(workout)
                .get();
    }

    public List<Workout_log> findAll() throws Exception {
        return firestore.collection("workout_logs")
                .get().get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(Workout_log.class))
                .collect(Collectors.toList());
    }

    public List<Workout_log> findByUserId(String userId) throws Exception {
        return firestore.collection("workout_logs")
                .whereEqualTo("user_id", userId)
                .get().get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(Workout_log.class))
                .collect(Collectors.toList());
    }
}