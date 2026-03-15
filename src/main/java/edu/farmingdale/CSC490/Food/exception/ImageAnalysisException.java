package edu.farmingdale.CSC490.Food.exception;

public class ImageAnalysisException extends Exception {
    
    public ImageAnalysisException(String message) {
        super(message);
    }
    
    public ImageAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}