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
public class imageManager {

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
            return Optional.of(encoded);
        } catch (IOException e) {
            log.error("Failed to process multipart file: {}", e.getMessage());
            return Optional.empty();
        }
    }
    /**
     * Port 2 : get image from file path
     * @param imagePath the image path from front end
     * return the image 64-bit encoded string
     */

    public Optional<String> encodeImageFromFile(String imagePath){
        Optional<byte[]> imageBytes = readFile(imagePath);

        if (imageBytes.isEmpty()) {
            log.error("Failed to read image file: {}", imagePath);
            return Optional.empty();
        }

        if (imageBytes.get().length == 0) {
            log.error("Image file is empty: {}", imagePath);
            return Optional.empty();
        }

        String encoded = Base64.getEncoder().encodeToString(imageBytes.get());
        return Optional.of(encoded);
    }

    /**
     * a tool method , read image from file path
     * @param filePath  the image path
     * @return the image bytes
     */
    private Optional<byte[]> readFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("File does not exist: {}", filePath);
                return Optional.empty();
            }
            return Optional.of(Files.readAllBytes(path));
        } catch (IOException e) {
            log.error("Failed to read file {}: {}", filePath, e.getMessage());
            return Optional.empty();
        }
    }

}
