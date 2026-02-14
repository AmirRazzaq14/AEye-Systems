package edu.farmingdale.CSC490.Dashboard.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chart {
    private String title;
    private Map<String, Integer> data; // day and value
}
