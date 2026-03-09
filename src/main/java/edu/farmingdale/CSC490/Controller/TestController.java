package edu.farmingdale.CSC490.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String testServer() {
        return "WizCoach Spring Boot Backend is running successfully!";
    }
}
