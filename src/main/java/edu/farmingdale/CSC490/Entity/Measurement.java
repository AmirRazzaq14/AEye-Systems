package edu.farmingdale.CSC490.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Measurement {
    private int measurement_id;
    private int user_id;
    private int weight_ibs;
    private int chest_in;
    private int waist_in;
    private int arms_in;
    private int legs_in;
    private LocalDate date;
}
