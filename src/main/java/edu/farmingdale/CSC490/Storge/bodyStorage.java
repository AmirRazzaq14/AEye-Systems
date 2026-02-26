package edu.farmingdale.CSC490.Storge;

import edu.farmingdale.CSC490.Entity.Body_measurement;

import java.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class bodyStorage implements DateRangeDataRequest<Body_measurement> {

    private final MockDatabase mockDatabase;

    public bodyStorage(MockDatabase mockDatabase) {
        this.mockDatabase = mockDatabase;
    }

    @Override
    public List<Body_measurement> getDataByUserIdAndDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        return mockDatabase.bodyMeasurements.stream()
                .filter(measurement -> measurement.getUser_id() == userId)
                .filter(measurement -> {
                    LocalDate recordedDate = measurement.getRecorded_at() != null
                            ? measurement.getRecorded_at().atZone(ZoneId.systemDefault()).toLocalDate()
                            : LocalDate.MIN;
                    return !recordedDate.isBefore(startDate) && !recordedDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }
}
