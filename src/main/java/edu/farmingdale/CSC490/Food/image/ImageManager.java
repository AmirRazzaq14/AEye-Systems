package edu.farmingdale.CSC490.Food.image;


import edu.farmingdale.CSC490.Food.exception.imageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

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
    public String encodeImageFromMultipartFile(MultipartFile image)  {
        log.info("Encode Image From Multipart File");
        try {
            if (image == null || image.isEmpty()) {
                log.error("Received empty or null image file");
                throw new imageException(10100,  "Received empty or null image file", "");
            }
            
            byte[] imageBytes = image.getBytes();
            if (imageBytes.length == 0) {
                log.error("Image file is empty");
                throw new imageException(10101,  "Image file is empty", "");
            }

            String encoded = Base64.getEncoder().encodeToString(imageBytes);
            if (isValidBase64(encoded)) {
                log.error("Invalid Base64 string");
                throw new imageException(10102,  "Invalid Base64 string", "");
            }

            return encoded;
        } catch (IOException e) {
            log.error("Failed to process multipart file: {}", e.getMessage());
            throw new imageException(10103,  "Failed to process multipart file", e.getMessage());
        }
    }

    /**
     * Port 2 : get image from file path
     * @param imagePath the image path from front end
     * return the image 64-bit encoded string
     */

    public String encodeImageFromFile(String imagePath){
        log.info("Encode Image From File");

        if (imagePath == null || imagePath.isEmpty()) {
            log.error("Received empty or null image path");
            throw new imageException(10104,  "Received empty or null image path", "");
        }

        byte[] imageBytes = readFile(imagePath);
        if (imageBytes.length == 0) {
            log.error("Image file is empty: {}", imagePath);
            throw new imageException(10105,  "Image file is empty", "");
        }

        String encoded = Base64.getEncoder().encodeToString(imageBytes);
        if (isValidBase64(encoded)) {
            log.error("Invalid Base64 string: {}", imagePath);
            throw new imageException(10106,  "Invalid Base64 string", "");
        }

        return encoded;
    }



    /**
     * Verify that the Base64 string is valid
     */
    private boolean isValidBase64(String base64) {
        try {
            Base64.getDecoder().decode(base64);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * a tool method , read image from file path
     * @param filePath  the image path
     * @return the image bytes
     */
    private byte[] readFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("File does not exist: {}", filePath);
                throw new imageException(100107,  "image File does not exist", "");
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Failed to read file {}: {}", filePath, e.getMessage());
            throw new imageException(100108,  "Failed to read image file", "");
        }
    }

}
