package edu.farmingdale.CSC490.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {
    private int goal_id;
    private int user_id;
    private String goal_type;
    private double target_weight; // set as double
    private double target_body_fat_percent; // set as double
    private LocalDate start_date;
    private LocalDate target_date;
    private String status;
    private Instant created_at;
}
