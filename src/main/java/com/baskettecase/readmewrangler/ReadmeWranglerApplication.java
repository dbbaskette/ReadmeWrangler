package com.baskettecase.readmewrangler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for README Wrangler.
 * A Spring Boot application that polishes repository documentation using Embabel.
 */
@SpringBootApplication
public class ReadmeWranglerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReadmeWranglerApplication.class, args);
    }
}
