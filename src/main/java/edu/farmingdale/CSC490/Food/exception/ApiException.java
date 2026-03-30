package edu.farmingdale.CSC490.Food.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final int code;
    private final String detail;

    public ApiException(int code, String message, String detail) {
        super(message);
        this.code = code;
        this.detail = detail;
    }

}