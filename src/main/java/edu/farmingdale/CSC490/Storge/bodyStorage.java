package edu.farmingdale.CSC490.Storge;

import edu.farmingdale.CSC490.Entity.Body_measurement;

import java.time.LocalDate;
import java.util.List;

public class bodyStorage implements DateRangeDataRequest<Body_measurement>{
    @Override
    public List<Body_measurement> getDataByUserIdAndDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        //  retrieve data from database and add to list
        return null;
    }
}
