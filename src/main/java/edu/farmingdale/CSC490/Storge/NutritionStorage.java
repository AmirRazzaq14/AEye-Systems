package edu.farmingdale.CSC490.Storge;

import edu.farmingdale.CSC490.Entity.Nutrition_log;

import java.time.LocalDate;
import java.util.List;

public class NutritionStorage implements DateRangeDataRequest<Nutrition_log>{
    @Override
    public List<Nutrition_log> getDataByUserIdAndDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        // retrieve data from database and add to list

        return null;
    }
}
