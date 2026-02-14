package edu.farmingdale.CSC490.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workout_plan {
    private int plan_id;
    private int user_id;
    private int plan_type;
    private String notes;
}