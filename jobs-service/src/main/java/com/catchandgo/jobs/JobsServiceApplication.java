package com.catchandgo.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.catchandgo")
public class JobsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobsServiceApplication.class, args);
    }
}
