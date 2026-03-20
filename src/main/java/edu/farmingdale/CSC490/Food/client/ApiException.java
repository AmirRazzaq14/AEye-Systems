package edu.farmingdale.CSC490.Food.client;

import lombok.Getter;

@Getter
public class ApiException extends Exception {
    
    private int statusCode;
    
    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

}