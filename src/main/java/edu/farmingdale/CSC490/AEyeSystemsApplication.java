package edu.farmingdale.CSC490;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AEyeSystemsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AEyeSystemsApplication.class, args);
        System.out.println("Your can started on http://localhost:8080/nutrition.html");
    }
}