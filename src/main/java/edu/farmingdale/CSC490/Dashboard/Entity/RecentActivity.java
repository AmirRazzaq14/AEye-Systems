package edu.farmingdale.CSC490.Dashboard.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivity {
    private String type; //workout, nutrition, measurement
    private String info;
    private Instant time;
}
