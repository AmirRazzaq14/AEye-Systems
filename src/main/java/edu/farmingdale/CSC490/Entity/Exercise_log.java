package edu.farmingdale.CSC490.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise_log {
    private int log_id;
    private int session_id;
    private int exercise_id;
    private int sets_completed;
    private int reps_per_set;
    private int weight_lbs;
    private String notes;
}