package edu.farmingdale.CSC490.Storge;

import edu.farmingdale.CSC490.Entity.Workout_log;

import java.time.LocalDate;
import java.util.List;

public class WorkoutStorage implements DateRangeDataRequest<Workout_log> {
    @Override

    public List<Workout_log> getDataByUserIdAndDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        // retrieve data from database and add to list

        return null;
    }
}
