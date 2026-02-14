package edu.farmingdale.CSC490.Dashboard.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivity {
    private String type; //exercise, nutrition, measurement
    private String title;
    private String info;
    private String time;
}
