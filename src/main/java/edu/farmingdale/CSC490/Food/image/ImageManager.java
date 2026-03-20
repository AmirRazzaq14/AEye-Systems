package edu.farmingdale.CSC490.Food.image;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;

/**
 * Manages the images. the outside port for image processing,
 * returns the image 64-bit encoded string
 */

@Slf4j
@Component
public class ImageManager {

    /**
     * Port 1 : get image from multipart file, which from front end
     * @param image the image from front end
     * return the image 64-bit encoded string
     */
    public Optional<String> encodeImageFromMultipartFile(MultipartFile image)  {
        try {
            if (image == null || image.isEmpty()) {
                log.error("Received empty or null image file");
                return Optional.empty();
            }
            
            byte[] imageBytes = image.getBytes();
            if (imageBytes.length == 0) {
                log.error("Image file is empty");
                return Optional.empty();
            }

            String encoded = Base64.getEncoder().encodeToString(imageBytes);
            if (!isValidBase64(encoded)) {
                log.error("Generated invalid Base64 string");
                return Optional.empty();
            }else{
                log.info("Successfully encoded image file:{}",encoded);
            }

            return Optional.of(encoded);
        } catch (IOException e) {
            log.error("Failed to process multipart file: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Verify that the Base64 string is valid
     */
    private boolean isValidBase64(String base64) {
        try {
            Base64.getDecoder().decode(base64);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


}
