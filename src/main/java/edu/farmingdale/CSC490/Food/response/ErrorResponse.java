package edu.farmingdale.CSC490.Food.response;

public record ErrorResponse(
        int code,
        String message,
        String detail,
        String path,
        long timestamp
) {
    public ErrorResponse(int code, String message, String detail, String path) {
        this(code, message, detail, path, System.currentTimeMillis());
    }
}