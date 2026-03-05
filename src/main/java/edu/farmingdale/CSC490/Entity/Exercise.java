package edu.farmingdale.CSC490.Entity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {
    @DocumentId
    private String exercise_id;      // changed int → String (Firestore uses String IDs)
    private String exercise_name;    // changed int → String (was a bug)
    private String muscle_group;
    private String notes;
}