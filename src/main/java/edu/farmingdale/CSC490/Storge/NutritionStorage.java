package edu.farmingdale.CSC490.Storge;

import edu.farmingdale.CSC490.Entity.Nutrition_log;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class NutritionStorage implements DateRangeDataRequest<Nutrition_log> {

    private final MockDatabase mockDatabase;

    public NutritionStorage(MockDatabase mockDatabase) {
        this.mockDatabase = mockDatabase;
    }

    @Override
    public List<Nutrition_log> getDataByUserIdAndDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        return mockDatabase.nutritionLogs.stream()
                .filter(log -> log.getUser_id() == userId)
                .filter(log -> !log.getLog_date().isBefore(startDate) && !log.getLog_date().isAfter(endDate))
                .collect(Collectors.toList());
    }
}
