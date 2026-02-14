package edu.farmingdale.CSC490.Storge;

import edu.farmingdale.CSC490.Entity.Goal;

import java.time.LocalDate;
import java.util.List;

public class GoalStorage implements DateRangeDataRequest<Goal>{

    @Override
    public List<Goal> getDataByUserIdAndDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        // retrieve data from database and add to list
        return null;
    }
}
