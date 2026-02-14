package edu.farmingdale.CSC490.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workout_log {
    private int session_log_id;
    private int user_id;
    private LocalDate workout_date;
    private int exercises_done;
    private int reps_per_exercise;
    private String workout_length;
    private String intensity_amount;
    private String notes;
    private Instant logged_at;


}
