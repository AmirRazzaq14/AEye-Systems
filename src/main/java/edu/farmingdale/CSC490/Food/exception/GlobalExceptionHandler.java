package edu.farmingdale.CSC490.Food.exception;

import edu.farmingdale.CSC490.Food.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(
            ApiException e,
            HttpServletRequest request) {

        log.warn("API Exception: {} - {}", e.getMessage(), e.getDetail());

        ErrorResponse error = new ErrorResponse(
                e.getCode(),
                e.getMessage(),
                e.getDetail(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(imageException.class)
    public ResponseEntity<ErrorResponse> handleImage(
            imageException e,
            HttpServletRequest request) {

        log.warn("Image Exception: {} - {}", e.getMessage(), e.getDetail());

        ErrorResponse error = new ErrorResponse(
                e.getCode(),
                e.getMessage(),
                e.getDetail(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(promptException.class)
    public ResponseEntity<ErrorResponse> handlePrompt(
            promptException e,
            HttpServletRequest request) {

        log.warn("Prompt Exception: {} - {}", e.getMessage(), e.getDetail());

        ErrorResponse error = new ErrorResponse(
                e.getCode(),
                e.getMessage(),
                e.getDetail(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(parserException.class)
    public ResponseEntity<ErrorResponse> handleParser(
            parserException e,
            HttpServletRequest request) {

        log.warn("Parser Exception: {} - {}", e.getMessage(), e.getDetail());

        ErrorResponse error = new ErrorResponse(
                e.getCode(),
                e.getMessage(),
                e.getDetail(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(foodAnalyzeException.class)
    public ResponseEntity<ErrorResponse> handleFoodAnalyze(
            foodAnalyzeException e,
            HttpServletRequest request) {

        log.warn("Food Analyze Exception: {} - {}", e.getMessage(), e.getDetail());

        ErrorResponse error = new ErrorResponse(
                e.getCode(),
                e.getMessage(),
                e.getDetail(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AISuggestionException.class)
    public ResponseEntity<ErrorResponse> handleAISuggestion(
            AISuggestionException e,
            HttpServletRequest request) {
        log.warn("AI Suggestion Exception: {} - {}", e.getMessage(), e.getDetail());
        ErrorResponse error = new ErrorResponse(
                e.getCode(),
                e.getMessage(),
                e.getDetail(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(
            Exception e,
            HttpServletRequest request) {

        log.error("Unknown System Exception: ", e);

        ErrorResponse error = new ErrorResponse(
                50000,
                "The system is busy, please try again later",
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}