// Entity/Exercise_log.java
package edu.farmingdale.CSC490.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor    // ← required for Firestore toObject()
@AllArgsConstructor
@Builder
public class Exercise_log {
    private String  id;
    private String  userId;
    private String  date;
    private int     log_id;
    private int     session_id;
    private int     exercise_id;
    private int     sets_completed;
    private int     reps_per_set;
    private int     weight_lbs;
    private String  notes;
    private String  updatedAt;
}