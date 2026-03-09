package edu.farmingdale.CSC490.Storge;

import com.google.cloud.firestore.Firestore;
import edu.farmingdale.CSC490.Entity.Nutrition_log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class NutritionStorage implements DateRangeDataRequest<Nutrition_log> {

    @Autowired
    private Firestore firestore;

    @Override
    public List<Nutrition_log> getDataByUserIdAndDateRange(
            int userId, LocalDate startDate, LocalDate endDate) throws Exception {

        return firestore.collection("users")
                .document(String.valueOf(userId))
                .collection("nutritionLogs")
                .get().get()
                .getDocuments()
                .stream()
                .map(doc -> {
                    Nutrition_log log = doc.toObject(Nutrition_log.class);
                    if (log != null) {
                        log.setId(doc.getId());
                        log.setDate(doc.getId()); // document ID is the date string
                    }
                    return log;
                })
                .filter(log -> {
                    if (log == null || log.getDate() == null) return false;
                    try {
                        LocalDate logDate = LocalDate.parse(log.getDate());
                        return !logDate.isBefore(startDate) && !logDate.isAfter(endDate);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }
}