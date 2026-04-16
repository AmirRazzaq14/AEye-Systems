package edu.farmingdale.CSC490.Food.AOP;

import edu.farmingdale.CSC490.Food.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

import static edu.farmingdale.CSC490.Food.exception.ApiException.GEMINI_TEMPORARILY_UNAVAILABLE;

@Aspect
@Component
@Slf4j
public class RetryAspect {

    @Around("@annotation(retryable)")
    public Object handleGeminiRetries(ProceedingJoinPoint joinPoint, Retryable retryable) throws Throwable {
        int maxRetries = retryable.maxRetries();
        int retryCount = 0;

        log.info("Starting Gemini API call with maxRetries={}", maxRetries);

        while (true) {
            try {
                return joinPoint.proceed();
            } catch (ApiException e) {
                if (e.getCode() == GEMINI_TEMPORARILY_UNAVAILABLE && retryCount < maxRetries) {
                    retryCount++;
                    long delay = (long) (Math.pow(2, retryCount) * 1000);
                    delay += ThreadLocalRandom.current().nextInt(1000);

                    log.warn("Gemini API unavailable (code: {}). Retrying in {} ms (attempt {}/{})", 
                            e.getCode(), delay, retryCount, maxRetries);
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Retry interrupted", ie);
                        throw ie;
                    }
                } else {
                    log.error("Gemini API failed with code {} after {} retries. No more retries.", 
                            e.getCode(), retryCount);
                    throw e;
                }
            } catch (RuntimeException e) {
                if (e.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    log.error("Operation interrupted, no retry");
                    throw new RuntimeException(e.getCause());
                } else if (e.getCause() instanceof java.io.IOException) {
                    if (retryCount < maxRetries) {
                        retryCount++;
                        long delay = (long) (Math.pow(2, retryCount) * 1000);
                        delay += ThreadLocalRandom.current().nextInt(1000);

                        log.warn("Network error detected. Retrying in {} ms (attempt {}/{})",
                                delay, retryCount, maxRetries);

                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.error("Retry interrupted during network retry", ie);
                            throw new RuntimeException(ie);
                        }
                    } else {
                        log.error("Network error persisted after {} retries", retryCount);
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        }
    }
}
