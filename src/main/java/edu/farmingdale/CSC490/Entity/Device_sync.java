package edu.farmingdale.CSC490.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device_sync {
    private int sync_id;
    private int user_id;
    private String device_type;
    private String device_id; // from local device ?
    private String sync_data; // need json format
    private Instant synced_at;
    private boolean sync_status;

}
