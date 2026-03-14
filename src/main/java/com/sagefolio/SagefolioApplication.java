package com.sagefolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SagefolioApplication {
    public static void main(String[] args) {
        SpringApplication.run(SagefolioApplication.class, args);
    }
}
