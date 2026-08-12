package com.vsu.researchapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResearchappApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResearchappApplication.class, args);
    }
}
