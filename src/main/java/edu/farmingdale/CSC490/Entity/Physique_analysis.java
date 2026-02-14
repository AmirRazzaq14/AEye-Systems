package edu.farmingdale.CSC490.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Physique_analysis {
    private int analysis_id;
    private int user_id;
    private int photo_id;
    private String body_fat_percentage;
    private String posture_score;
    private String symmetry_score;
    private String analysis;
    private String ai_recommendations;
    private Instant analysis_time;
}
