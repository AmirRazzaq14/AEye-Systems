package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Food.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {
    
    private FileUtils fileUtils;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        fileUtils = new FileUtils();
    }
    
    @Test
    void testReadFile_Success() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.txt");
        String content = "Hello, World!";
        Files.writeString(testFile, content);
        
        // Act
        Optional<byte[]> result = fileUtils.readFile(testFile.toString());
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(content, new String(result.get()));
    }
    
    @Test
    void testReadFile_FileNotExists() {
        // Arrange
        String nonExistentFile = tempDir.resolve("nonexistent.txt").toString();
        
        // Act
        Optional<byte[]> result = fileUtils.readFile(nonExistentFile);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testReadFile_EmptyFile() throws IOException {
        // Arrange
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);
        
        // Act
        Optional<byte[]> result = fileUtils.readFile(emptyFile.toString());
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(0, result.get().length);
    }
    
    @Test
    void testReadTextFile_Success() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.txt");
        String content = "Hello, World!";
        Files.writeString(testFile, content);
        
        // Act
        Optional<String> result = fileUtils.readTextFile(testFile.toString());
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(content, result.get());
    }
    
    @Test
    void testReadTextFile_WithWhitespace() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.txt");
        String content = "  Hello, World!  \n  ";
        Files.writeString(testFile, content);
        
        // Act
        Optional<String> result = fileUtils.readTextFile(testFile.toString());
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(content.trim(), result.get());
        assertNotEquals(content, result.get());
    }

}