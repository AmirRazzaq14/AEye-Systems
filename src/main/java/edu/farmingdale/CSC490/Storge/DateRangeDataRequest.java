package edu.farmingdale.CSC490.Storge;

import java.time.LocalDate;
import java.util.List;

public interface DateRangeDataRequest<T> {

    List<T> getDataByUserIdAndDateRange(int userId, LocalDate startDate, LocalDate endDate);
}
