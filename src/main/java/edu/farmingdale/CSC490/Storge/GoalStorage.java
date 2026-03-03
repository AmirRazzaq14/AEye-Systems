package edu.farmingdale.CSC490.Storge;

import edu.farmingdale.CSC490.Entity.Goal;

import java.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class GoalStorage implements DateRangeDataRequest<Goal> {

    private final MockDatabase mockDatabase;

    public GoalStorage(MockDatabase mockDatabase) {
        this.mockDatabase = mockDatabase;
    }

    @Override
    public List<Goal> getDataByUserIdAndDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        return mockDatabase.goals.stream()
                .filter(goal -> goal.getUser_id() == userId)
                .filter(goal -> {
                    LocalDate createdDate = goal.getCreated_at() != null
                            ? goal.getCreated_at().atZone(ZoneId.systemDefault()).toLocalDate()
                            : (goal.getStart_date() != null ? goal.getStart_date() : LocalDate.MIN);
                    return !createdDate.isBefore(startDate) && !createdDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }
}
