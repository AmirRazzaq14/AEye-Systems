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

        return firestore.collection("nutrition_logs")
                .whereEqualTo("user_id", String.valueOf(userId))
                .get().get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(Nutrition_log.class))
                .filter(log -> log.getLog_date() != null
                        && !log.getLog_date().isBefore(startDate)
                        && !log.getLog_date().isAfter(endDate))
                .collect(Collectors.toList());
    }
}
