package edu.farmingdale.CSC490.Storge;

import com.google.cloud.firestore.Firestore;
import edu.farmingdale.CSC490.Entity.Body_measurement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class bodyStorage implements DateRangeDataRequest<Body_measurement> {

    @Autowired
    private Firestore firestore;

    @Override
    public List<Body_measurement> getDataByUserIdAndDateRange(
            int userId, LocalDate startDate, LocalDate endDate) throws Exception {

        return firestore.collection("body_measurements")
                .whereEqualTo("user_id", String.valueOf(userId))
                .get().get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(Body_measurement.class))
                .filter(measurement -> {
                    LocalDate recordedDate = measurement.getRecorded_at() != null
                            ? measurement.getRecorded_at().atZone(ZoneId.systemDefault()).toLocalDate()
                            : LocalDate.MIN;
                    return !recordedDate.isBefore(startDate) && !recordedDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }
}
