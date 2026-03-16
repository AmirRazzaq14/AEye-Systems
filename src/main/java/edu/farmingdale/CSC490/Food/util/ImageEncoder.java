package edu.farmingdale.CSC490.Food.util;

import edu.farmingdale.CSC490.Food.exception.ImageAnalysisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Component
public class ImageEncoder {

    private final FileUtils fileUtils;
    
    public ImageEncoder(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }
    
    public Optional<String> encodeImage(String imagePath) throws ImageAnalysisException{
        Optional<byte[]> imageBytes = fileUtils.readFile(imagePath);
        
        if (imageBytes.isEmpty()) {
            log.error("Failed to read image file: {}", imagePath);
            throw new ImageAnalysisException("Failed to read image file: " + imagePath);
        }
        
        if (imageBytes.get().length == 0) {
            log.error("Image file is empty: {}", imagePath);
            throw new ImageAnalysisException("Image file is empty: " + imagePath);
        }
        
        String encoded = Base64.getEncoder().encodeToString(imageBytes.get());
        return Optional.of(encoded);
    }



}