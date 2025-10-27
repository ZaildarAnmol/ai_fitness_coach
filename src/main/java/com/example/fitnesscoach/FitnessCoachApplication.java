package com.example.fitnesscoach;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.io.File;

@SpringBootApplication
public class FitnessCoachApplication {
    public static void main(String[] args) {
        SpringApplication.run(FitnessCoachApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createDirectoriesOnStartup(){
        new File("uploads").mkdirs();
        new File("outputs").mkdirs();
    }
}
