package edu.farmingdale.CSC490.Repository;

import com.google.cloud.firestore.Firestore;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class NutritionRepository {

    @Autowired
    private Firestore firestore;

    // Save a nutrition log
    public void save(Nutrition_log log) throws Exception {
        firestore.collection("nutrition_logs")
                .document()
                .set(log)
                .get();
    }

    // Get all logs for a user
    public List<Nutrition_log> findByUserId(String userId) throws Exception {
        return firestore.collection("nutrition_logs")
                .whereEqualTo("userId", userId)
                .get().get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(Nutrition_log.class))
                .collect(Collectors.toList());
    }

    // Delete a log
    public void delete(String logId) throws Exception {
        firestore.collection("nutrition_logs")
                .document(logId)
                .delete()
                .get();
    }
}