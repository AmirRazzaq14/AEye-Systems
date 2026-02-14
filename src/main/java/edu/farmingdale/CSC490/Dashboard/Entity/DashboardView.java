package edu.farmingdale.CSC490.Dashboard.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardView {

    private Card workoutCard;
    private Card nutritionCard;
    private Card goalCard;
    private Chart activityChart;
    private Chart nutritionChart;
    private List<RecentActivity> recentActivity; // list of recent activities on bottom

}
