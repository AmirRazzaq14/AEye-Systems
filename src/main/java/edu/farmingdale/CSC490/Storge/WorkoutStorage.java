package edu.farmingdale.CSC490.Storge;

import com.google.cloud.firestore.Firestore;
import edu.farmingdale.CSC490.Entity.Workout_log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class WorkoutStorage implements DateRangeDataRequest<Workout_log> {

    @Autowired
    private Firestore firestore;

    @Override
    public List<Workout_log> getDataByUserIdAndDateRange(
            int userId, LocalDate startDate, LocalDate endDate) throws Exception {

        return firestore.collection("workout_logs")
                .whereEqualTo("user_id", String.valueOf(userId))
                .get().get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(Workout_log.class))
                .filter(log -> log.getWorkout_date() != null
                        && !log.getWorkout_date().isBefore(startDate)
                        && !log.getWorkout_date().isAfter(endDate))
                .collect(Collectors.toList());
    }
}