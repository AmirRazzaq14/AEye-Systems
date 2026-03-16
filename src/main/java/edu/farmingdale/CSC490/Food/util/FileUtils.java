package edu.farmingdale.CSC490.Food.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Component
public class FileUtils {

    public Optional<byte[]> readFile(String filePath) {
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
    
    public Optional<String> readTextFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("Text File does not exist: {}", filePath);
                return Optional.empty();
            }
            return Optional.of(Files.readString(path).trim());
        } catch (IOException e) {
            log.error("Failed to read text file {}: {}", filePath, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> tempFile(MultipartFile image) throws  IOException {
        if(image == null){
            log.error("Image is null");
            return Optional.empty();
        }
        Path tempImageFile = Files.createTempFile("foodFile", "_" + image.getOriginalFilename());
        Files.write(tempImageFile, image.getBytes());
        return Optional.of(tempImageFile.toString());
    }
}