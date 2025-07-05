package com.moderation.sentinel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
// Entry point of Spring Boot App
public class SentinelApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SentinelApplication.class, args);
    }
}
