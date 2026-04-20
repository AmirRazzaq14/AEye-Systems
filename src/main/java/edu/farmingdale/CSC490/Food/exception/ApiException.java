package edu.farmingdale.CSC490.Food.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    public static final int GEMINI_ERROR = 10311;
    public static final int GEMINI_TEMPORARILY_UNAVAILABLE = 10312;
    public static final int GEMINI_UNEXPECTED_ERROR = 10313;
    public static final int NETWORK_ERROR = 10314;
    public static final int INTERRUPTED_ERROR = 10315;


    private final int code;
    private final String detail;

    public ApiException(int code, String message, String detail) {
        super(message);
        this.code = code;
        this.detail = detail;
    }

}