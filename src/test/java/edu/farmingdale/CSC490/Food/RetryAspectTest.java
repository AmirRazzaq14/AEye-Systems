package edu.farmingdale.CSC490.Food;

import edu.farmingdale.CSC490.Food.AOP.RetryAspect;
import edu.farmingdale.CSC490.Food.AOP.Retryable;
import edu.farmingdale.CSC490.Food.exception.ApiException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static edu.farmingdale.CSC490.Food.exception.ApiException.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryAspectTest {

    private RetryAspect retryAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Retryable retryable;

    @BeforeEach
    void setUp() {
        retryAspect = new RetryAspect();
    }

    @Test
    void testSuccessfulCall_NoRetry() throws Throwable {
        // Arrange
        Object expectedResult = "success";
        when(joinPoint.proceed()).thenReturn(expectedResult);
        when(retryable.maxRetries()).thenReturn(3);

        // Act
        Object result = retryAspect.handleGeminiRetries(joinPoint, retryable);

        // Assert
        assertEquals(expectedResult, result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testApiException503_ShouldRetryAndSucceed() throws Throwable {
        // Arrange
        when(retryable.maxRetries()).thenReturn(3);
        
        ApiException apiException = new ApiException(GEMINI_TEMPORARILY_UNAVAILABLE, "Service unavailable", "503 error");
        Object expectedResult = "success after retry";
        
        when(joinPoint.proceed())
            .thenThrow(apiException)
            .thenThrow(apiException)
            .thenReturn(expectedResult);

        // Act
        Object result = retryAspect.handleGeminiRetries(joinPoint, retryable);

        // Assert
        assertEquals(expectedResult, result);
        verify(joinPoint, times(3)).proceed();
    }

    @Test
    void testExceedMaxRetries_ShouldThrowException() throws Throwable {
        // Arrange
        when(retryable.maxRetries()).thenReturn(2);
        
        ApiException apiException = new ApiException(GEMINI_TEMPORARILY_UNAVAILABLE, "Service unavailable", "503 error");
        when(joinPoint.proceed()).thenThrow(apiException);

        // Act
        ApiException thrown = assertThrows(ApiException.class, () -> retryAspect.handleGeminiRetries(joinPoint, retryable));

        // Assert
        assertEquals(GEMINI_TEMPORARILY_UNAVAILABLE, thrown.getCode());
        verify(joinPoint, times(3)).proceed();
    }

    @Test
    void testNon503Error_ShouldNotRetry() throws Throwable {
        // Arrange
        when(retryable.maxRetries()).thenReturn(3);
        
        ApiException apiException = new ApiException(GEMINI_UNEXPECTED_ERROR, "Bad request", "400 error");
        when(joinPoint.proceed()).thenThrow(apiException);

        // Act & Assert
        ApiException thrown = assertThrows(ApiException.class, () -> retryAspect.handleGeminiRetries(joinPoint, retryable));
        
        assertEquals(GEMINI_UNEXPECTED_ERROR, thrown.getCode());
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testInterruptedException_ShouldNotRetry() throws Throwable {
        // Arrange
        when(retryable.maxRetries()).thenReturn(1);
        
        RuntimeException runtimeException = new RuntimeException(new InterruptedException("Thread interrupted"));
        when(joinPoint.proceed()).thenThrow(runtimeException);

        // Act
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> retryAspect.handleGeminiRetries(joinPoint, retryable));

        // Assert
        assertInstanceOf(InterruptedException.class, thrown.getCause());
        verify(joinPoint, times(1)).proceed();


    }

    @Test
    void testIOExceptionWrappedInRuntimeException_ShouldRetry() throws Throwable {
        // Arrange
        when(retryable.maxRetries()).thenReturn(2);
        
        RuntimeException networkException = new RuntimeException(new java.io.IOException("Connection timeout"));
        Object expectedResult = "success after network retry";
        
        when(joinPoint.proceed())
            .thenThrow(networkException)
            .thenReturn(expectedResult);

        // Act
        Object result = retryAspect.handleGeminiRetries(joinPoint, retryable);

        // Assert
        assertEquals(expectedResult, result);
        verify(joinPoint, times(2)).proceed();
    }

    @Test
    void testNonNetworkRuntimeException_ShouldNotRetry() throws Throwable {
        // Arrange
        when(retryable.maxRetries()).thenReturn(3);
        
        RuntimeException businessException = new RuntimeException(new IllegalArgumentException("Invalid input"));
        when(joinPoint.proceed()).thenThrow(businessException);

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> retryAspect.handleGeminiRetries(joinPoint, retryable));
        
        assertEquals(businessException, thrown);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void testExponentialBackoffDelayCalculation() throws Throwable {
        // Arrange
        when(retryable.maxRetries()).thenReturn(3);

        ApiException apiException = new ApiException(GEMINI_TEMPORARILY_UNAVAILABLE, "Service unavailable", "503 error");
        Object expectedResult = "success";
        
        when(joinPoint.proceed())
            .thenThrow(apiException)
            .thenThrow(apiException)
            .thenThrow(apiException)
            .thenReturn(expectedResult);

        long startTime = System.currentTimeMillis();

        // Act
        Object result = retryAspect.handleGeminiRetries(joinPoint, retryable);

        long elapsed = System.currentTimeMillis() - startTime;

        // Assert
        assertEquals(expectedResult, result);
        verify(joinPoint, times(4)).proceed();

        long minExpectedDelay = 2000 + 4000 + 8000;
        long maxExpectedDelay = minExpectedDelay + 3000;

        assertTrue(elapsed >= minExpectedDelay,
                "Expected at least " + minExpectedDelay + "ms delay (2s + 4s + 8s)");
        assertTrue(elapsed < maxExpectedDelay,
                "Expected less than " + maxExpectedDelay + "ms (with jitter)");
    }
}
