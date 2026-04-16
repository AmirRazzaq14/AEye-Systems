package edu.farmingdale.CSC490.Food.exception;

import lombok.Getter;

@Getter
public class parserException extends RuntimeException{

    public static final int UNKNOWN_PARSER_EXCEPTION = 100400;
    public static final int UNKNOWN_JSON_PROCESSING_EXCEPTION = 100401;
    public static final int EMPTY_JSON_RESPONSE = 100402;
    public static final int INVALID_SERVER_TYPE = 100403;
    public static final int INVALID_OLLAMA_RESPONSE = 100404;
    public static final int INVALID_GEMINI_RESPONSE= 100405;

    private final int code;
    private final String detail;

    public parserException(int code, String message, String detail) {
        super(message);
        this.code = code;
        this.detail = detail;
    }
}
