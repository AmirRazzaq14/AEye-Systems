package edu.farmingdale.CSC490.Food;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);

    @Autowired(required = false)
    private PythonServiceManager pythonServiceManager;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent(ApplicationReadyEvent event) {
        if (pythonServiceManager != null) {
            logger.info("Starting Python service...");
            pythonServiceManager.startPythonService();
        }
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationClosedEvent(ContextClosedEvent event) {
        if (pythonServiceManager != null) {
            logger.info("Stopping Python service...");
            pythonServiceManager.stopPythonService();
        }
    }
}
