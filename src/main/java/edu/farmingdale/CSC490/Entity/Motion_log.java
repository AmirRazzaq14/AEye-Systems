package edu.farmingdale.CSC490.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Motion_log {
    /** Firestore document id; usually yyyy-mm-dd. */
    private String id;
    private String date;

    /** User-entered weight used for estimation (kg). */
    private double weightKg;

    /** Total calories estimated for the day/session (kcal). */
    private double caloriesTotal;

    /**
     * Per exercise breakdown.
     * Example:
     * {
     *   "squat": {"reps": 12, "kcal": 3.4},
     *   "arm_raise": {"reps": 20, "kcal": 2.8}
     * }
     */
    private Map<String, Map<String, Object>> perExercise;

    /** ISO-8601 timestamp for last update. */
    private String updatedAt;
}

