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
public class Photo_upload {
    private int photo_id;
    private int user_id;
    private String photo_url; // int -> String
    private String photo_type;
    private String file_size;
    private String metadata;
    private Instant uploaded_at;
}
