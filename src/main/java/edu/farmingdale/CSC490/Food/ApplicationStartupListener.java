package edu.farmingdale.CSC490.Food;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener {

    @Autowired(required = false)
    private PythonServiceManager pythonServiceManager;

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (pythonServiceManager != null) {
            System.out.println("Application startup: Attempting to start Python service...");
            pythonServiceManager.startPythonService();
        }
    }
}
