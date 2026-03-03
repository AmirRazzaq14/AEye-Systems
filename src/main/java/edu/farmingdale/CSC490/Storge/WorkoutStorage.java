package edu.farmingdale.CSC490.Storge;

import edu.farmingdale.CSC490.Entity.Workout_log;

import java.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class WorkoutStorage implements DateRangeDataRequest<Workout_log> {

    private final MockDatabase mockDatabase;

    public WorkoutStorage(MockDatabase mockDatabase) {
        this.mockDatabase = mockDatabase;
    }

    @Override
    public List<Workout_log> getDataByUserIdAndDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        return mockDatabase.workoutLogs.stream()
                .filter(log -> log.getUser_id() == userId)
                .filter(log -> !log.getWorkout_date().isBefore(startDate) && !log.getWorkout_date().isAfter(endDate))
                .collect(Collectors.toList());
    }
}
