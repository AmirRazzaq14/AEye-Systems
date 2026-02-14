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
    private double weight_ibs; //int -> double
    private double chest_in; //int -> double
    private double waist_in; // int -> double
    private double arms_in; // int -> double
    private double legs_in; //int -> double
    private LocalDate date;
}
