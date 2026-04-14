package edu.farmingdale.CSC490.Food.exception;

import lombok.Getter;

@Getter
public class AISuggestionException extends RuntimeException{

    private final int code;
    private final String detail;

    public AISuggestionException(int code, String message, String detail) {
        super(message);
        this.code = code;
        this.detail = detail;
    }
}
