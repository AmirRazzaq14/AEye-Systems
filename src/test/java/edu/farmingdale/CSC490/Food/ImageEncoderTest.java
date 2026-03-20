package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Food.util.FileUtils;
import edu.farmingdale.CSC490.Food.util.ImageEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageEncoderTest {
    
    @Mock
    private FileUtils fileUtils;
    
    private ImageEncoder imageEncoder;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        imageEncoder = new ImageEncoder(fileUtils);
    }
    
    @Test
    void testEncodeImage_Success() throws ImageAnalysisException {
        // Arrange
        String imagePath = "/path/to/image.jpg";
        byte[] imageBytes = "fake image data".getBytes();
        
        when(fileUtils.readFile(imagePath)).thenReturn(Optional.of(imageBytes));
        
        // Act
        Optional<String> result = imageEncoder.encodeImage(imagePath);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(Base64.getEncoder().encodeToString(imageBytes), result.get());
    }
    
    @Test
    void testEncodeImage_FileNotFound(){
        // Arrange
        String imagePath = "/path/to/missing.jpg";
        
        when(fileUtils.readFile(imagePath)).thenReturn(Optional.empty());

        try {
            // Act
            imageEncoder.encodeImage(imagePath);

        }catch (ImageAnalysisException e){
            // Assert
            assertTrue(e.getMessage().contains("Failed to read image file"));
        }


    }
    
    @Test
    void testEncodeImage_EmptyFile(){
        // Arrange
        String imagePath = "/path/to/empty.jpg";
        byte[] emptyBytes = new byte[0];
        
        when(fileUtils.readFile(imagePath)).thenReturn(Optional.of(emptyBytes));

        try  {
            // Act
            imageEncoder.encodeImage(imagePath);

        } catch (ImageAnalysisException e) {
            // Assert
            assertTrue(e.getMessage().contains("Image file is empty"));
        }
    }
    
    @Test
    void testEncodeImage_WithRealFile(@TempDir Path tempDir) throws IOException, ImageAnalysisException {
        // Integration testing with real FileUtils
        FileUtils realFileUtils = new FileUtils();
        ImageEncoder realEncoder = new ImageEncoder(realFileUtils);
        
        // Create a test image file
        Path testImage = tempDir.resolve("test.jpg");
        byte[] testData = "test image content".getBytes();
        Files.write(testImage, testData);


        // Act
        Optional<String> result = realEncoder.encodeImage(testImage.toString());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(Base64.getEncoder().encodeToString(testData), result.get());

    }
}