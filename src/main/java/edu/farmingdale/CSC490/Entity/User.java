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
public class User {
    @DocumentId
    private String user_id;
    private String email;
    private String password;
    
    // Core Profile Metrics
    private Double weight;
    private Double height;
    private String gender;
    private Integer age;
}