package edu.farmingdale.CSC490.Food.AOP;

import edu.farmingdale.CSC490.Food.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Aspect
@Component
@Slf4j
public class RetryAspect {

    @Around("@annotation(retryable) && @annotation(retryable)")
    public Object handleGeminiRetries(ProceedingJoinPoint joinPoint, Retryable retryable) throws Throwable {
        int maxRetries = retryable.maxRetries();
        int retryCount = 0;

        log.info("Retrying Gemini API call with maxRetries={}", maxRetries);

        while (true) {
            try {
                return joinPoint.proceed();
            } catch (ApiException e) {
                if (e.getCode() == 10312 && retryCount < maxRetries) {
                    retryCount++;
                    long delay = (long) (Math.pow(2, retryCount) * 1000);
                    delay += ThreadLocalRandom.current().nextInt(1000); // jitter

                    log.warn("Gemini API unavailable. Retrying in {} ms (attempt {})", delay, retryCount);
                    Thread.sleep(delay);

                } else {
                    log.error("Gemini API failed after {} retries", retryCount);
                    throw e;
                }
            }
        }

    }
}
