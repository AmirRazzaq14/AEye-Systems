package edu.farmingdale.CSC490.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Body_measurement {
    private int measurement_id;
    private int analysis_id;
    private int user_id;
    private String body_part;
    private double length_in; // set as double
    private String measurement_type;
    private Instant recorded_at;
}
