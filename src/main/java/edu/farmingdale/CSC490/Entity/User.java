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
public class User {

    private int user_id;
    private String email;
    private String password;
    private String first_name;
    private String last_name;
    private LocalDate date_of_birth;
    private String gender;
    private int height_in;
    private int weight_in;
    private LocalDate created_date;


}
