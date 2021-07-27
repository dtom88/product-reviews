package com.somecmpn.reviews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SpringBootConfiguration
public class ProductReviewsApp {
    public static final String API_PREFIX = "/api/v1";
    public static void main(String... args) {
        SpringApplication.run(ProductReviewsApp.class, args);
    }
}
