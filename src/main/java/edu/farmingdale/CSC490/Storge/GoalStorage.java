package edu.farmingdale.CSC490.Storge;

import com.google.cloud.firestore.Firestore;
import edu.farmingdale.CSC490.Entity.Goal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class GoalStorage implements DateRangeDataRequest<Goal> {

    @Autowired
    private Firestore firestore;

    @Override
    public List<Goal> getDataByUserIdAndDateRange(
            int userId, LocalDate startDate, LocalDate endDate) throws Exception {

        return firestore.collection("goals")
                .whereEqualTo("user_id", String.valueOf(userId))
                .get().get()
                .getDocuments()
                .stream()
                .map(doc -> doc.toObject(Goal.class))
                .filter(goal -> {
                    LocalDate createdDate = goal.getCreated_at() != null
                            ? goal.getCreated_at().atZone(ZoneId.systemDefault()).toLocalDate()
                            : (goal.getStart_date() != null ? goal.getStart_date() : LocalDate.MIN);
                    return !createdDate.isBefore(startDate) && !createdDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }
}