package edu.farmingdale.CSC490.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {
    private int exercise_id;
    private int exercise_name;
    private String muscle_group; // int -> String
    private String notes;
}
